package it.polito.tdp.emergency.model;

import java.util.PriorityQueue;

import it.polito.tdp.emergency.model.Event.EventType;
import it.polito.tdp.emergency.model.Patient.PatientStatus;

public class Simulator {

	// Simulation parameters

	private int NS; // number of studios

	private int DURATION_TRIAGE = 5 * 60;
	private int DURATION_WHITE = 10 * 60;
	private int DURATION_YELLOW = 15 * 60;
	private int DURATION_RED = 30 * 60;

	private int WHITE_TIMEOUT = 30 * 60;
	private int YELLOW_TIMEOUT = 30 * 60;
	private int RED_TIMEOUT = 60 * 60;

	// World model
	private PriorityQueue<Patient> waitingRoom;//è una combinazione di codice e tempo di arrivo la priorità
	private int occupiedStudios = 0; //non creo una classe studio perche in questo caso sono tutti uguali e a me interessa sapere quali e quanti sono liberi
	
	// Measures of Interest
	private int patientsTreated = 0;
	private int patientsDead = 0;
	private int patientsAbandoned = 0;

	// Event queue
	private PriorityQueue<Event> queue;

	public Simulator(int NS) {
		this.NS = NS;
		this.queue = new PriorityQueue<>();
		this.waitingRoom = new PriorityQueue<>(new PatientComparator());//passandogli il comparatore la coda usera questo metodo per ordinare gli elementi e non il metodo comparator presente nell'oggetto comparabile
	}

	public void addPatient(Patient patient, int time) {
		patient.setStatus(PatientStatus.NEW);
		Event e=new Event(patient,time+DURATION_TRIAGE,EventType.TRIAGE);
		queue.add(e);
	}

	public void run() {
		while (!queue.isEmpty()) {
			Event e = queue.poll();
			System.out.println(e);

			switch (e.getType()) {
			case TRIAGE:
				processTriageEvent(e);
				break;
			case TIMEOUT:
				processTimeoutEvent(e);
				break;
			case FREE_STUDIO:
				processFreeStudioEvent(e);
				break;
			}
		}
	}

	/**
	 * A patient finished treatment. The studio is freed, and a new patient is
	 * called in.
	 * 
	 * @param e
	 */
	private void processFreeStudioEvent(Event e) {
		Patient p=e.getPatient();
		//un paziente ha liberato lo studio
		this.patientsTreated++;
		p.setStatus(PatientStatus.OUT);
		this.occupiedStudios--;
		
		//devo chiamare il prossimo paziente dalla lista di attesa
		Patient next =waitingRoom.poll();
		if(next!=null){
			//lo mando in  studio
			int duration=0;
			if(next.getStatus()==PatientStatus.WHITE){
				duration=DURATION_WHITE;
			}else if(next.getStatus()==PatientStatus.YELLOW){
				duration=DURATION_YELLOW;
			}else if(next.getStatus()==PatientStatus.RED){
				duration=DURATION_RED;	
			}
			
			this.occupiedStudios++;
			p.setStatus(PatientStatus.TREATING);
			
			//eliminare i timeout futuri dalla coda degli eventi - come fare? lo indico nello switch			
			
			//trattare un paziente significa dire che libererà lo studio dopo il tempo di trattamento
			queue.add(new Event(p,e.getTime()+duration,EventType.FREE_STUDIO));
			
		}
	}

	private void processTimeoutEvent(Event e) {
		
		//genero eventi time out solo quando un paziente va a finire in lista di attesa
		Patient p=e.getPatient();
		
		switch(p.getStatus()){
		case WHITE:
			//ABBANDONA
			//toglierlo dalla lista di attesa aggiornare la lista degli abbandoni
			this.patientsAbandoned++;
			p.setStatus(PatientStatus.OUT);
			waitingRoom.remove(p);
			break;
		case YELLOW:
			//DIVENTA ROSSO
			//cambia status ->devo toglierlo cambaire statoi e rimetterlo affinche la coda si accorga del nuovo stato
			waitingRoom.remove(p);
			p.setStatus(PatientStatus.RED);
			waitingRoom.add(p);
			queue.add(new Event(p,e.getTime()+RED_TIMEOUT,EventType.TIMEOUT));
			break;
		case RED:
			//muori
			this.patientsDead++;
			p.setStatus(PatientStatus.BLACK);
			waitingRoom.remove(p);
			break;
		
		case OUT:
		case TREATING:
			//timeout arrivato troppo tardi, non serve piu e lo ignoriamo
			break;
		default:
			throw new InternalError("Stato paziente errato "+p.toString());
		}
		
	}

	/**
	 * Patient goes out of triage. A severity code is assigned. If a studio is
	 * free, then it is immediately assigned. Otherwise, he is put in the waiting
	 * list.
	 * 
	 * @param e
	 */
	private void processTriageEvent(Event e) {
		Patient p=e.getPatient();
		//fine del triage
		
		//devo assegnare un codice di gravità (random)
		int rand=(int) (1+Math.random()*3);
		if(rand==1)
			p.setStatus(PatientStatus.WHITE);
		else if(rand==2)
			p.setStatus(PatientStatus.YELLOW);
		else if(rand==3)
			p.setStatus(PatientStatus.RED);
		
		//se c'è uno studio libero lo mando in cura
		if(this.occupiedStudios<NS){
			// i primi tre pazienti al mattini entreranno negli studi e uscendo liberano lo studio quindi chimao il prossimo paziente
			int duration=0;
			if(p.getStatus()==PatientStatus.WHITE){
				duration=DURATION_WHITE;
			}else if(p.getStatus()==PatientStatus.YELLOW){
				duration=DURATION_YELLOW;
			}else if(p.getStatus()==PatientStatus.RED){
				duration=DURATION_RED;	
			}
			
			this.occupiedStudios++;
			p.setStatus(PatientStatus.TREATING);
			
			//trattare un paziente significa dire che libererà lo studio dopo il tempo di trattamento
			queue.add(new Event(p,e.getTime()+duration,EventType.FREE_STUDIO));
			
		}else{
			//lo metto in lista di attesa
			//devo schedulare l'evento di time out che dipende dalla gravità
			
			int timeout=0;
			if(p.getStatus()==PatientStatus.WHITE){
				timeout=WHITE_TIMEOUT;
			}else if(p.getStatus()==PatientStatus.YELLOW){
				timeout=YELLOW_TIMEOUT;
			}else if(p.getStatus()==PatientStatus.RED){
				timeout=RED_TIMEOUT;	
			}
			
			p.setQueueTime(e.getTime());//dice che sei entrato in lista d'attesa al tempo del triage
			waitingRoom.add(p);//non posso impostare il time dopo averlo aggiunto alla coda perche il tempo viente usato dalla coda stessa per l ordinamento degli eventi
			
			queue.add(new Event(p,e.getTime()+timeout,EventType.TIMEOUT));
			
		}
			
		
		//se no lo metto in lista di attesa
	
	}

	public int getPatientsTreated() {
		return patientsTreated;
	}

	public int getPatientsDead() {
		return patientsDead;
	}

	public int getPatientsAbandoned() {
		return patientsAbandoned;
	}
}

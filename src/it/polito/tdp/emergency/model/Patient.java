package it.polito.tdp.emergency.model;

public class Patient {
	
	public enum PatientStatus { NEW, WHITE, YELLOW, RED, BLACK, TREATING, OUT } ;
	
	private String name ;
	private PatientStatus status ;
	private int queueTime ; //si ricorda quando il paziente è entrato nella lista per permettere il confronto nella coda di priorita
	//lista di attesa->il paziente non sa quando uscirà da tale lista xk dipende dagli altri pazienti si trova in lista quando ha assegnato un colore
	//consideriamo la gravità e a parita di gravita dal tempo di attesa
	
	public Patient(String name) {
		super();
		this.name = name;
		this.status = PatientStatus.NEW ;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PatientStatus getStatus() {
		return status;
	}
	public void setStatus(PatientStatus status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return String.format("[%s-%s]", name, status);
	}
	public int getQueueTime() {
		return queueTime;
	}
	public void setQueueTime(int queueTime) {
		this.queueTime = queueTime;
	}
	

}

package it.polito.tdp.emergency;

import it.polito.tdp.emergency.model.Patient;
import it.polito.tdp.emergency.model.Simulator;

public class Main {

	public static void main(String[] args) {
		Simulator sim = new Simulator(15) ;
		
		for(int i=0; i<50; i++) {
			Patient p = new Patient("Pat"+String.valueOf(i)) ;
			sim.addPatient(p, 8*60 + i * 10);
		}
		
		sim.run();
		
		System.out.println("Treated:   " + sim.getPatientsTreated());
		System.out.println("Abandoned: " + sim.getPatientsAbandoned());
		System.out.println("Dead:      " + sim.getPatientsDead());


	}

}

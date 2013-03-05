package org.enernoc.oadr2.vtn


/**
 * A class to represent the VENStatus creation form
 * for Play's automatic binding of form fields to Objects
 * This object is used to track communications with VENs
 *
 * @author Jeff LaJoie
 *
 */

 class VenStatus {

	
	//@Column(name = "EVENTID")
	 String eventID;
	
	//@Column(name = "VENID")
	 String venID;
	
	//@Column(name = "OPTSTATUS")
	 String optStatus;
	
	//@Column(name = "PROGRAM")
	 String program;
	
//	@Column(name = "TIME")
	 Date time;
	
	//@Column(name = "REQUESTID")
	 String requestID;
	
    static constraints = {
		requestID(nullable: true)		
		//select a program id from one of the available programs
    }	 
	public String toString(){
		return ("\nVEN ID: " + this.venID +
				"\nEvent ID: " + this.eventID +
				"\nProgram: " + this.program +
				"\nStatus: " + this.optStatus +
				"\nTime: " + this.time.toString());
	}
	
	
	/**
	 * Formats the date as readable format
	 *
	 * @return a String of a readable DateTime
	 */
	public String displayTime(){
		return(time.format("MM/dd/yyyy @ h:mm aa"));	}
	
}
	


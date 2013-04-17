package com.enernoc.open.oadr2.vtn


/**
 * A class to represent the VENStatus creation form
 * for Play's automatic binding of form fields to Objects
 * This object is used to track communications with VENs
 *
 * @author Jeff LaJoie
 *
 */

class VenStatus {

    static belongsTo = [event: Event, ven: Ven]
    //String eventID: retrieved from event

    //String venID: retrieved from ven

    //@Column(name = "OPTSTATUS")
    String optStatus

    //String program: retrieved from event.marketContext.programName 

    //	@Column(name = "TIME")
    Date time

    //@Column(name = "REQUESTID")
    String requestID

    static constraints = {
        requestID nullable: true
        time nullable: false
        //optStatus inList: ['optIn', 'optOut']
    }
    
    public String toString(){
        "VEN Status \n  VEN ID: $ven.venID\n  Event ID: $event.eventID\n  Program: $event.marketContext.programName" +
        "\n  Status: $optStatus\n  Time: $time"
    }

    /**
     * Formats the date as readable format
     *
     * @return a String of a readable DateTime
     */
    public String displayTime(){
        return time.format("dd/MM/yyyy HH:mm")
    }
}
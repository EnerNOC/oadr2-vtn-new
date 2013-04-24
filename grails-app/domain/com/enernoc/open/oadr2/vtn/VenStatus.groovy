package com.enernoc.open.oadr2.vtn


/**
 * Model class for VenStatus that persists unto the database
 * Each VenStatus belongs to an Event and VEN
 * 
 * @author Yang Xiang
 *
 */

class VenStatus {

    static belongsTo = [event: Event, ven: Ven]
    String optStatus
    Date time
    String requestID
    static constraints = {
        requestID nullable: true
        time nullable: false
        //TODO create enum object for optStatus
        //optStatus inList: ['optIn', 'optOut']
    }
    
    /**
     * Return the string representation of Venstatus
     * 
     * @return String
     */
    public String toString(){
        "VEN Status \n  VEN ID: $ven.venID\n  Event ID: $event.eventID\n  Program: $event.marketContext.name" +
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
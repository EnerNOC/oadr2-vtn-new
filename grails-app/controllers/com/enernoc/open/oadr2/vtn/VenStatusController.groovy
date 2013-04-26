package com.enernoc.open.oadr2.vtn

/**
 * VEN controller to manage all VEN objects created
 * and the display page for those objects
 *
 * @author Yang Xiang
 * 
 */
public class VenStatusController {

    static defaultAction = 'venStatuses'
    
    /**
     * Base method called to access the default page for the VENStatuses controller
     *  
     * @return a redirect to the venStatuses() call as to render the default page
     */
    def index() {
        return redirect(action: "venStatuses")
    }

    /**
     * Default method to render the page for the VENStatus table
     * 
     * @param eventID - The eventID specific to the Events to be displayed
     * @return venStatus/vens.gsp for VENStatus display, edit and deletion
     */
    def venStatuses() {
        //def eventList = Event.list()
        def event = Event.findWhere(eventID: params.eventID)
        def venStatuses = event.venStatuses
        def	eventList = Event.executeQuery("SELECT distinct e.eventID FROM Event e")
        [venStatusList: venStatuses, eventList: eventList, event: event.eventID]
    }

    /** Removes the venStatus with the given id from the database
     *
     * @param id - the database ID of the venStatus to be deleted
     * @return a redirect to venStatuses() without the deleted venStatus
     */
    def deleteStatus(){
        def venStatus = VenStatus.get(params.id)
        if ( ! venStatus ) {
            response.sendError 404, "No VEN Status for ID $params.id"
            return
        }
        def event = venStatus.event.eventID
        venStatus.delete()
        redirect(action:"venStatuses", params:[eventID: event])
    }
}

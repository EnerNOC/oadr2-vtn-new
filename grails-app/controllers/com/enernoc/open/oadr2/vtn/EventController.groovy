package com.enernoc.open.oadr2.vtn

import java.util.List;


/**
 * Events controller to manage all Event objects created
 * and the display page for those objects
 *
 * @author Yang Xiang
 * 
 */
class EventController {
    def messageSource
    def pushService
    def xmppService
    def eiEventService
    
    static defaultAction = 'events'
    
    /**
     * Base return for the default rendering of the Events page
     *
     * @return a redirect for the events()
     */
    def index() {
        redirect action:"events"
    }

    /**
     * Default method to render the page for the Event table
     *
     * @return the default render page for event display, edit and deletion
     */
    def events() {
        def event = Event.list( limit:20, sort:'startDate', order:'desc' )
        [eventList:event]
    }

    /**
     * 
     */
    def renderAJAXTable() {
        Date currentDate = new Date()
        GregorianCalendar calendar = new GregorianCalendar()
        calendar.time = currentDate
        def events = Event.list()
        render view: "eventsTable", model: [event: events]
    }
    
    /**
     * The default page render for new events to be created based on
     * the file at event/blankEvent.gsp
     *
     * @return the rendered page to create an event
     */
    def blankEvent() {
        // TODO 'distinct' should not be necessary (program name should be enforced unique)
        def model = [:]        
        model.programsList = Program.list()
        if( ! flash.chainModel?.event )
            model.event = new Event(startDate: new Date(), endDate:new Date())
        model
    }

    /**
     * Method called on the newEvent page when the Create this event button is submitted
     *
     * @param Event 
     * @return on success: redirect to the VENStatus page based on the EventID of the created Event
     * @return on fail: chains to blankEvent() with invalid event
     */
    def newEvent() {
        try {
            params.priority = params.priority.toLong()
        }
        catch ( IllegalArgumentException ) {
            params.priority = -1L
        }
        
        params.startDate = parseDttm( params.startDate, params.startTime )
        params.endDate = parseDttm( params.endDate, params.endTime )
        def program;
        if ( params.programID == "null" ) {
        } else {
            program = Program.get( params.programID.toLong() )
            if ( ! program ) {
                response.sendError 404, "No program for ID $params.programID"
                return
            }
        }
        params.remove 'programID'
        def event = new Event(params)
        event.program = program
        
        if ( event.validate() ) {
            def eiEvent = eiEventService.buildEiEvent(event)
//            pushService.pushNewEvent eiEvent, event.program.vens.collect { it }
            program.addToEvents event
            prepareVenStatus event
            program.save(flush: true)
            flash.message = "Success, your event has been created"
        }
        else {
            flash.message="Please fix the errors below"
            def errors = event.errors.allErrors.collect {
                log.debug "Event creation validation error: $it"
                messageSource.getMessage it, null
            }
            // TODO return invalid event, not a blank event
            return chain(action:"blankEvent", model:[errors: errors, event: event])
        }

        event.refresh()
//        redirect controller:"VenStatus", action:"venStatuses", params:[eventID: event.eventID
        redirect mapping: "eventSignal", params:[eventID: event.id]
    }

    /**
     * Parses the String date and String time into a Date object
     * 
     * @param String date
     * @param String time
     * @return Date
     */
    static parseDttm( String date, String time) {
        return Date.parse( "dd/MM/yyyy HH:mm", "$date $time")
    }
    
    /**
     * On the Event display page will take the EventStatus of the event and set it to CANCELLED
     *
     * @param id - The database ID of the Event to be cancelled
     * @return a redirect to the Events page, which should show the updated EventStatus of the cancelled event
     */
    def cancelEvent() {
        def event = Event.get(params.id)
    if ( ! event ) {
        response.sendError 404, "No event for ID $params.id"
        return
    }
        //Event event = Event.get(params.id)
        event.modificationNumber = event.modificationNumber + 1
        event.cancelled = true
        redirect action: "events"
    }

    /**
     * On the Event display page, will take the Event that is selected and remove it from the database
     *
     * @param id - database ID of the Event to be deleted
     * @return a redirect to the Events page which should show the list of Events without the deleted event
     */
    def deleteEvent() {
        def event = Event.get(params.id)
        if ( ! event ) {
            response.sendError 404, "No event for ID $params.id"
            return
        }
        def program = event.program
        event.program.removeFromEvents event
        event.delete()
        program.save()
        redirect actions: "events"
    }

    /**
     * On the Event display page, allows user to edit selected event
     *
     * @param id - The database ID of the Event to be edited
     * @return renders event/editEvent.gsp to allow user to update current event
     */
    def editEvent() {
        def model = [:]
        if ( ! flash.chainModel?.currentEvent ) {
            model.currentEvent = Event.get(params.id)
            if ( ! model.currentEvent ) {
                response.sendError 404, "No event for ID $params.id"
                return
            }
        }
        model
    }

    /**
     * Updates the event with a given id with the new parameters input from the user
     * 
     * @param Event, id
     * @return on success: redirects to events() to render updated event
     * @return on fail: chains to editEvent() with the invalid event
     */
    def updateEvent() {
        try {
            params.priority = params.priority.toLong()
        } 
        catch(IllegalArgumentException) {
            params.priority = -1L
        }
        params.startDate = parseDttm( params.startDate, params.startTime )
        params.endDate = parseDttm( params.endDate, params.endTime )
        
        def event = Event.get(params.id)
        event.properties = params
        if ( event.validate() ) {
            def eiEvent = eiEventService.buildEiEvent(event)
            event.modificationNumber +=1 // TODO this could be done with a save hook
            event.save()
            pushService.pushNewEvent eiEvent, event.program.vens.collect { it }
            flash.message="Success, your event has been updated"
        }
        else {
            flash.message="Please fix the errors below"
            def errors = event.errors.allErrors.collect {
                log.debug "Event update validation error: $it"
                messageSource.getMessage(it, null)
            }
            return chain(action:"editEvent", model:[errors: errors, currentEvent: event])
        }
        chain action:"events", model:[error: null]
    }

    /**
     * Prepares the VENs by creating a VENStatus object for each and setting the OptStatus to Pending 1
     *
     * @param vens - List of VENs to be traversed and will be used to construct a VENStatus object
     * @param event - Event containing the EventID which will be used for construction of a VENStatus object
     */
    protected void prepareVenStatus( Event event ) {
        
        event.program.vens.each { v ->
            // TODO create a method called VenStatus.create( ven, event ) that 
            // creates a new VenStatus object
            def venStatus = new VenStatus()
            venStatus.optStatus = "Pending request"
            venStatus.requestID = v.clientURI
            event.addToVenStatuses(venStatus)
            v.addToVenStatuses(venStatus)
            venStatus.time = new Date()
            if ( venStatus.validate() ) {
                v.save()
                log.debug "Created new VenStatus for Event: ${event.eventID}, VEN: ${v.venID}"
            }
            // TODO raise exception if VenStatus couldn't be created!
            else log.warn "Validation error for venStatus ${venStatus}"
        }
    }
}

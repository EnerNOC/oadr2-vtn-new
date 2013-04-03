package com.enernoc.oadr2.vtn

import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeConstants
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar

import com.enernoc.open.oadr2.model.CurrentValue
import com.enernoc.open.oadr2.model.DateTime
import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiEventSignal
import com.enernoc.open.oadr2.model.EventStatusEnumeratedType
import com.enernoc.open.oadr2.model.ObjectFactory
import com.enernoc.open.oadr2.model.PayloadFloat

/**
 * Events controller to manage all Event objects created
 * and the display page for those objects
 *
 * @author Jeff LaJoie
 */
class EventController {
    def messageSource
    def pushService
    def xmppService
    def eiEventService
    
    static ObjectFactory objectFactory = new ObjectFactory()
    static DatatypeFactory datatypeFactory
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance()
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error creating DatatypeFactory!",e)
        }
    }

    /**
     * Base return for the default rendering of the Events page
     *
     * @return a redirect for the routes.Events.events() route
     */
    def index() {
        redirect action:"events"
    }

    /**
     * The default page render for Events, inclusive of ordering of EiEvents
     * based on their start DateTime, in ascending order
     *
     * @return the rendered views.html.events page with a sorted list of EiEvents
     */
    def events() {
        def event = Event.list()
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
     * the file at views.html.newEvent
     *
     * @return the rendered page to create an event, with all fields vacant
     */
    def blankEvent() {
        // TODO 'distinct' should not be necessary (program name should be enforced unique)
        def programs = Program.executeQuery("SELECT distinct programName FROM Program")
        def date = new Date()
        def dateFormatted = g.formatDate(date:date, format:"MM/dd/yyyy")
        def timeFormatted = g.formatDate(date:date, format:"hh:mm aa")
        [ programList: programs, date: dateFormatted, time: timeFormatted]
    }

    /**
     * Method called on the newEvent page when the Create this event button is submitted
     *
     * @return a redirect to the VENStatus page based on the EventID of the created Event
     * @throws JAXBException
     */
    def newEvent() {
        try {
            params.intervals = params.intervals.toLong()
        }
        catch ( IllegalArgumentException ) {
            params.intervals = -1L
        }
        try {
            params.priority = params.priority.toLong()
        }
        catch ( IllegalArgumentException ) {
            params.priority = -1L
        }
        
        params.startDate = parseDttm( params.startDate, params.startTime )
        params.endDate = parseDttm( params.endDate, params.endTime )

        def event = new Event(params)
        // TODO should use ID, not program name
        def program = Program.find("from Program as p where p.programName=?", [params.programName])

//        if (program != null) program.addToEvent(event)
        event.marketContext = program

        if ( event.validate() ) {
            def eiEvent = eiEventService.buildEiEvent(event)
            event.status = eiEventService.updateStatus(eiEvent, (int)event.intervals).value
//            program.save()
            populateFromPush(event)
            def vens = Ven.findAll("from Ven as v where v.program.id=?", [event.marketContext.id])
            pushService.pushNewEvent(eiEvent, vens)
            flash.message="Success, your event has been created"
        }
        else {
            flash.message="Fail"
            def errors = event.errors.allErrors.collect {
                messageSource.getMessage(it, null)
            }
            // TODO return invalid event, not a blank event
            return chain(action:"blankEvent", model:[error: errors])
        }

        redirect controller:"VenStatus", action:"venStatuses", params:[eventID: event.eventID]

    }
    
    static parseDttm( String date, String time) {
        return Date.parse( "MM/dd/yyy hh:mm aa", "$date $time")
    }
    
    /**
     * On the Event display page will take the EventStatus of the event and set it to CANCELLED
     *
     * @param id - The database ID of the Event to be cancelled
     * @return a redirect to the Events page, which should show the updated EventStatus of the cancelled event
     */
    def cancelEvent() {
        def event = Event.get(params.id)
        //Event event = Event.get(params.id)
        event.modificationNumber = event.modificationNumber + 1
        event.status = "cancelled"
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
        event.delete()
        //flash("success", "Event has been deleted")
        redirect actions: "events"
    }

    def editEvent() {
        def currentEvent = Event.get(params.id)
        def programs = Program.executeQuery("SELECT distinct programName FROM Program")

        [currentEvent: currentEvent, programList: programs]
    }

    /**
     * Called when the Save this event button is pressed on the Edit event form
     *
     * @param id - database ID of the Event to be modified
     * @return a redirect to the Events page which should show the list of Events with the modified event
     *
     }*/
    /*Update Event modified to fit a groovier framework
     * Updates the event with a given id with the new parameters input from the user
     */
    def updateEvent() {
        try {
            params.intervals = params.intervals.toLong()
        } catch(IllegalArgumentException) {
            params.intervals = -1L
        }
        try {
            params.priority = params.priority.toLong()
        } catch(IllegalArgumentException) {
            params.priority = -1L
        }
        def event = Event.get(params.id)
        def programOld = Program.find("from Program where programName=?", [event.programName])
        def alteredEvent = new Event(params)
        def programNew = Program.find("from Program where programName=?", [alteredEvent.programName])
        alteredEvent.id = event.id

        //def testing = new EiEvent()
        if ( programNew != null ) {
            programOld.removeFromEvent(event)
            programOld.save()
            programNew.addToEvent(alteredEvent)
        }
        if ( alteredEvent.validate() ) {
            Long duration = alteredEvent.getMinutesDuration()
            def eiEvent = eiEventService.buildEiEvent(alteredEvent)
            alteredEvent.duration = alteredEvent.createXCalString(duration)
            alteredEvent.status = eiEventService.updateStatus(eiEvent, (int)alteredEvent.intervals).value
            event.delete()
            programNew.save()
            //populateFromPush(newEvent)
            //def vens = Ven.findAll("from Ven as v where v.programID=?", [event.programName])
            //pushService.pushNewEvent(event.eiEvent, vens)
            flash.message="Success, your event has been updated"
            //def vens = getVENs(event.eiEvent)

        }
        else {
            flash.message="Fail"
            def errors = alteredEvent.errors.allErrors.collect {
                messageSource.getMessage(it, null)
            }
            return chain(action:"editEvent", model:[error: errors])
        }
        chain action:"events", model:[error: null]
    }

    /**
     * Passes the VENs and event to the prepareVENs method
     *
     * @param event - event to be used for getVENs and prepareVENs
     */
    protected void populateFromPush( Event event ) {
        def customers = Ven.findAll(program: event.marketContext)
        prepareVENs customers, event
    }

    /**
     * Prepares the VENs by creating a VENStatus object for each and setting the OptStatus to Pending 1
     *
     * @param vens - List of VENs to be traversed and will be used to construct a VENStatus object
     * @param event - Event containing the EventID which will be used for construction of a VENStatus object
     */
    protected void prepareVENs ( List<Ven> vens, Event event ) {
        vens.each { v ->
            // TODO create a method called VenStatus.create( ven, event ) that 
            // creates a new VenStatus object
            def venStatus = new VenStatus()
            venStatus.optStatus = "Pending"
            venStatus.requestID = v.clientURI
            venStatus.eventID = event.eventID
            venStatus.program = v.programID
            venStatus.venID = v.venID
            venStatus.time = new Date()
            if ( venStatus.validate() ) {
                venStatus.save()
                log.debug "Created new VenStatus for Event: ${event.eventID}, VEN: ${v.venID}"
            }
            // TODO raise exception if VenStatus couldn't be created!
            else log.warn "Validation error for venStatus {}", venStatus
        }
    }
}

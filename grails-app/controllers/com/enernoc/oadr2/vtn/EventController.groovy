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

    static ObjectFactory objectFactory = new ObjectFactory();
    static DatatypeFactory datatypeFactory;
    static{
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
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
        XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar)
        xCalendar.timezone = DatatypeConstants.FIELD_UNDEFINED
        def events = Event.list()
        events.each { e ->
            def eiEvent = eiEventService.buildEvent(e)
            e.eiEvent.eventDescriptor.createdDateTime = new DateTime().withValue(xCalendar)
            if( e.eiEvent.getEventDescriptor().eventStatus != EventStatusEnumeratedType.CANCELLED )
                e.eiEvent.getEventDescriptor().eventStatus =
                        updateStatus(e, e.eiEvent.getEiEventSignals().getEiEventSignals().size())
            e.eiEvent.getEiEventSignals().getEiEventSignals() { eventSignal ->
                eventSignal.currentValue = new CurrentValue().withPayloadFloat(
                        new PayloadFloat().withValue(updateSignalPayload(e)))
            }
        }
        render view: "eventsTable", model: [event: events]
    }
    /**
     * The default page render for new events to be created based on
     * the file at views.html.newEvent
     *
     * @return the rendered page to create an event, with all fields vacant
     */
    def blankEvent() {
        //Event newForm = new Event();
        def programs = Program.executeQuery("SELECT distinct b.programName FROM Program b")
        // def programs = ["one", "two", "three"]
        // return ok(views.html.newEvent.render(form(Event.class).fill(newForm), newForm, makeProgramMap()));
        def date = new Date()
        def dateFormatted = g.formatDate(date:date, format:"MM-dd-yyyy")
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

        def event = new Event(params)
        def program = Program.find("from Program as p where p.programName=?", [event.programName])

        def errorMessage = [];
        //def testing = new EiEvent()
        if (program != null) program.addToEvent(event)

        if ( event.validate() ) {
            def eiEvent = eiEventService.buildEvent(event)
            Long duration = event.getMinutesDuration()
            event.duration = event.createXCalString(duration)
            event.status = updateStatus(eiEvent, (int)event.intervals).value
            program.save()
            populateFromPush(event);
            def vens = Ven.findAll("from Ven as v where v.programID=?", [event.programName]);
            pushService.pushNewEvent(eiEvent, vens)
            flash.message="Success, your event has been created"
            //def vens = getVENs(event.eiEvent)

        }
        else {
            flash.message="Fail"
            event.errors.allErrors.each {
                errorMessage << messageSource.getMessage(it, null)
            }
            return chain(action:"blankEvent", model:[error: errorMessage])
        }
        //chain(action:"events", model:[error: errorMessage])
        redirect controller:"VenStatus", action:"venStatuses", params:[eventID: event.eventID]

    }
    /**
     * On the Event display page will take the EventStatus of the event and set it to CANCELLED
     *
     * @param id - The database ID of the Event to be cancelled
     * @return a redirect to the Events page, which should show the updated EventStatus of the cancelled event
     */
    def cancelEvent() {
        def event = Event.get(params.id)
        //Event event = Event.get(params.id);
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
        //flash("success", "Event has been deleted");
        redirect actions: "events"
    }

    def editEvent() {
        def currentEvent = Event.get(params.id)
        def programs = Program.executeQuery("SELECT distinct b.programName FROM Program b")

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
        def programOld = Program.find("from Program as p where p.programName=?", [event.programName])
        def alteredEvent = new Event(params)
        def programNew = Program.find("from Program as p where p.programName=?", [alteredEvent.programName])
        alteredEvent.id = event.id
        def errorMessage = ""

        //def testing = new EiEvent()
        if ( programNew != null ) {
            programOld.removeFromEvent(event)
            programOld.save()
            programNew.addToEvent(alteredEvent)
        }
        if ( alteredEvent.validate() ) {
            Long duration = alteredEvent.getMinutesDuration()
            def eiEvent = eiEventService.buildEvent(alteredEvent)
            alteredEvent.duration = alteredEvent.createXCalString(duration)
            alteredEvent.status = updateStatus(eiEvent, (int)alteredEvent.intervals).value
            event.delete()
            programNew.save()
            //populateFromPush(newEvent);
            //def vens = Ven.findAll("from Ven as v where v.programID=?", [event.programName]);
            //pushService.pushNewEvent(event.eiEvent, vens)
            flash.message="Success, your event has been updated"
            //def vens = getVENs(event.eiEvent)

        }
        else {
            flash.message="Fail"
            alteredEvent.errors.allErrors.each {
                errorMessage << messageSource.getMessage(it, null)
            }
            return chain(action:"editEvent", model:[error: errorMessage])
        }
        chain action:"events", model:[error: errorMessage]
    }

    /**
     * Updates the EventStatus based on the current time and time of the event
     *
     * @param event - the event to have the EventStatus updated
     * @param intervals - the number of time intervals contained in the
     * @return the EventStatusEnumeratedType the EventStatus should be set to
     */
    protected EventStatusEnumeratedType updateStatus( EiEvent event, int intervals ) {

        Date currentDate = new Date()
        GregorianCalendar calendar = new GregorianCalendar()
        calendar.time = currentDate
        XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar)
        xCalendar.timezone = DatatypeConstants.FIELD_UNDEFINED

        DateTime currentTime = new DateTime().withValue(xCalendar)
        DateTime startTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().value.normalize());
        DateTime endTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().value.normalize());

        DateTime rampUpTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().value.normalize());

        rampUpTime.value.add(getDuration(event.getEiActivePeriod().getProperties().getXEiRampUp().getDuration().value));
        Duration d = getDuration(event, intervals);
        endTime.value.add(d);

        if ( currentTime.value.compare( startTime.value) == -1) {
            if( currentTime.value.compare(rampUpTime.value) == -1 )
                return EventStatusEnumeratedType.FAR
            else return EventStatusEnumeratedType.NEAR
        }
        else if ( currentTime.value.compare(startTime.value) > 0
        && currentTime.value.compare(endTime.value) == -1 )
            return EventStatusEnumeratedType.ACTIVE

        else if ( currentTime.value.compare(endTime.value) > 0)
            return EventStatusEnumeratedType.COMPLETED

        else return EventStatusEnumeratedType.NONE
    }


    /**
     * Updates the SignalPayloadFloat based on the EventStatus contained in the EiEvent
     *
     * @param event - Contains the EventStatus that determines the SignalPayload
     * @return the SignalPayload as a float to be set in the construction of the EiEvent
     */
    protected float updateSignalPayload( EiEvent event ) {
        if(event.getEventDescriptor().getEventStatus().equals(EventStatusEnumeratedType.ACTIVE))
            return 1;
        return 0;
    }

    /**
     * Passes the VENs and event to the prepareVENs method
     *
     * @param event - event to be used for getVENs and prepareVENs
     */
    protected void populateFromPush( Event event ) {
        def customers = Ven.findAll("from Ven as v where v.programID=?", [event.programName]);
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
            def venStatus = new VenStatus();
            venStatus.optStatus = "Pending 1"
            venStatus.requestID = v.clientURI
            venStatus.eventID = event.eventID
            venStatus.program = v.programID
            venStatus.venID = v.venID
            venStatus.time = new Date()
            if ( venStatus.validate() ) {
                venStatus.save()
                log.debug venStatus.time
            }
            else log.warn "Validation error for venStatus {}", venStatus
        }
    }

    /**
     * Converts an event to a duration based on the event and number of intervals
     *
     * @param event - Duration that needs to be converted from String to Duration
     * @param intervals - number of intervals to be serviced
     * @return Duration from the event multiplied by the number of intervals
     */
    static Duration getDuration( EiEvent event, int intervals ) {
        Duration duration = datatypeFactory.newDuration(
                Event.minutesFromXCal(event.eiActivePeriod.properties.duration.duration.value) * 60000)
        if ( intervals ) duration = duration.multiply(intervals)
        return intervals
    }

    static Duration getDuration( EiEvent event ) {
        return getDuration( event, 0 )
    }

    /**
     * Converts an event string of DurationValue to a Duration
     *
     * @param duration - Duration that needs to be converted from String to Duration
     * @return Duration from the event
     */
    static Duration getDuration( String duration ) {
        return datatypeFactory.newDuration( duration )
    }

    /**
     * Formats a duration to be acceptable by the schema validation
     *
     * @param duration - the duration to be modified with the .000 truncated
     * @return String with an acceptable duration value, minus the .000 precision
     */
    static String formatDuration( Duration duration ) {
        return duration.toString().replaceAll(".000", "")
    }
}


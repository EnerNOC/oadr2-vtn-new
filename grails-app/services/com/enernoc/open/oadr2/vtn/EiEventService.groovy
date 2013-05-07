package com.enernoc.open.oadr2.vtn

import javax.xml.bind.JAXBElement
import javax.xml.datatype.DatatypeConstants
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar

import com.enernoc.open.oadr2.model.CurrentValue
import com.enernoc.open.oadr2.model.DateTime
import com.enernoc.open.oadr2.model.Dtstart
import com.enernoc.open.oadr2.model.DurationPropType
import com.enernoc.open.oadr2.model.DurationValue
import com.enernoc.open.oadr2.model.EiActivePeriod
import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiEventSignal
import com.enernoc.open.oadr2.model.EiEventSignals
import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.EiTarget
import com.enernoc.open.oadr2.model.EventStatusEnumeratedType
import com.enernoc.open.oadr2.model.EventDescriptor
import com.enernoc.open.oadr2.model.Interval
import com.enernoc.open.oadr2.model.Intervals
import com.enernoc.open.oadr2.model.MarketContext
import com.enernoc.open.oadr2.model.OadrCreatedEvent
import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrRequestEvent
import com.enernoc.open.oadr2.model.OadrResponse
import com.enernoc.open.oadr2.model.ObjectFactory
import com.enernoc.open.oadr2.model.PayloadFloat
import com.enernoc.open.oadr2.model.Properties
import com.enernoc.open.oadr2.model.ResponseCode
import com.enernoc.open.oadr2.model.ResponseRequiredType
import com.enernoc.open.oadr2.model.SignalPayload
import com.enernoc.open.oadr2.model.SignalTypeEnumeratedType
import com.enernoc.open.oadr2.model.Uid
import com.enernoc.open.oadr2.model.EventDescriptor.EiMarketContext
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent
import com.enernoc.open.oadr2.model.Properties.Tolerance
import com.enernoc.open.oadr2.model.Properties.Tolerance.Tolerate

/**
 * EiEventService handles all persistence and object creation of payloads
 * 
 * @author Yang Xiang
 *
 */
public class EiEventService {

    static transactional = true

    String vtnID // injected property value
    def df = DatatypeFactory.newInstance()

    /**
     * Determines which method to call based on the Object it is passed
     * 
     * @param o - Object to be used to create the responding payload Object 
     * @return an Object to be marshalled to XML and sent over HTTP or XMPP
     */
    public Object handleOadrPayload( Object o ) {
        if ( o == null )
            throw new RuntimeException("Payload may not be null")

        if ( o instanceof OadrRequestEvent ) {
            log.debug "oadrRequestEvent"
            return handleOadrRequest( (OadrRequestEvent)o )
        }

        else if ( o instanceof OadrCreatedEvent ) {
            log.debug "oadrCreatedEvent"
            return handleOadrCreated( (OadrCreatedEvent)o )
        }
        else if( o instanceof OadrResponse ) {
            log.debug "OadrResponse"
            //TODO implement handleOadrResponse
            //handleOadrResponse( (OadrResponse)o )
            return null
        }
        else {
            log.error "Unknown type: ${o?.class}"
            throw new RuntimeException("Payload was unknown type: ${o?.class}")
        }
    }

    public OadrResponse handleOadrCreated( OadrCreatedEvent oadrCreatedEvent ) {
        def responseCode = verifyOadrCreated( oadrCreatedEvent )[0]
        def desc = verifyOadrCreated( oadrCreatedEvent )[1]
        println("responsecode is: " + responseCode)
        println("desc is: "+ desc)
        if ( isSuccessful( oadrCreatedEvent ) )
            persistFromCreatedEvent oadrCreatedEvent

        else
            log.warn "Incoming oadrCreatedEvent contained a non-200 response: $oadrCreatedEvent"

        return new OadrResponse()
            .withEiResponse(new EiResponse()
                .withRequestID(UUID.randomUUID().toString())
                .withResponseCode(new ResponseCode(responseCode))
                .withResponseDescription(desc))
    }

    protected boolean isSuccessful( OadrCreatedEvent payload ) {
        payload.eiCreatedEvent.eiResponse.responseCode.value == '200'
    }

    /**
     * Takes an OadrCreatedEvent and verifies that the Response exists and there are no errors in the payload
     * 
     * @param oadrCreatedEvent - the OadrCreatedEvent to be checked for errors
     * @return a response code as a string
     */
    def verifyOadrCreated( OadrCreatedEvent oadrCreatedEvent ) {
        def venID = oadrCreatedEvent.eiCreatedEvent.venID
        def response = "200"
        def desc = "OK"
        oadrCreatedEvent.eiCreatedEvent.eventResponses?.eventResponses?.each { evtResponse ->
            if ( response != "200" ) return // skip remaining elements if there's already an error.

            String eventId = evtResponse.qualifiedEventID.eventID
            long modificationNumber = evtResponse.qualifiedEventID.modificationNumber

            def event = Event.findWhere( eventID: eventId, venID: venID )
            def venStatuses = VenStatus.where{ ven.venID == venID }.findAll()

            if ( ! event ) {
                response = "404"
                desc = "Event not found"
            }
            if ( ! venStatuses ) {
                response = "409"
                desc = "Invalid VEN ID"
            }
        }
        return [response, desc]
    }

    /**
     * Takes an OadrRequestEvent and persists the data to the tables
     * While formatting an ordering the Pending/Active events for the OadrDistributeEvent
     * 
     * @param oadrRequestEvent - Request incoming from the VEN
     * @return an OadrDistributeEvent containing all payload information
     */
    public OadrDistributeEvent handleOadrRequest(OadrRequestEvent oadrRequestEvent){

        EiResponse eiResponse = new EiResponse()
                .withResponseCode( new ResponseCode("200") )
        if ( ! oadrRequestEvent.eiRequestEvent.requestID )
            eiResponse.requestID = oadrRequestEvent.eiRequestEvent.requestID
        else
            eiResponse.requestID = UUID.randomUUID().toString()

        OadrDistributeEvent oadrDistributeEvent = new OadrDistributeEvent()
            .withEiResponse(eiResponse)
            .withRequestID(UUID.randomUUID().toString())
            .withVtnID(this.vtnID)

        // FIXME validate VEN ID against HTTP credentials
        def limit = oadrRequestEvent.eiRequestEvent.replyLimit.intValue()
        // TODO order according to date, priority & status
        def events = Event.executeQuery("select e from Event e, Ven v where  v.venID = :vID and e.program in elements(v.programs) and e.endDate > :d",
            [vID: oadrRequestEvent.eiRequestEvent.venID , d: new Date()],[max : limit])
        oadrDistributeEvent.oadrEvents = events.collect { e ->
            new OadrEvent()
                    .withEiEvent(buildEiEvent(e))
                    .withOadrResponseRequired(ResponseRequiredType.ALWAYS)
        }
        persistFromRequestEvent oadrRequestEvent, events

        return oadrDistributeEvent
    }

    /**
     * Persists the information from an OadrRequestEvent into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    protected void persistFromRequestEvent( OadrRequestEvent requestEvent, List<Event> events ) {
        def ven = Ven.findWhere( venID: requestEvent.eiRequestEvent.venID )
        events.each { event ->
            def venStatus = VenStatus.findWhere(
                    ven: ven, event: event )

            if ( ! venStatus ) {
                venStatus = new VenStatus()
                ven.addToVenStatuses(venStatus)
                event.addToVenStatuses(venStatus)
                venStatus.optStatus = "Pending response"
            }

            venStatus.time = new Date()
            ven.save()
            event.save()
        }
    }

    /**
     * Persists the information from an OadrCreatedEvent into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    protected void persistFromCreatedEvent( OadrCreatedEvent createdEvent ) {
        createdEvent.eiCreatedEvent.eventResponses?.eventResponses?.each { response ->
            String eventId = response.qualifiedEventID.eventID
            long modificationNumber = response.qualifiedEventID.modificationNumber
        }
        // FIXME query for status per event in the eventResponses element,
        // set venStatus for each event
        def venStatuses = VenStatus.where{ ven.venID == createdEvent.eiCreatedEvent.venID }.findAll()

        venStatuses.each { status ->
            if ( createdEvent.eiCreatedEvent.eventResponses ) {

                createdEvent.eiCreatedEvent.eventResponses.eventResponses.each { eventResponse ->
                    def optType = eventResponse.optType.toString()
                    log.debug "now setting the new optType: $optType"
                    status.optStatus = optType
                }
            }
            status.time = new Date()
            status.save()
        }
    }

    /**
     * Persists the information from an OadrResponse into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    public void handleFromOadrResponse( OadrResponse response ) {
        def status = VenStatus.findByRequestID( response.eiResponse.requestID )
        if ( status ) {
            status.time = new Date()
            status.optStatus = response.eiResponse.optType
            status.save()
        }
        else log.warn "No status found for response $response"
    }

    /**
     * Gets the DurationValue from an EiEvent as a java.util.Duration
     * 
     * @param event - EiEvent to have the DurationValue pulled from
     * @return a Duration based on the EiEvent DurationValue
     */
    public Duration getDuration( EiEvent event ) {
        return this.df.newDuration(
            event.eiActivePeriod.properties.duration.duration.value)
    }

    /**
     * Takes the Event form pulled from the scala.html and crafts
     *
     * @param event - the wrapper from the scala.html form for EiEvent
     * @return the EiEvent built from the Event wrapper
     */
    public EiEvent buildEiEvent( Event event ) {
        Date currentDate = new Date()
        GregorianCalendar calendar = new GregorianCalendar()
        def objectFactory = new ObjectFactory()
        calendar.setTime(currentDate)
        XMLGregorianCalendar xCalendar = df.newXMLGregorianCalendar(calendar)
        xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED) // FIXME

        JAXBElement<SignalPayload> signalPayload = objectFactory.createSignalPayload(
                new SignalPayload(new PayloadFloat(1)))

        def intervalList = []
        EiEvent newEvent = event.toEiEvent()

        /*event.intervals.each { evt, i ->
            intervalList.add(new Interval()
                .withDuration(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(formatDuration(getDuration(newEvent)))))
                    .withUid(new Uid().withText("" + i))
                    .withStreamPayloadBase(signalPayload))
        }*/
        event.intervals.eachWithIndex { evt, i->
            intervalList.add(new Interval()
                    .withDuration(new DurationPropType()
                    .withDuration(new DurationValue()
                    .withValue(formatDuration(getDuration(newEvent)))))
                    .withUid(new Uid()
                    .withText("" + i))
                    .withStreamPayloadBase(signalPayload))
        }
        Intervals intervals = new Intervals(intervalList)
        newEvent.withEiActivePeriod(new EiActivePeriod()
                    .withProperties(new Properties()
                        .withDtstart(new Dtstart()
                            .withDateTime(new DateTime()
                                .withValue(newEvent.eiActivePeriod.properties.dtstart.dateTime.value.normalize())))
                        .withDuration(new DurationPropType()
                            .withDuration(new DurationValue()
                                .withValue(formatDuration(getDuration(newEvent, (int)event.intervals)))))
                        .withTolerance(new Tolerance()
                            .withTolerate(new Tolerate()
                                .withStartafter(new DurationValue()
                                    // FIXME proper duration
                                    .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S")))))))
                        .withXEiNotification(new DurationPropType()
                            .withDuration(new DurationValue()
                                // FIXME proper duration
                                .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                        .withXEiRampUp(new DurationPropType()
                            .withDuration(new DurationValue()
                                // FIXME proper duration
                                .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                        .withXEiRecovery(new DurationPropType()
                            .withDuration(new DurationValue()
                                // FIXME proper duration
                                .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))))
                    .withEiTarget(new EiTarget())
                    .withEventDescriptor(new EventDescriptor()
                        .withCreatedDateTime(new DateTime().withValue(xCalendar))
                        .withEiMarketContext(new EiMarketContext()
                            .withMarketContext(new MarketContext()
                                .withValue(event.program.marketContext)))
                        .withEventID(event.eventID)
                        .withEventStatus(updateStatus(newEvent, (int)event.intervals))
                        .withModificationNumber(event.modificationNumber) //changed to the set modification number
                        .withPriority(event.priority)
                        .withTestEvent("False")
                        .withVtnComment("No VTN Comment"))
                    .withEiEventSignals(new EiEventSignals()
                        .withEiEventSignals(new EiEventSignal()
                            .withCurrentValue(new CurrentValue()
                                .withPayloadFloat(new PayloadFloat()
                                    .withValue(updateSignalPayload(newEvent)))) //TODO Not sure what this value is supposed to be, must be 0 when NEAR
                            .withIntervals(new Intervals()
                                .withIntervals(intervalList))
                            .withSignalID("TH_SIGNAL_ID")
                            .withSignalName("simple")
                    .withSignalType(SignalTypeEnumeratedType.LEVEL)))
        return newEvent
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
    
    /**
     * Updates the SignalPayloadFloat based on the EventStatus contained in the EiEvent
     *
     * @param event - Contains the EventStatus that determines the SignalPayload
     * @return the SignalPayload as a float to be set in the construction of the EiEvent
     */
    protected float updateSignalPayload( EiEvent event ) {
        if(event.eventDescriptor.eventStatus.equals(EventStatusEnumeratedType.ACTIVE)) {
            return 1
        }
        return 0
    }
    
    /**
     * Converts an event to a duration based on the event and number of intervals
     *
     * @param event - Duration that needs to be converted from String to Duration
     * @param intervals - number of intervals to be serviced
     * @return Duration from the event multiplied by the number of intervals
     */
    protected Duration getDuration( EiEvent event, int intervals ) {
        Duration duration = df.newDuration(event.eiActivePeriod.properties.duration.duration.value)
        if ( intervals ) duration = duration.multiply(intervals)
        return duration
    }


    /**
     * Converts an event string of DurationValue to a Duration
     *
     * @param duration - Duration that needs to be converted from String to Duration
     * @return Duration from the event
     */
    protected Duration getDuration( String duration ) {
        return df.newDuration( duration )
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
        XMLGregorianCalendar xCalendar = df.newXMLGregorianCalendar(calendar)
        xCalendar.timezone = 0 // GMT

        DateTime currentTime = new DateTime(xCalendar)
        def startDttm = event.eiActivePeriod.properties.dtstart.dateTime.value.normalize()
        DateTime startTime = new DateTime(startDttm)
        DateTime endTime = new DateTime(startDttm) // FIXME

        DateTime rampUpTime = new DateTime().withValue(startDttm)

        rampUpTime.value.add(getDuration(event.eiActivePeriod.properties.XEiRampUp.duration.value))
        Duration d = getDuration(event, intervals)
        endTime.value.add(d)

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

    
}
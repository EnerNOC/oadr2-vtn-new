package com.enernoc.open.oadr2.vtn

import javax.xml.bind.JAXBElement
import javax.xml.datatype.DatatypeConstants
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar

import org.springframework.aop.aspectj.RuntimeTestWalker.ThisInstanceOfResidueTestVisitor;

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
import com.sun.media.jai.opimage.LogCRIF;

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
            handleOadrResponse( (OadrResponse)o )
            return null
        }
        else {
            log.error "Unknown type: ${o?.class}"
            throw new RuntimeException("Payload was unknown type: ${o?.class}")
        }
    }

    public OadrResponse handleOadrCreated( OadrCreatedEvent oadrCreatedEvent ) {
        def responseCode = verifyOadrCreated( oadrCreatedEvent )[0]
        def description = verifyOadrCreated( oadrCreatedEvent )[1]
        log.debug responseCode
        log.debug description
        if ( isSuccessful( oadrCreatedEvent ) )
            persistFromCreatedEvent oadrCreatedEvent

        else
            log.warn "Incoming oadrCreatedEvent contained a non-200 response: $oadrCreatedEvent"

        return new OadrResponse()
            .withEiResponse(new EiResponse()
                .withRequestID(UUID.randomUUID().toString())
                .withResponseCode(new ResponseCode(responseCode))
                .withResponseDescription(description))
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
        def description = "OK"
        try {
            oadrCreatedEvent.eiCreatedEvent.eventResponses.eventResponses.each { evtResponse ->
                if ( evtResponse.responseCode.value != "200" ) {
                    response = evtResponse.responseCode.value
                    description = "eventResponse contained a non-200 response: $evtResponse"
                    return [response, description]// skip remaining elements if there's already an error.
                }
                String eventId = evtResponse.qualifiedEventID.eventID
                long modificationNumber = evtResponse.qualifiedEventID.modificationNumber

                def event = Event.findWhere( eventID: eventId)
                def venStatuses = VenStatus.where{ ven.venID == venID }.findAll()

                if ( ! event ) {
                    response = "404"
                    description = "Event not found"
                }
                if ( ! venStatuses ) {
                    response = "409"
                    description = "Invalid VEN ID"
                }
            }
        } catch (e) {
            log.warn "oadrCreatedEvent does not have eventResponses"
        }
        
        return [response, description]
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
        if ( oadrRequestEvent.eiRequestEvent.requestID )
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
    public void handleOadrResponse( OadrResponse response ) {
        //def status = VenStatus.findByRequestID( response.eiResponse.requestID ) TODO implement a new VenTranaction to handle responses
        if ( status ) {
            status.time = new Date()
            status.optStatus = "Pending 2"
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
        GregorianCalendar now = new GregorianCalendar()
        def objectFactory = new ObjectFactory()
        now.setTime(new Date())

        EiEvent eiEvent = event.toEiEvent()
        eiEvent.withEiActivePeriod(new EiActivePeriod()
            .withProperties(new Properties()
                .withDtstart(new Dtstart()
                    .withDateTime(new DateTime()
                        .withValue(event.xmlStart) ) )
                .withDuration(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.duration.toString() ) ) )
                .withTolerance(new Tolerance()
                    .withTolerate(new Tolerate()
                        .withStartafter(new DurationValue()
                            .withValue(event.toleranceDuration.toString() ) ) ) )
                .withXEiNotification(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.notificationDuration.toString() ) ) )
                .withXEiRampUp(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.rampUpDuration.toString() ) ) )
                .withXEiRecovery(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue( event.recoveryDuration.toString() ) ) ) 
                ) 
            )
//                .withEiTarget(new EiTarget()) // TODO
        .withEventDescriptor(new EventDescriptor()
            .withCreatedDateTime(new DateTime()
                .withValue( df.newXMLGregorianCalendar(now) ) )
            .withEiMarketContext(new EiMarketContext()
                .withMarketContext(new MarketContext()
                    .withValue( event.program.marketContext ) ) )
            .withEventID( event.eventID )
            .withEventStatus( this.getCurrentStatus( event ) )
            .withModificationNumber( event.modificationNumber )
            .withPriority( event.priority )
            .withTestEvent("False")
            .withVtnComment(""))
        .withEiEventSignals( new EiEventSignals()
            .withEiEventSignals(
                event.signals.collect { signal ->
                    def eiSignal = new EiEventSignal()
                        .withIntervals( new Intervals( signal.intervals.collect {
                            this.buildInterval it, objectFactory
                        } ) )
                        .withSignalID( signal.signalID )
                        .withSignalName( signal.name )
                        .withSignalType( signal.type.xmlType )

                    def currentInterval = signal.currentInterval
                    if ( currentInterval )
                        eiSignal.currentValue = new CurrentValue( new PayloadFloat( signal.currentInterval ) )
                    return eiSignal
                }
            ))
        return eiEvent
    }
    
    protected Interval buildInterval( EventInterval interval, ObjectFactory objectFactory ) {
        return new Interval()
            .withDuration( new DurationPropType()
                .withDuration( new DurationValue()
                    .withValue( interval.duration.toString() )))
            .withUid( new Uid( interval.uid.toString() ) )
            .withStreamPayloadBase(
                objectFactory.createSignalPayload(
                    new SignalPayload(new PayloadFloat( interval.level ) ) ) )
    }
    
    /**
     * Returns the correct event status (FAR, NEAR, ACTIVE) based on the current 
     * time and time of the event
     *
     * @param event - the Event domain object
     * @return the EventStatusEnumeratedType
     */
    protected EventStatusEnumeratedType getCurrentStatus( Event event ) {
        if ( event.cancelled )
            return EventStatusEnumeratedType.CANCELLED
        
        Date now = new Date()
        if ( now < event.startDate ) {
            def rampUpStart = new Date(now.time - event.rampUp)
            if ( now < rampUpStart ) 
                return EventStatusEnumeratedType.FAR
            return EventStatusEnumeratedType.NEAR
        }
        
        if ( now < event.endDate )
            return EventStatusEnumeratedType.ACTIVE
        
        // else event is ended
        return EventStatusEnumeratedType.COMPLETED
    }
}
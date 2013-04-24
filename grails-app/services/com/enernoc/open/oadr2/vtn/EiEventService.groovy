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
        def responseCode, desc = verifyOadrCreated( oadrCreatedEvent )

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
        oadrCreatedEvent.eiCreatedEvent.eiResponse.responseCode.value == '200'
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
            if ( ! ven ) {
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
        def ven = Ven.findWhere( venID: oadrRequestEvent.eiRequestEvent.venID )

        def limit = oadrRequestEvent.eiRequestEvent.replyLimit.intValue()
        // TODO order according to date, priority & status
        def events = Event.findAll(max : limit) { 
            marketContext.id == ven.program.id
            endDate > new Date() // include only events that have not ended
        }

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
                ven.addToVenStatus(venStatus)
                event.addToVenStatus(venStatus)
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
                    .withValue( event.marketContext.programURI ) ) )
            .withEventID( event.eventID )
            .withEventStatus( this.getCurrentStatus( event ) )
            .withModificationNumber( event.modificationNumber )
            .withPriority( event.priority )
            .withTestEvent("False")
            .withVtnComment(""))
        .withEiEventSignals( new EiEventSignals()
            .withEiEventSignals(
                event.signals.collect { signal ->
                    new EiEventSignal()
                        .withCurrentValue(new CurrentValue()
                            .withPayloadFloat(new PayloadFloat()
                                .withValue( signal.getCurrentValue() ) ) )
                        .withIntervals( signal.intervals.collect {
                            this.buildInterval it
                        })
                        .withSignalID( signal.signalID )
                        .withSignalName( signal.name )
                        .withSignalType( this.getEiSignalType(signal.type) )
            }))
        return eiEvent
    }
    
    protected Interval buildInterval( EventInterval interval ) {
        return new Interval()
            .withDuration( new DurationPropType()
                .withDuration( new DurationValue()
                    .withValue( interval.duration.toString() )))
            .withUid( interval.uid.toString() )
            .withText( interval.uid )
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
    
    protected SignalTypeEnumeratedType getEiSignalType( EventInterval interval ) {
        switch ( interval.signalType ) {
            case SignalType.PRICE_RELATIVE:
                return SignalTypeEnumeratedType.PRICE_RELATIVE
            default:
                return SignalTypeEnumeratedType.LEVEL
        }
    }
}
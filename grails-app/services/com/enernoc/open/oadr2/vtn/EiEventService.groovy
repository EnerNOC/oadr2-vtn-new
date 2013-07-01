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

        if ( ! this.vtnID )
            log.warn "+++++++++++++++ VTN ID should not be null!"
            
        if ( o instanceof OadrRequestEvent ) {
            log.debug "oadrRequestEvent"
            return handleOadrRequest( (OadrRequestEvent)o )
        }

        else if ( o instanceof OadrCreatedEvent ) {
            log.debug "oadrCreatedEvent"
            return handleOadrCreated( (OadrCreatedEvent)o )
        }
      /*  else if( o instanceof OadrResponse ) {
            log.debug "OadrResponse"
            handleOadrResponse( (OadrResponse)o )
            return null
        }*/
        else {
            log.error "Unknown type: ${o?.class}"
            throw new RuntimeException("Payload was unknown type: ${o?.class}")
        }
    }

    public OadrResponse handleOadrCreated( OadrCreatedEvent oadrCreatedEvent ) {
        
        def (responseCode, description) = [200,"OK"]
        if ( isSuccessful( oadrCreatedEvent ) )
            (responseCode, description) = processEventResponses( oadrCreatedEvent )

        else
            log.warn "Incoming oadrCreatedEvent contained a non-200 response: $oadrCreatedEvent"
        
        def oadrResponse = new OadrResponse()
            .withEiResponse(new EiResponse()
                .withRequestID(oadrCreatedEvent.eiCreatedEvent.eiResponse.requestID)
                .withResponseCode(new ResponseCode(responseCode))
                .withResponseDescription(description))
            
        return oadrResponse
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
    def processEventResponses( OadrCreatedEvent oadrCreatedEvent ) {
        def response = "200"
        def description = "OK"
        
        def venID = oadrCreatedEvent.eiCreatedEvent.venID
        def eventResponses = oadrCreatedEvent.eiCreatedEvent.eventResponses?.eventResponses
        
        if ( ! eventResponses  ) {
            log.warn "oadrCreatedEvent does not have eventResponses"
            // TODO should this result in an error?
            return [response, description]
        }
        
        def errors = eventResponses.collect { evtResponse ->
            String eventId = evtResponse.qualifiedEventID.eventID
            long modificationNumber = evtResponse.qualifiedEventID.modificationNumber
            def optType = evtResponse.optType.value()
            def eventResponseCode = evtResponse.responseCode.value
            
            log.debug "Event response $eventResponseCode from VEN: $venID, " +
                      "eventID: ${eventId}/${modificationNumber}, opt: $optType"
            
            if ( eventResponseCode != "200" ) {
                // TODO if we get a non-200 response, do we process the opt??
                log.warn "++++ Not processing non-200 event response!"
                return
            }
            def venTxLog = VenTransactionLog.findWhere(UID: evtResponse.requestID)
            if ( venTxLog ) {
                venLog.responseDate = new Date()
                venLog.save()
            }
            else log.warn "Unknown request ID: $evtResponse.requestID"
            

            def event = Event.findWhere( eventID: eventId )
            if ( ! event ) {
                log.warn "Event not found!"
                return ["404", "Event not found"]
            }

            def venStatus = VenStatus.where{ ven.venID == venID; event == event }.find()

            if ( ! venStatus ) {
                log.warn "VEN status not found!" 
                return ["409", "Invalid VEN ID"]
            }
            
            switch (optType) {
                case("optIn") :
                    venStatus.optStatus = StatusCode.OPT_IN
                    break
                case("optOut") :
                    venStatus.optStatus = StatusCode.OPT_OUT
                    break
                default :
                    log.error "Invalid opt Type! $optType"
                    return [409,"Invalid opt type!"]
            }
            venStatus.time = new Date()
            venStatus.save()
        }
        
        errors.findAll() // filter out null
        
        if ( errors ) {
            (response,description) = errors[0]
            log.warn "Returning error response: $response, $description"
        }
        return [response,description]
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
                
        eiResponse.requestID = oadrRequestEvent.eiRequestEvent.requestID ?: 
            UUID.randomUUID().toString()
            
        OadrDistributeEvent oadrDistributeEvent = new OadrDistributeEvent()
            .withEiResponse(eiResponse)
            .withRequestID(UUID.randomUUID().toString())
            .withVtnID(this.vtnID)

        def ven = Ven.findWhere( venID: oadrRequestEvent.eiRequestEvent.venID )
        if ( ! ven ) {
            log.warn "Unknown VEN ID! ${oadrRequestEvent.eiRequestEvent.venID}"
            eiResponse.responseCode.value = "404"
            return oadrDistributeEvent
        }

        // FIXME validate VEN ID against HTTP credentials
        def limit = oadrRequestEvent.eiRequestEvent.replyLimit?.intValue() ?: 100
        // TODO order according to date, priority & status
        def events = Event.executeQuery(
            "select e from Event e, Ven v where v.venID = :vID and e.program in elements(v.programs) and e.endDate > :d",
            [vID: ven.venID , d: new Date()],[max : limit]).sort()
        oadrDistributeEvent.oadrEvents = events.collect { e ->
            new OadrEvent()
                    .withEiEvent(buildEiEvent(e))
                    .withOadrResponseRequired(ResponseRequiredType.ALWAYS)
        }
        
        persistFromRequestEvent ven, events
        
        def venLog = new VenTransactionLog()
        venLog.venID = ven.venID
        venLog.sentDate = new Date()
        venLog.UID = eiResponse.requestID
        log.debug eiResponse.requestID
        if ( venLog.validate() )
            venLog.save()
        else log.warn "Couldn't validate VEN TXN!"
        
        return oadrDistributeEvent
    }

    /**
     * Persists the information from an OadrRequestEvent into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    protected void persistFromRequestEvent( Ven ven, List<Event> events ) {
        events.each { event ->
            def venStatus = VenStatus.findWhere(
                    ven: ven, event: event )
            if ( ! venStatus ) {
                venStatus = new VenStatus()
                ven.addToVenStatuses venStatus
                event.addToVenStatuses venStatus
            }
            venStatus.optStatus = StatusCode.DISTRIBUTE_SENT
            venStatus.time = new Date()
            
            ven.save failOnError: true
            event.save failOnError: true
        }        
    }

    /**
     * Persists the information from an OadrResponse into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    public void handleOadrResponse( OadrResponse re, String uri ) {
        def ven = Program.list()
        if ( ven ) {
            ven.each { status ->
                status.time = new Date()
                status.optStatus = StatusCode.DISTRIBUTE_SENT
                status.save()
            }
            log.debug "after loop"
        }
        else log.warn "Ven with clientURI $uri not found"
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
        now.setTime new Date()
        
//        log.debug "--- $event"

        EiEvent eiEvent = event.toEiEvent()
        eiEvent.withEiActivePeriod(new EiActivePeriod()
            .withProperties(new Properties()
                .withDtstart(new Dtstart()
                    .withDateTime(new DateTime()
                        .withValue(event.xmlStart) ) )
                .withDuration(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.duration ) ) )
                .withTolerance(new Tolerance()
                    .withTolerate(new Tolerate()
                        .withStartafter(new DurationValue()
                            .withValue(event.toleranceDuration ) ) ) )
                .withXEiNotification(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.notificationDuration ) ) )
                .withXEiRampUp(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue(event.rampUpDuration ) ) )
                .withXEiRecovery(new DurationPropType()
                    .withDuration(new DurationValue()
                        .withValue( event.recoveryDuration ) ) ) 
                ) 
            )
            .withEiTarget(new EiTarget()) // TODO
        .withEventDescriptor(new EventDescriptor()
            .withCreatedDateTime(new DateTime()
                .withValue( df.newXMLGregorianCalendar(now).normalize() ) )
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

                    def currentValue = signal.currentInterval?.level ?: 0
                    eiSignal.currentValue = new CurrentValue( new PayloadFloat( currentValue ) )
                    
                    return eiSignal
                }
            ))
        
        return eiEvent
    }
    
    protected Interval buildInterval( EventInterval interval, ObjectFactory objectFactory ) {
        return new Interval()
            .withDuration( new DurationPropType()
                .withDuration( new DurationValue()
                    .withValue( interval.duration )))
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
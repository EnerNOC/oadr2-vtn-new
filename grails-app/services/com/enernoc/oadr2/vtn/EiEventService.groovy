package com.enernoc.oadr2.vtn

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
import com.enernoc.open.oadr2.model.EventDescriptor
import com.enernoc.open.oadr2.model.Interval
import com.enernoc.open.oadr2.model.Intervals
import com.enernoc.open.oadr2.model.MarketContext
import com.enernoc.open.oadr2.model.OadrCreatedEvent
import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrRequestEvent
import com.enernoc.open.oadr2.model.OadrResponse
import com.enernoc.open.oadr2.model.PayloadFloat
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
 * @author Jeff LaJoie
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
            return null;
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
		venID = oadrCreatedEvent.eiCreatedEvent.venID
		response = "200"
		desc = "OK"
		oadrCreatedEvent.eiCreatedEvent.eventResponses?.eventResponses?.each { response ->
			if ( response != "200" ) return // skip remaining elements if there's already an error.
			
			String eventId = response.qualifiedEventID.eventID
			long modificationNumber = response.qualifiedEventID.modificationNumber
			
			def event = Event.findWhere( eventID: eventId, venID: venID )
			def ven = VenStatus.findWhere( venID: venID )
			
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
		// TODO filter by marketContext if given
		def events = Event.findAll(max : limit) { programName == ven.programID }
		         
        oadrDistributeEvent.oadrEvents = events.collect { e ->
            new OadrEvent()
                .withEiEvent(EventController.buildEvent(e))
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
		def venID = requestEvent.eiRequestEvent.venID
		def ven = Ven.findWhere( venID: venID )
		events.each { event ->
	        def venStatus = VenStatus.findWhere( 
				venID: venID, eventID: event )
			
			if ( ! venStatus ) {
				venStatus = ew VenStatus()
				venStatus.venID = venID
                venStatus.program = ven.programID
                venStatus.optStatus = "Awaiting response"
			}
			
	        venStatus.time = new Date()
			venStatus.save()
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
        def venStatuses = VenStatus.findAllWhere( venID: createdEvent.eiCreatedEvent.venID )

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
        return this.df.newDuration(Event.minutesFromXCal(
			event.eiActivePeriod.properties.duration.duration.value) * 60000)
    }
    
	/**
	 * Takes the Event form pulled from the scala.html and crafts
	 *
	 * @param event - the wrapper from the scala.html form for EiEvent
	 * @return the EiEvent built from the Event wrapper
	 */
	public EiEvent buildEiEvent( Event event ) {
		Date currentDate = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(currentDate);
		XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
		xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);

		JAXBElement<SignalPayload> signalPayload = objectFactory.createSignalPayload(
			new SignalPayload(new PayloadFloat(1)));

		String contextName = event.programName;
		Intervals intervals = new Intervals();
		def intervalList = []
		EiEvent newEvent = event.toEiEvent();

		event.intervals.each { evt, i ->
			intervalList.add(new Interval()
				.withDuration(new DurationPropType()
					.withDuration(new DurationValue()
						.withValue(formatDuration(getDuration(newEvent)))))
					.withUid(new Uid().withText("" + i))
					.withStreamPayloadBase(signalPayload));
		}
		intervals.setIntervals(intervalList);
		newEvent.withEiActivePeriod(new EiActivePeriod()
					.withProperties(new Properties()
						.withDtstart(new Dtstart()
							.withDateTime(new DateTime()
								.withValue(newEvent.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize())))
						.withDuration(new DurationPropType()
							.withDuration(new DurationValue()
								.withValue(formatDuration(getDuration(newEvent, (int)event.getIntervals())))))
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
								.withValue(contextName)))
						.withEventID(event.eventID)
						.withEventStatus(updateStatus(newEvent, (int)event.intervals))
						.withModificationNumber(event.modificationNumber) //changed to the set modification number
						.withPriority(event.priority)
						.withTestEvent("False")
						.withVtnComment(""))
					.withEiEventSignals(new EiEventSignals()
						.withEiEventSignals(new EiEventSignal()
							.withCurrentValue(new CurrentValue()
								.withPayloadFloat(new PayloadFloat()
									.withValue(updateSignalPayload(newEvent)))) //TODO Not sure what this value is supposed to be, must be 0 when NEAR
							.withIntervals(new Intervals()
								.withIntervals(intervalList))
							.withSignalID("TH_SIGNAL_ID")
							.withSignalName("simple")
							.withSignalType(SignalTypeEnumeratedType.LEVEL)));
		return newEvent;
	}
}
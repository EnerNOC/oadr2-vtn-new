package org.enernoc.oadr2.vtn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.EiResponse;
import org.enernoc.open.oadr2.model.EventResponses.EventResponse;
import org.enernoc.open.oadr2.model.EventStatusEnumeratedType;
import org.enernoc.open.oadr2.model.OadrCreatedEvent;
import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent;
import org.enernoc.open.oadr2.model.OadrRequestEvent;
import org.enernoc.open.oadr2.model.OadrResponse;
import org.enernoc.open.oadr2.model.ResponseCode;
import org.enernoc.open.oadr2.model.ResponseRequiredType;
import org.apache.commons.logging.LogFactory

/**
 * EiEventService handles all persistence and object creation of payloads
 * 
 * @author Jeff LaJoie
 *
 */
public class EiEventService{

	static transactional = true
	private static final log = LogFactory.getLog(this)
	
	//static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Events");
	//static EntityManager entityManager = entityManagerFactory.createEntityManager();

    private volatile static EiEventService instance = null;
    
    public EiEventService(){
        
    }
    
    /**
     * Static singleton creation for non-controller packages
     * as Play does not permit injection into multiple packages
     * 
     * @return the sole synchronized EiEventService object
     */
    public static EiEventService getInstance(){
                if(instance == null){
                    instance = new EiEventService();
                }
        return instance;
    }
    
    /**
     * Determines which method to call based on the Object it is passed
     * 
     * @param o - Object to be used to create the responding payload Object 
     * @return an Object to be marshalled to XML and sent over HTTP or XMPP
     */
    public Object handleOadrPayload(Object o){
        if(o instanceof OadrRequestEvent){
            return handleOadrRequest((OadrRequestEvent)o);
        }
        else if(o instanceof OadrCreatedEvent){
            return handleOadrCreated((OadrCreatedEvent)o);
        }
        else if(o instanceof OadrResponse){
            handleFromOadrResponse((OadrResponse)o);
            return null;
        }
        else{
            throw new RuntimeException("Object was not of correct class");
        }
    }    
    
    /**
     * Take a OadrCreatedEvent and persists the information, as well as creating an OadrResponse
     * 
     * @param oadrCreatedEvent
     * @return an OadrResponse containing information to respond to an OadrCreatedEvent
     *
     * public static OadrResponse handleOadrCreated(OadrCreatedEvent oadrCreatedEvent){
        String responseCode = verifyOadrCreated(oadrCreatedEvent);
        if(oadrCreatedEvent.getEiCreatedEvent().getEiResponse().getResponseCode().getValue().charAt(0) == '2'){
            persistFromCreatedEvent(oadrCreatedEvent);
            createNewEm();
            entityManager.persist(oadrCreatedEvent);
            entityManager.getTransaction().commit();
            
            return new OadrResponse()
                .withEiResponse(new EiResponse()
                    .withRequestID("TH_REQUEST_ID")
                    .withResponseCode(new ResponseCode(responseCode))
                    .withResponseDescription("Optional description!"));
        }
        else{
            return new OadrResponse()
                .withEiResponse(new EiResponse()
                        .withRequestID("TH_REQUEST_ID")
                        .withResponseCode(new ResponseCode("200"))
                        .withResponseDescription("Incoming event contained errors"));
        }
    }
     *
     */
    //@Transactional
    public static OadrResponse handleOadrCreated(OadrCreatedEvent oadrCreatedEvent){
        String responseCode = verifyOadrCreated(oadrCreatedEvent);
        if(oadrCreatedEvent.eiCreatedEvent.eiResponse.responseCode.value.charAt(0) == '2'){
            persistFromCreatedEvent(oadrCreatedEvent);
			
			
            
            return new OadrResponse()
                .withEiResponse(new EiResponse()
                    .withRequestID("TH_REQUEST_ID")
                    .withResponseCode(new ResponseCode(responseCode))
                    .withResponseDescription("Optional description!"));
        }
        else{
            return new OadrResponse()
                .withEiResponse(new EiResponse()
                        .withRequestID("TH_REQUEST_ID")
                        .withResponseCode(new ResponseCode("200"))
                        .withResponseDescription("Incoming event contained errors"));
        }
    }
    
    /**
     * Takes an OadrCreatedEvent and verifies that the Response exists and there are no errors in the payload
     * 
     * @param oadrCreatedEvent - the OadrCreatedEvent to be checked for errors
     * @return a response code as a string
     */
   //@SuppressWarnings("unchecked")
   // @Transactional
    public static String verifyOadrCreated(OadrCreatedEvent oadrCreatedEvent){
        if(oadrCreatedEvent.eiCreatedEvent.eventResponses != null){
            String eventId = oadrCreatedEvent.eiCreatedEvent.eventResponses.eventResponses().get(0).getQualifiedEventID().getEventID();
            long modificationNumber = oadrCreatedEvent.getEiCreatedEvent().getEventResponses().getEventResponses().get(0).getQualifiedEventID().getModificationNumber();
            /*ArrayList<EiEvent> events = (ArrayList<EiEvent>) entityManager.createQuery("SELECT event FROM EiEvent event WHERE event.eventDescriptor.eventID = :event AND event.eventDescriptor.modificationNumber = :modNumber")
                    .setParameter("event", eventId)
                    .setParameter("modNumber", modificationNumber)
                    .getResultList();
                    */
			def events = Event.findAllWhere(eventID: eventId, modificationNumber: modificationNumber)		
            /*ArrayList<VenStatus> vens = (ArrayList<VenStatus>) entityManager.createQuery("SELECT venStatus FROM VENStatus venStatus WHERE venStatus.venID = :ven")
                    .setParameter("ven", oadrCreatedEvent.getEiCreatedEvent().getVenID())
                    .getResultList();
                    */
			def vens = VenStatus.findAllWhere(venID: oadrCreatedEvent.eiCreatedEvent.venID)
            if(events.size() > 0 && vens.size() > 0){
                return "200";
            }
            else if(vens.size() == 0){
                return "409";
            }
        }
        return "400";
    }
    
    /**
     * Takes an OadrRequestEvent and persists the data to the tables
     * While formatting an ordering the Pending/Active events for the OadrDistributeEvent
     * 
     * @param oadrRequestEvent - Request incoming from the VEN
     * @return an OadrDistributeEvent containing all payload information
     */
  //  @SuppressWarnings("unchecked")
    //@Transactional
    public static OadrDistributeEvent handleOadrRequest(OadrRequestEvent oadrRequestEvent){
        
        /**
         * Comparator to determine ordering of the OadrEvents
         * Expected ordering is
         * 1. Active events have priority
         * 2. Within Active, priority is determine by the EventDescriptor.Priority
         * 3. If both have equal EventDescriptor.Priority, the earlier start time is the higher priority
         * 4. Pending events are sorted by earlier start time
         * @author jlajoie
         *
         
        
        class OadrEventComparator implements Comparator<OadrEvent>{
            public int compare(OadrEvent eventOne, OadrEvent eventTwo){                
                boolean eventOneIsActive = eventOne.getEiEvent().getEventDescriptor().getEventStatus().equals(EventStatusEnumeratedType.ACTIVE);
                boolean eventTwoIsActive = eventTwo.getEiEvent().getEventDescriptor().getEventStatus().equals(EventStatusEnumeratedType.ACTIVE);
                int comparedEventPriority = eventOne.getEiEvent().getEventDescriptor().getPriority().compareTo(eventTwo.getEiEvent().getEventDescriptor().getPriority());
                int comparedEventDt = eventOne.getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().compare(
                        eventTwo.getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue());                
                if(eventOneIsActive){
                    if(eventTwoIsActive){
                        if(comparedEventPriority == 0){
                            return comparedEventDt;
                        }
                        return comparedEventPriority;
                    }
                    return -1;
                }
                else if(eventTwoIsActive){
                    return 1;
                }
                else{
                    return comparedEventDt;
                }
            }                
        }*/        
        
        EiResponse eiResponse = new EiResponse(); 
        if(!oadrRequestEvent.eiRequestEvent.requestID.equals("")){
            eiResponse.setRequestID(oadrRequestEvent.eiRequestEvent.requestID());
        }
        else{
            eiResponse
                .withRequestID("TH_REQUEST_ID");
        }
        eiResponse.setResponseCode(new ResponseCode("409"));    
        
        //createNewEm();
        //entityManager.persist(oadrRequestEvent);  
        //entityManager.getTransaction().commit();        
        persistFromRequestEvent(oadrRequestEvent);    
        OadrDistributeEvent oadrDistributeEvent = new OadrDistributeEvent()
                .withEiResponse(eiResponse)
                .withRequestID("TH_REQUEST_ID")
                .withVtnID("TH_VTN");                
                
        /*List<Ven> vens = entityManager.createQuery("FROM VEN v WHERE v.venID = :ven")	//THERE CAN ME MULTIPLE VENS WITH THE SAME ID
            .setParameter("ven", oadrRequestEvent.getEiRequestEvent().getVenID())
            .getResultList();*/  
		
		def vens = Ven.findAllWhere(venID: oadrRequestEvent.eiRequestEvent.venID)				      
        def events = new ArrayList<Event>();
		
        for(Ven ven : vens){
            log.info("VEN is - " + ven.venID);
            eiResponse.setResponseCode(new ResponseCode("200"));
            /*for(EiEvent event : (List<EiEvent>)entityManager.createQuery("SELECT event FROM EiEvent event WHERE event.eventDescriptor.eiMarketContext.marketContext.value = :market")
                    .setParameter("market", ven.getProgramID())
                    .getResultList())*/
			Event.findAllWhere(programName: ven.programID).each { event ->
                events.add(event);
            }
			         
            List<OadrEvent> oadrEvents = new ArrayList<OadrEvent>();
			events.each{ e ->
                oadrEvents.add(new OadrEvent()
                    .withEiEvent(EventController.buildEventFromForm(e))
                    .withOadrResponseRequired(ResponseRequiredType.ALWAYS) //TODO Not sure if set to always
                );
            }
            //Collections.sort(oadrEvents, new OadrEventComparator());
            //oadrEvents = listReduce(oadrEvents); //NOT NEEDED ANYMORE, DUPLICATE AND OVERLAPPING EVENTS SHOULD BE CONTAINED
            if(oadrRequestEvent.getEiRequestEvent().getReplyLimit() != null){
                oadrEvents = removeEventsOverLimit(oadrEvents, oadrRequestEvent.getEiRequestEvent().getReplyLimit().intValue());
            }
            oadrDistributeEvent.withOadrEvents(oadrEvents);
        }
        return oadrDistributeEvent;
    }
    
    public static ArrayList<OadrEvent> removeEventsOverLimit(List<OadrEvent> events, int replyLimit){
        ArrayList<OadrEvent> returnList = new ArrayList<OadrEvent>();
        for(int i = 0; i < replyLimit && i < events.size(); i++){
            returnList.add(events.get(i));
        }
        return returnList;
    }
    
    /**
     * Removes duplicate and overlapping events  
     * 
     * @param oadrEvents - List of OadrEvent containing all events within Market Contexts
     * @return - The reduced ArrayList containing no overlapping events within the same MarketContext
     */
   // @Deprecated //no longer used due to how the schema validates the payload
    public static ArrayList<OadrEvent> listReduce(List<OadrEvent> oadrEvents){
        Map<String, OadrEvent> eventMap = new HashMap<String, OadrEvent>();
        for(OadrEvent event : oadrEvents){
            String marketContext = event.getEiEvent().getEventDescriptor().getEiMarketContext().getMarketContext().getValue();
            XMLGregorianCalendar eventOneStartDt = event.getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue();
            XMLGregorianCalendar eventOneEndDt = event.getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue();
            if(eventMap.containsKey(marketContext)){
                eventOneEndDt.add(getDuration(event.getEiEvent()));XMLGregorianCalendar eventTwoDt = 
                        eventMap.get(marketContext).getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue();
                int comparedDt = eventOneEndDt.compare(eventTwoDt);
                if(comparedDt > 0){
                    if(eventOneStartDt.compare(eventTwoDt) != 1){
                        eventMap.put(marketContext, event);
                    }
                }
                
            }
            else{
                eventMap.put(marketContext, event);
            }
        }
        return new ArrayList<OadrEvent>(eventMap.values());
    }
    
    /**
     * Persists the information from an OadrRequestEvent into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
   // @SuppressWarnings("unchecked")
   // @Transactional
    public static void persistFromRequestEvent(OadrRequestEvent requestEvent){
        //List<VenStatus> venStatuses = new ArrayList<VenStatus>();
        def venStatuses = VenStatus.findAllWhere(venID: requestEvent.eiRequestEvent.venID)
		/*venStatuses = entityManager.createQuery("SELECT status FROM VENStatus " +
            "status WHERE status.venID = :ven")
            .setParameter("ven", requestEvent.getEiRequestEvent().getVenID())
            .getResultList();
        */
        if(venStatuses.size() == 0){
            venStatuses.add(new VenStatus());
        }
        for(VenStatus venStatus : venStatuses){
            venStatus.setTime(new Date());
            venStatus.setVenID(requestEvent.getEiRequestEvent().getVenID());
        
            //createNewEm();
            
           // List<Ven> customers = (List<Ven>)entityManager.createQuery("SELECT c FROM VEN c WHERE c.venID = :ven")
           //         .setParameter("ven", requestEvent.getEiRequestEvent().getVenID())
           //         .getResultList();
			def customers = Ven.findAllWhere(venID: requestEvent.eiRequestEvent.venID)
					
            for(Ven customer : customers){
                venStatus.setProgram(customer.getProgramID());
                log.info("Above testing the EiEvent query");
               // List<EiEvent> events = (List<EiEvent>)entityManager.createQuery("SELECT event FROM EiEvent event WHERE event.eventDescriptor.eiMarketContext.marketContext.value = :market")
               //         .setParameter("market", venStatus.getProgram())
               //         .getResultList();
                def events = Event.findAllWhere(programName: venStatus.program)
                if(customer != null){  
                    events.each{event ->
                        venStatus.eventID = event.eventID
                        venStatus.optStatus = "Pending 2"
						venStatus.save()
                    }
                }
            }
        }
    }

    /**
     * Persists the information from an OadrCreatedEvent into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    //@SuppressWarnings("unchecked")
    //@Transactional
    public static void persistFromCreatedEvent(OadrCreatedEvent createdEvent){
        def venStatuses = VenStatus.findAllWhere(venID: createdEvent.getEiCreatedEvent().getVenID())


        venStatuses.each { status ->
            if(createdEvent.getEiCreatedEvent().getEventResponses() != null){
				
                createdEvent.eiCreatedEvent.eventResponses.eventResponses.each { eventResponse ->
                    status.setOptStatus(eventResponse.getOptType().toString());
                }
            }
            status.time = new Date();
			status.save()
        }
    }
    

    /**
     * Persists the information from an OadrResponse into the database
     * 
     * @param requestEvent - The event to be used to form the persistence object
     */
    public void handleFromOadrResponse(OadrResponse response){
		def status = VenStatus.findByRequestID(response.eiResponse.requestID) 
        if(status != null){
            status.time = new Date()
            status.optStatus = "Pending 2"
			status.save()
        }
    }
        

    
    /**
     * Gets the DurationValue from an EiEvent as a java.util.Duration
     * 
     * @param event - EiEvent to have the DurationValue pulled from
     * @return a Duration based on the EiEvent DurationValue
     */
    //@Deprecated //no longer used, only called by other deprecated listReduce method
    public static Duration getDuration(EiEvent event){
        DatatypeFactory df = null;
        try {
            df = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
          e.printStackTrace();
        }
        return df.newDuration(Event.minutesFromXCal(event.getEiActivePeriod().getProperties().getDuration().getDuration().getValue()) * 60000);
    }
    
}

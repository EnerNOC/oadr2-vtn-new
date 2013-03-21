package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import models.Event;
import models.Program;
import models.VEN;
import models.VENStatus;

import org.enernoc.open.oadr2.model.CurrentValue;
import org.enernoc.open.oadr2.model.DateTime;
import org.enernoc.open.oadr2.model.Dtstart;
import org.enernoc.open.oadr2.model.DurationPropType;
import org.enernoc.open.oadr2.model.DurationValue;
import org.enernoc.open.oadr2.model.EiActivePeriod;
import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.EiEventSignal;
import org.enernoc.open.oadr2.model.EiEventSignals;
import org.enernoc.open.oadr2.model.EiTarget;
import org.enernoc.open.oadr2.model.EventDescriptor;
import org.enernoc.open.oadr2.model.EventDescriptor.EiMarketContext;
import org.enernoc.open.oadr2.model.EventStatusEnumeratedType;
import org.enernoc.open.oadr2.model.Interval;
import org.enernoc.open.oadr2.model.Intervals;
import org.enernoc.open.oadr2.model.MarketContext;
import org.enernoc.open.oadr2.model.ObjectFactory;
import org.enernoc.open.oadr2.model.PayloadFloat;
import org.enernoc.open.oadr2.model.Properties;
import org.enernoc.open.oadr2.model.Properties.Tolerance;
import org.enernoc.open.oadr2.model.Properties.Tolerance.Tolerate;
import org.enernoc.open.oadr2.model.SignalPayload;
import org.enernoc.open.oadr2.model.SignalTypeEnumeratedType;
import org.enernoc.open.oadr2.model.Uid;

import play.data.Form;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import service.PushService;
import service.XmppService;

/**
 * Events controller to manage all Event objects created
 * and the display page for those objects
 * 
 * @author Jeff LaJoie
 */
public class Events extends Controller {
      
      @Inject static XmppService xmppService;
      @Inject static PushService pushService;
      static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Events");
      static ObjectFactory objectFactory = new ObjectFactory();
      static EntityManager entityManager = entityManagerFactory.createEntityManager();
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
      public static Result index() {
    	  return redirect(routes.Events.events());
      }
      
      /**
       * The default page render for Events, inclusive of ordering of EiEvents
       * based on their start DateTime, in ascending order
       * 
       * @return the rendered views.html.events page with a sorted list of EiEvents
       * from the EiEventComparator class
       */
      @SuppressWarnings("unchecked")
      @Transactional
      public static Result events(){ 	  
    	  return ok(views.html.events.render());
      }

      @SuppressWarnings("unchecked")
      @Transactional
      public static Result renderAJAXTable(){
          /**
           * Comparator to return the ordering of the two EiEvents based on start time
           * 
           * @author Jeff LaJoie       
           */
          class EiEventComparator implements Comparator<EiEvent>{
              public int compare(EiEvent eventOne, EiEvent eventTwo){
                  return eventOne.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().compare(
                          eventTwo.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue());
              }
          } 
          Date currentDate = new Date();          
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(currentDate);
          XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
          xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
          List<EiEvent> events = JPA.em().createQuery("FROM EiEvent").getResultList();
          Collections.sort(events, new EiEventComparator());
          for(EiEvent e : events){
              e.getEventDescriptor()
                  .withCreatedDateTime(new DateTime().withValue(xCalendar));
              if(!e.getEventDescriptor().getEventStatus().equals(EventStatusEnumeratedType.CANCELLED))
                  e.getEventDescriptor().setEventStatus(updateStatus(e, e.getEiEventSignals().getEiEventSignals().size()));
                  for(EiEventSignal eventSignal : e.getEiEventSignals().getEiEventSignals()){
                      eventSignal.setCurrentValue(new CurrentValue().withPayloadFloat(new PayloadFloat().withValue(updateSignalPayload(e))));
                  }
          }  
          return ok(views.html.eventsTable.render(events, new Event()));
      }
      
      /**
       * The default page render for new events to be created based on
       * the file at views.html.newEvent
       * 
       * @return the rendered page to create an event, with all fields vacant
       */
      public static Result blankEvent(){
    	  Event newForm = new Event();
    	  return ok(views.html.newEvent.render(form(Event.class).fill(newForm), newForm, makeProgramMap()));
      }    

      /**
       * Method called on the newEvent page when the Create this event button is submitted
       * 
       * @return a redirect to the VENStatus page based on the EventID of the created Event
       * @throws JAXBException
       */
      @Transactional
      public static Result newEvent() throws JAXBException{
    	  Form<Event> filledForm = form(Event.class).bindFromRequest();
          if(filledForm.hasErrors()) {
        	  addFlashError(filledForm.errors());
              return badRequest(views.html.newEvent.render(filledForm, new Event(), makeProgramMap()));
          }
    	  else{  
    		  EiEvent newEvent = buildEventFromForm(filledForm.get());
    		  JPA.em().persist(newEvent);	      		  
    		  flash("success", "Event as been created");
    		  List<VEN> vens = getVENs(newEvent);
    		  populateFromPush(newEvent);
    		  pushService.pushNewEvent(newEvent, vens);
    		  return redirect(routes.VENStatuses.venStatuses(newEvent.getEventDescriptor().getEventID()));
    	  }
      }

      /**
       * On the Event display page will take the EventStatus of the event and set it to CANCELLED
       * 
       * @param id - The database ID of the Event to be cancelled
       * @return a redirect to the Events page, which should show the updated EventStatus of the cancelled event
       */
      @Transactional
      public static Result cancelEvent(Long id){
          EiEvent event = JPA.em().find(EiEvent.class, id);
          event.getEventDescriptor().setModificationNumber(event.getEventDescriptor().getModificationNumber() + 1);
          if(event.getEventDescriptor().getEventStatus() != EventStatusEnumeratedType.CANCELLED)
              event.getEventDescriptor().setEventStatus(EventStatusEnumeratedType.CANCELLED);
          else
              event.getEventDescriptor().setEventStatus(updateStatus(event, event.getEiEventSignals().getEiEventSignals().size()));
          for(EiEventSignal e : event.getEiEventSignals().getEiEventSignals()){
              e.setCurrentValue(new CurrentValue().withPayloadFloat(new PayloadFloat(updateSignalPayload(event))));
          }
          return redirect(routes.Events.events());
      }

      /**
       * On the Event display page, will take the Event that is selected and remove it from the database
       * 
       * @param id - database ID of the Event to be deleted
       * @return a redirect to the Events page which should show the list of Events without the deleted event
       */
      @Transactional
      public static Result deleteEvent(Long id){
    	  JPA.em().remove(JPA.em().find(EiEvent.class, id));
          flash("success", "Event has been deleted");
          return redirect(routes.Events.events());
      }

      /**
       * Called when the Save this event button is pressed on the Edit event form
       * 
       * @param id - database ID of the Event to be modified
       * @return a redirect to the Events page which should show the list of Events with the modified event
       */
      @Transactional
      public static Result updateEvent(Long id){
          Form<Event> eventForm = form(Event.class).bindFromRequest();
          if(eventForm.hasErrors()) {
              return badRequest(views.html.editEvent.render(id, eventForm, new Event(), makeProgramMap()));
          }
          Event newEventForm = eventForm.get();
          EiEvent event = JPA.em().find(EiEvent.class, id);            
          Date currentDate = new Date();          
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(currentDate);
          XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
          xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
          
          JAXBElement<SignalPayload> signalPayload = objectFactory.createSignalPayload(new SignalPayload(new PayloadFloat(1)));
          
          String contextName = JPA.em().find(Program.class, Long.parseLong(newEventForm.getMarketContext())).getProgramName();
          Intervals intervals = new Intervals();
          ArrayList<Interval> intervalList = new ArrayList<Interval>();
          
          for(int i=0; i < newEventForm.getIntervals(); i++){
              intervalList.add(new Interval()
                  .withDuration(new DurationPropType()
                      .withDuration(new DurationValue()
                          .withValue(formatDuration(getDuration(event)))))
                  .withUid(new Uid()
                      .withText("" + i))
                  .withStreamPayloadBase(signalPayload));
          }
          intervals.setIntervals(intervalList);                    
          event.setEiActivePeriod(new EiActivePeriod()
                  .withProperties(new Properties()
                      .withDtstart(new Dtstart()
                          .withDateTime(new DateTime()
                              .withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize())))
                      .withDuration(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue(formatDuration(getDuration(event, (int)newEventForm.getIntervals())))))
                      .withTolerance(new Tolerance()
                          .withTolerate(new Tolerate()
                              .withStartafter(new DurationValue()
                                  .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S")))))))
                      .withXEiNotification(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                      .withXEiRampUp(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                      .withXEiRecovery(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))));
          event.setEiTarget(new EiTarget());
          event.setEventDescriptor(new EventDescriptor()
                      .withCreatedDateTime(new DateTime().withValue(xCalendar))
                      .withEiMarketContext(new EiMarketContext()
                              .withMarketContext(new MarketContext()
                                      .withValue(contextName)))
                      .withEventID(newEventForm.getEventID())
                      .withEventStatus(updateStatus(event, (int)newEventForm.getIntervals()))
                      .withModificationNumber(event.getEventDescriptor().getModificationNumber() + 1)
                      .withPriority(newEventForm.getPriority())
                      .withTestEvent("False")
                      .withVtnComment("No VTN Comment"));
          event.setEiEventSignals(new EiEventSignals()
          .withEiEventSignals(new EiEventSignal()
                  .withCurrentValue(new CurrentValue()
                          .withPayloadFloat(new PayloadFloat()
                                  .withValue(updateSignalPayload(event)))) //TODO Not sure what this value is supposed to be, must be 0 when NEAR
                  .withIntervals(new Intervals()
                      .withIntervals(intervalList))
                  .withSignalID("TH_SIGNAL_ID")
                  .withSignalName("simple")
                  .withSignalType(SignalTypeEnumeratedType.LEVEL)));
          
          JPA.em().merge(event);
          flash("success", "Event has been updated");
          return redirect(routes.Events.events());
      }

      /**
       * On the Event display page, is called when one of the Edit buttons are pressed
       * (NOTE: Program and DateTimes are NOT saved when modifying an event and must always be re-entered)
       * 
       * @param id - database ID of the Event to be modified
       * @return a render of the Event form page with the information contained
       */
      @Transactional
      public static Result editEvent(Long id){
    	  Event form = new Event(JPA.em().find(EiEvent.class, id));
    	  return ok(views.html.editEvent.render(id, form(Event.class).fill(form), form, makeProgramMap()));
      }
    
      /**
       * Takes the error Map with a string as a key and adds
       * the key and value to the flash() scope to be accessed
       * 
       * @param errors Map containing the List of errors and the key
       */
      public static void addFlashError(Map<String, List<ValidationError>> errors){
    	  for(String key : errors.keySet()){
    		  List<ValidationError> currentError = errors.get(key);
    		  for(ValidationError error : currentError){
    			  flash(key, error.message());
    		  }
    	  }	  
      }

      /**
       * Updates the EventStatus based on the current time and time of the event
       * 
       * @param event - the event to have the EventStatus updated
       * @param intervals - the number of time intervals contained in the 
       * @return the EventStatusEnumeratedType the EventStatus should be set to
       */
      @Transactional
      public static EventStatusEnumeratedType updateStatus(EiEvent event, int intervals){
          DatatypeFactory df = null;
          try {
              df = DatatypeFactory.newInstance();
          } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
          }
          
          Date currentDate = new Date();          
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(currentDate);
          XMLGregorianCalendar xCalendar = df.newXMLGregorianCalendar(calendar);
          xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
          
          DateTime currentTime = new DateTime().withValue(xCalendar);
          DateTime startTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize());
          DateTime endTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize());
          
          DateTime rampUpTime = new DateTime().withValue(event.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize());
          
          rampUpTime.getValue().add(getDuration(event.getEiActivePeriod().getProperties().getXEiRampUp().getDuration().getValue()));
          Duration d = getDuration(event, intervals);
          endTime.getValue().add(d);
                  
          if(currentTime.getValue().compare(startTime.getValue()) == -1){
              if(currentTime.getValue().compare(rampUpTime.getValue()) == -1 ){
                  return EventStatusEnumeratedType.FAR;
              }
              else{
                  return EventStatusEnumeratedType.NEAR;                 
              }
          }
          else if(currentTime.getValue().compare(startTime.getValue()) > 0 && currentTime.getValue().compare(endTime.getValue()) == -1){
              return EventStatusEnumeratedType.ACTIVE;        
          }
          else if(currentTime.getValue().compare(endTime.getValue()) > 0){
              return EventStatusEnumeratedType.COMPLETED;
          }
          else{
              return EventStatusEnumeratedType.NONE;
          }
      }

      
      /**
       * Updates the SignalPayloadFloat based on the EventStatus contained in the EiEvent
       * 
       * @param event - Contains the EventStatus that determines the SignalPayload
       * @return the SignalPayload as a float to be set in the construction of the EiEvent
       */
      public static float updateSignalPayload(EiEvent event){
          if(event.getEventDescriptor().getEventStatus().equals(EventStatusEnumeratedType.ACTIVE))
              return 1;
          return 0;
      }
      
      /**
       * Passes the VENs and event to the prepareVENs method
       * 
       * @param event - event to be used for getVENs and prepareVENs
       */
      @Transactional
      public static void populateFromPush(EiEvent event){
          List<VEN> customers = getVENs(event);
          prepareVENs(customers, event);
      }

      /**
       * Converts the Program table to a Map usable by the Scala Helper function, /@Select
       * 
       * @return a usable Map<String, String> for the .scala.html files
       */
      @SuppressWarnings("unchecked")
      @Transactional
      public static Map<String, String> makeProgramMap(){
          List<Program> programList = Persistence.createEntityManagerFactory("Events").createEntityManager().createQuery("FROM Program").getResultList();
          Map<String, String> programMap = new HashMap<String, String>();
    	  for(Program program : programList){
    		  programMap.put(program.getId() + "", program.getProgramName());
    	  }
    	  return programMap;
      }

      /**
       * Retrieves the list of VENs where there is a URI (for VTN-Push) and that matches the marketContext matches
       * the programID
       * 
       * @param event - The event with the marketContext to be compared to VENs
       * @return a list of VENs that match the event's MarketContext and possess a clientURI
       */
      @SuppressWarnings("unchecked")
      public static List<VEN> getVENs(EiEvent event){
          return JPA.em().createQuery("SELECT c from VEN c WHERE c.programId = :program and c.clientURI != ''")
                  .setParameter("program", event.getEventDescriptor().getEiMarketContext().getMarketContext().getValue())
                  .getResultList();
      }
      
      /**
       * Prepares the VENs by creating a VENStatus object for each and setting the OptStatus to Pending 1
       * 
       * @param vens - List of VENs to be traversed and will be used to construct a VENStatus object
       * @param event - Event containing the EventID which will be used for construction of a VENStatus object
       */
      public static void prepareVENs(List<VEN> vens, EiEvent event){
          for(VEN v : vens){
              VENStatus venStatus = new VENStatus();
              venStatus.setOptStatus("Pending 1");
              venStatus.setRequestID(v.getClientURI());
              venStatus.setEventID(event.getEventDescriptor().getEventID());
              venStatus.setProgram(v.getProgramId());
              venStatus.setVenID(v.getVenID());
              venStatus.setTime(new Date());
              JPA.em().persist(venStatus);              
          }    
      }
      
      /**
       * Converts an event to a duration based on the event and number of intervals
       * 
       * @param event - Duration that needs to be converted from String to Duration
       * @param intervals - number of intervals to be serviced
       * @return Duration from the event multiplied by the number of intervals
       */
      public static Duration getDuration(EiEvent event, int intervals){
          DatatypeFactory df = null;
          try {
              df = DatatypeFactory.newInstance();
          } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
          }
          Duration duration = df.newDuration(Event.minutesFromXCal(event.getEiActivePeriod().getProperties().getDuration().getDuration().getValue()) * 60000);
          return duration.multiply(intervals);
      }
      
      /**
       * Converts an event to a duration based on the event and number of intervals
       * 
       * @param event - Duration that needs to be converted from String to Duration
       * @return Duration from the event
       */
      public static Duration getDuration(EiEvent event){
          DatatypeFactory df = null;
          try {
              df = DatatypeFactory.newInstance();
          } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
          }
          return df.newDuration(Event.minutesFromXCal(event.getEiActivePeriod().getProperties().getDuration().getDuration().getValue()) * 60000);
      }
      
      /**
       * Converts an event string of DurationValue to a Duration
       * 
       * @param duration - Duration that needs to be converted from String to Duration
       * @return Duration from the event
       */
      public static Duration getDuration(String duration){
          DatatypeFactory df = null;
          try {
              df = DatatypeFactory.newInstance();
          } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
          }
          return df.newDuration(duration);
      }
      
      /**
       * Formats a duration to be acceptable by the schema validation
       * 
       * @param duration - the duration to be modified with the .000 truncated
       * @return String with an acceptable duration value, minus the .000 precision
       */
      public static String formatDuration(Duration duration){
          return duration.toString().replaceAll(".000", "");
      }
      
      /**
       * Takes the Event form pulled from the scala.html and crafts
       * 
       * @param newEventForm - the wrapper from the scala.html form for EiEvent
       * @return the EiEvent built from the Event wrapper
       */
      public static EiEvent buildEventFromForm(Event newEventForm){            
          Date currentDate = new Date();          
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(currentDate);
          XMLGregorianCalendar xCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
          xCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
          
          JAXBElement<SignalPayload> signalPayload = objectFactory.createSignalPayload(new SignalPayload(new PayloadFloat(1)));
          
          String contextName = JPA.em().find(Program.class, Long.parseLong(newEventForm.getMarketContext())).getProgramName();
          Intervals intervals = new Intervals();
          ArrayList<Interval> intervalList = new ArrayList<Interval>();
          EiEvent newEvent = newEventForm.toEiEvent();
          
          for(int i=0; i < newEventForm.getIntervals(); i++){
              intervalList.add(new Interval()
                  .withDuration(new DurationPropType()
                      .withDuration(new DurationValue()
                          .withValue(formatDuration(getDuration(newEvent)))))
                  .withUid(new Uid()
                      .withText("" + i))
                  .withStreamPayloadBase(signalPayload));
          }
          intervals.setIntervals(intervalList);    
          newEvent
              .withEiActivePeriod(new EiActivePeriod()
                  .withProperties(new Properties()
                      .withDtstart(new Dtstart()
                          .withDateTime(new DateTime()
                              .withValue(newEvent.getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().normalize())))
                      .withDuration(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue(formatDuration(getDuration(newEvent, (int)newEventForm.getIntervals())))))
                      .withTolerance(new Tolerance()
                          .withTolerate(new Tolerate()
                              .withStartafter(new DurationValue()
                                  .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S")))))))                      
                      .withXEiNotification(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                      .withXEiRampUp(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))
                      .withXEiRecovery(new DurationPropType()
                          .withDuration(new DurationValue()
                              .withValue((formatDuration(getDuration("P0Y0M0DT0H0M0S"))))))))
              .withEiTarget(new EiTarget())
              .withEventDescriptor(new EventDescriptor()
                      .withCreatedDateTime(new DateTime().withValue(xCalendar))
                      .withEiMarketContext(new EiMarketContext()
                              .withMarketContext(new MarketContext()
                                      .withValue(contextName)))
                      .withEventID(newEventForm.getEventID())
                      .withEventStatus(updateStatus(newEvent, (int)newEventForm.getIntervals()))
                      .withModificationNumber(0)
                      .withPriority(newEventForm.getPriority())
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
                              .withSignalType(SignalTypeEnumeratedType.LEVEL)));
          return newEvent;
      }   
}
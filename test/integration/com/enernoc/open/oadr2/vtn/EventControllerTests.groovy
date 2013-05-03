package com.enernoc.open.oadr2.vtn

import static org.junit.Assert.*

import java.util.Date;
import java.util.List;

import org.junit.*

import com.enernoc.open.oadr2.model.EiEvent;

/**
 * Integration test for Event Controller
 * @author Yang Xiang
 *
 */
class EventControllerTests {
    @Before
    void setUp() {
        def pro1 = new Program(name:"Program1", marketContext:"http://URI1.com")
        def pro2 = new Program(name:"Program2", marketContext:"http://URI2.com")
        def pro3 = new Program(name:"Program3", marketContext:"http://URI3.com")

        def ven1 = new Ven(venID:"ven1", name:"ven-one", clientURI:"http://URI1.com")
        def ven2 = new Ven(venID:"ven2", name:"ven-two", clientURI:"http://URI2.com")
        
        def event1 = new Event(
            program: Program.findWhere(name: "Program1"),
            eventID: "event1",
            startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
            endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:45"),
            priority: 4L,
            intervals: 2L,
            modificationNumber: 0L
            )
        pro1.addToVens(ven1)
        pro1.addToVens(ven2)
        pro1.addToEvents(event1)
        pro1.save( failOnError:true );

        pro2.addToVens(ven2)
        pro3.addToVens(ven2)
        pro2.save( failOnError:true );
        pro3.save( failOnError:true );

    }

    @Test
    /**
     * 1: Test default index method
     */
    void testIndex() {
        def controller = new EventController()
        controller.index()
        
        assert controller.response.redirectedUrl == '/event/events'
    }

    /**
     * 2: Test Event method
     */
    void testEvents() {
        def controller = new EventController()
        def model = controller.events()
        
        assert model.eventList == Event.list()
        
    }

    /**
     * 3: Test blankEvent upon initial call
     */
    void testBlankEvent() {
        def controller = new EventController()
        def model = controller.blankEvent()
        
        assert model.event.eventID == new Event().eventID
        assert model.event.program == new Event().program
        assert model.event.priority == new Event().priority
        assert model.event.startDate <= new Date()
        assert model.event.endDate <= new Date()
        assert model.event.cancelled == new Event().cancelled
        assert model.event.intervals == new Event().intervals
        assert model.event.modificationNumber == 0L

    }

    /**
     * 4: Test blankEvent upon a chained call from newEvent
     */
    void testChainedBlankEvent() {
        def controller = new EventController()
        controller.flash.chainModel = [event: new Event(
                program: Program.findWhere(name: "Program2"),
                eventID: "",
                startDate: new Date(),
                endDate: new Date().next(),
                priority: -1L,
                intervals: 0L,
                modificationNumber: -1L
                )]
        def model = controller.blankEvent()
        
        assert model.event == null

    }

    /**
     * 5: Test successful newEvent
     * 
     */
    void testSuccessfulNewEvent() {
        def event2 = new Event(
                program: Program.findWhere(name: "Program2"),
                eventID: "event2",
                startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
                endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:45"),
                priority: 4L,
                intervals: 2L
                )
        def controller = new EventController()
        controller.params.programID = event2.program.id
        controller.params.eventID = event2.eventID
        controller.params.priority = event2.priority
        controller.params.intervals = event2.intervals
        controller.params.startDate =  event2.startDate.format("dd/MM/yyyy")
        controller.params.startTime = event2.startDate.format("HH:mm")
        controller.params.endDate = event2.endDate.format("dd/MM/yyyy")
        controller.params.endTime = event2.endDate.format("HH:mm")
        controller.newEvent()

        assert controller.flash.message == "Success, your event has been created"
        assert controller.response.redirectedUrl == '/venStatus/venStatuses?eventID=event2'
        assert Event.findWhere(eventID: "event1") != null

    }

    /**
     * 6: Test fail newEvent
     * Note: the date is set into the year 2030 as oppose to new Date() to prevent assertion error for startDate and endDate
     */
    void testInvalidNewEvent() {
        def eventFail = new Event(
                program: Program.findWhere(name: "Program1"),
                eventID: "eventFail",
                startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2025 12:00"),
                endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
                priority: 4L,
                intervals: -2L
                )
        def controller = new EventController()
        controller.params.programID = eventFail.program.id
        controller.params.eventID = eventFail.eventID
        controller.params.priority = eventFail.priority
        controller.params.intervals = eventFail.intervals
        controller.params.startDate =  eventFail.startDate.format("dd/MM/yyyy")
        controller.params.startTime = eventFail.startDate.format("HH:mm")
        controller.params.endDate = eventFail.endDate.format("dd/MM/yyyy")
        controller.params.endTime = eventFail.endDate.format("HH:mm")
        controller.newEvent()
        
        assert controller.response.redirectedUrl == '/event/blankEvent'
        assert controller.flash.chainModel.event.program == eventFail.program
        assert controller.flash.chainModel.event.eventID == eventFail.eventID
        assert controller.flash.chainModel.event.priority == eventFail.priority
        assert controller.flash.chainModel.event.intervals == eventFail.intervals
        assert controller.flash.chainModel.event.startDate == eventFail.startDate
        assert controller.flash.chainModel.event.endDate == eventFail.endDate        

    }
    
    /**
     * 7: Test editEvent upon initial call
     */
    void testEditEvent() {
        def controller = new EventController()
        def event1 = Event.findWhere(eventID: "event1")
        controller.params.id = event1.id
        def model = controller.editEvent()

        assert model.currentEvent.eventID == event1.eventID
        assert model.currentEvent.program == event1.program
        assert model.currentEvent.priority == event1.priority
        assert model.currentEvent.startDate == event1.startDate
        assert model.currentEvent.endDate == event1.endDate
        assert model.currentEvent.cancelled == event1.cancelled
        assert model.currentEvent.intervals == event1.intervals
        assert model.currentEvent.modificationNumber == event1.modificationNumber

    }

    /**
     * 8: Test editEvent upon a chained call from updateEvent
     */
    void testChainedEditEvent() {
        def controller = new EventController()
        controller.flash.chainModel = [currentEvent: new Event(
                program: Program.findWhere(name: "ProgramDoNotExist"),
                eventID: "eventFail",
                startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2025 12:00"),
                endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
                priority: 4L,
                intervals: -2L,
                )]
        def model = controller.editEvent()

        assert model.currentEvent == null

    }

    /**
     * 9: Test successful updateEvent
     */
    void testSuccessfulUpdateEvent() {
        def controller = new EventController()
        def id = Event.findWhere(eventID: "event1").id
        controller.params.id = id
        def event3 = new Event(
                eventID: "eventUpdated",
                startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2025 12:00"),
                endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:14"),
                priority: 3L,
                intervals: 2L
                )
        def counter = Event.get( id ).modificationNumber
        
        controller.params.eventID = event3.eventID
        controller.params.priority = event3.priority
        controller.params.intervals = event3.intervals
        controller.params.startDate =  event3.startDate.format("dd/MM/yyyy")
        controller.params.startTime = event3.startDate.format("HH:mm")
        controller.params.endDate = event3.endDate.format("dd/MM/yyyy")
        controller.params.endTime = event3.endDate.format("HH:mm")
        controller.updateEvent()

        assert controller.response.redirectedUrl == '/event/events'
        assert Event.get( id ).eventID == event3.eventID
        assert Event.get( id ).priority == event3.priority
        assert Event.get( id ).intervals == event3.intervals
        assert Event.get( id ).startDate == event3.startDate
        assert Event.get( id ).endDate == event3.endDate
        assert Event.get( id ).modificationNumber == (counter + 1)

    }

    /**
     * 10: Test fail updateEvent
     */
    void testInvalidUpdateEvent() {
        def controller = new EventController()
        def id = Event.findWhere(eventID: "event1").id
        controller.params.id = id
        def eventFail = new Event(
                eventID: "eventFail",
                startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2025 12:00"),
                endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2010 12:14"),
                priority: -3L,
                intervals: -2L,
                )
        controller.params.eventID = eventFail.eventID
        controller.params.priority = eventFail.priority
        controller.params.intervals = eventFail.intervals
        controller.params.startDate =  eventFail.startDate.format("dd/MM/yyyy")
        controller.params.startTime = eventFail.startDate.format("HH:mm")
        controller.params.endDate = eventFail.endDate.format("dd/MM/yyyy")
        controller.params.endTime = eventFail.endDate.format("HH:mm")
        controller.updateEvent()

        assert controller.response.redirectedUrl == '/event/editEvent'
        assert controller.flash.chainModel.currentEvent.eventID == eventFail.eventID
        assert controller.flash.chainModel.currentEvent.priority == eventFail.priority
        assert controller.flash.chainModel.currentEvent.intervals == eventFail.intervals
        assert controller.flash.chainModel.currentEvent.startDate == eventFail.startDate
        assert controller.flash.chainModel.currentEvent.endDate == eventFail.endDate
        assert controller.flash.chainModel.currentEvent.modificationNumber == 0L
    }

    /**
     * 11: Test deleteEvent
     */
    void testDeleteEvent() {
        def controller = new EventController()
        def id = Event.findWhere(eventID: "event1").id
        controller.params.id = id
        controller.deleteEvent()

        assert controller.response.redirectedUrl == '/event/events'
        assert Event.get( id ) == null
        
    }
    
    /**
     * 12: Test cancelEvent
     */
    void testCancelEvent() {
        def controller = new EventController()
        def id = Event.findWhere(eventID: "event1").id
        def counter = Event.get( id ).modificationNumber
        controller.params.id = id
        controller.cancelEvent()

        assert controller.response.redirectedUrl == '/event/events'
        assert Event.get( id ).cancelled
        assert Event.get( id ).modificationNumber == (counter + 1)
        
    }
    
    /**
     * 13: Test prepareVenStatus
     */
    void testPrepareVenStatus() {
        def controller = new EventController()
        def event1 = Event.findWhere(eventID: "event1")
        def vens = Ven.executeQuery("select v from Ven v where :p in elements(v.programs)", [p: event1.program])
        def ven1 = Ven.findWhere(venID: "ven1")
        def ven2 = Ven.findWhere(venID: "ven2")
        def currentVenStatusCount = VenStatus.count
        controller.prepareVenStatus(event1, vens)

        assert VenStatus.count() > currentVenStatusCount
        assert event1.venStatuses != null
        assert ven1.venStatuses != null
        assert ven2.venStatuses != null
        
        
    }
    
    /**
     * 14: Test parseDate
     */
    void testParseDttm() {
        def controller = new EventController()
        def date = Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:45")
        def StringDate = date.format( "dd/MM/yyyy" )
        def StringTime = date.format( "HH:mm" )
        
        assert controller.parseDttm(StringDate, StringTime) == date
    }
}

package com.enernoc.open.oadr2.vtn

import static org.junit.Assert.*

import java.util.List;

import org.junit.*
import grails.test.GrailsMock

import com.enernoc.open.oadr2.model.CurrentValue
import com.enernoc.open.oadr2.model.DateTime
import com.enernoc.open.oadr2.model.Dtstart
import com.enernoc.open.oadr2.model.DurationPropType
import com.enernoc.open.oadr2.model.DurationValue
import com.enernoc.open.oadr2.model.EiActivePeriod
import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiEventSignal
import com.enernoc.open.oadr2.model.EiEventSignals
import com.enernoc.open.oadr2.model.EiCreatedEvent
import com.enernoc.open.oadr2.model.EiRequestEvent
import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.EiTarget
import com.enernoc.open.oadr2.model.EventResponses
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
import com.enernoc.open.oadr2.model.OptTypeType
import com.enernoc.open.oadr2.model.PayloadFloat
import com.enernoc.open.oadr2.model.Properties
import com.enernoc.open.oadr2.model.QualifiedEventID
import com.enernoc.open.oadr2.model.ResponseCode
import com.enernoc.open.oadr2.model.ResponseRequiredType
import com.enernoc.open.oadr2.model.SignalPayload
import com.enernoc.open.oadr2.model.SignalTypeEnumeratedType
import com.enernoc.open.oadr2.model.Uid
import com.enernoc.open.oadr2.model.EventDescriptor.EiMarketContext
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent
import com.enernoc.open.oadr2.model.Properties.Tolerance
import com.enernoc.open.oadr2.model.Properties.Tolerance.Tolerate
import com.enernoc.open.oadr2.vtn.StatusCode
/**
 * Integration Test for EiEventService
 *
 * @author Yang Xiang
 */
class EiEventServiceTests {

    @Before
    /**
     * Initial setup for EiEventService tests. Adds data into a mock database
     */
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
        def event2 = new Event(
            program: Program.findWhere(name: "Program2"),
            eventID: "event2",
            startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
            endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:45"),
            priority: 4L,
            intervals: 2L,
            modificationNumber: 0L
            )
        def event3 = new Event(
            program: Program.findWhere(name: "Program3"),
            eventID: "event3",
            startDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"),
            endDate: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:45"),
            priority: 3L,
            intervals: 2L,
            modificationNumber: 0L
            )
        def venstatus1 = new VenStatus( optStatus: StatusCode.OPT_IN, time: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"), requestID: "vs1")
        def venstatus2 = new VenStatus( optStatus: StatusCode.OPT_IN, time: Date.parse( "dd/MM/yyyy HH:mm", "01/01/2030 12:00"), requestID: "vs2")
        pro1.addToVens(ven1)
        pro1.addToEvents(event1)
        
        pro2.addToVens(ven2)
        pro2.addToEvents(event2)
        
        pro3.addToVens(ven1)
        pro3.addToVens(ven2)
        pro3.addToEvents(event3)
        
        pro1.save( failOnError:true )
        pro2.save( failOnError:true )
        pro3.save( failOnError:true )
        
        event1.addToVenStatuses(venstatus1)
        event3.addToVenStatuses(venstatus2)
        ven1.addToVenStatuses(venstatus1)
        ven1.addToVenStatuses(venstatus2)
        event1.save( failOnError:true )
        event3.save( failOnError:true )
        ven1.save( failOnError:true )
        
        def venLog = new VenTransactionLog(venID: "ven1", sentDate: new Date(), UID: "ven1ID").save(failOnError:true, flush: true)
        
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test
    /**
     * 1: Test handelOadrPayload
     */
    void testHandleOadrPayload() {
        def eiEventService = new EiEventService()
        def oadrRequestEventObject = new OadrRequestEvent().withEiRequestEvent( new EiRequestEvent().withReplyLimit( 5L ).withVenID( "ven2" ) )
        def oadrCreatedEventObject = 
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) ) )
            
        def oadrResponseObject = new OadrResponse()
        
        def nullObject
        try {
            eiEventService.handleOadrPayload(null)
        } 
        catch (RunetimeException) {
            nullObject = RunetimeException.message;
        }
        
        def otherObject
        def ob = new String()
        try {
            eiEventService.handleOadrPayload(ob)
        } 
        catch (RunetimeException) {
            otherObject = RunetimeException.message;
        }
        assert nullObject == "Payload may not be null"
        assert eiEventService.handleOadrPayload(oadrRequestEventObject) instanceof OadrDistributeEvent
        assert eiEventService.handleOadrPayload(oadrCreatedEventObject) instanceof OadrResponse
        assert eiEventService.handleOadrPayload(oadrResponseObject) == null
        assert otherObject == "Payload was unknown type: ${ob?.class}"
    }
    
    /**
     * 2: Test handleOadrCreated
     */
    void testBadHandleOadrCreated() {
        def eiEventService = new EiEventService()
        def invalidUriOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("200")) ) )
        def validUriOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("200"))
                    .withRequestID( "ven1ID" ) ) )
        def badOadrResponse = eiEventService.handleOadrCreated( invalidUriOadrCreatedObject )
        def validOadrResponse = eiEventService.handleOadrCreated( validUriOadrCreatedObject )
        
        assert badOadrResponse instanceof OadrResponse
        assert badOadrResponse.eiResponse.requestID == null
        assert badOadrResponse.eiResponse.responseCode.value == "404"
        assert badOadrResponse.eiResponse.responseDescription == "UID does not exist in Ven Transaction Log"
        assert validOadrResponse instanceof OadrResponse
        assert validOadrResponse.eiResponse.requestID == "ven1ID"
        assert validOadrResponse.eiResponse.responseCode.value == "200"
        assert validOadrResponse.eiResponse.responseDescription == "OK"
    }
    
    
    /**
     * 3: Test isSuccessful
     */
    void testIsSuccessful() {
        def eiEventService = new EiEventService()
        def goodOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("200")) ) )
        def badOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) ) )
        
        assert eiEventService.isSuccessful( goodOadrCreatedObject )
        assert !eiEventService.isSuccessful( badOadrCreatedObject )
    }

    /**
     * 4: Test verifyOadrCreated
     */
    void testVerifyOadrCreated() {
        def eiEventService = new EiEventService()
        def goodResponse = new EventResponses.EventResponse()
            .withResponseCode(new ResponseCode("200"))
            .withQualifiedEventID( new QualifiedEventID()
            .withEventID("event1")
            .withModificationNumber( 0L ))
        def badCodeResponse = new EventResponses.EventResponse()
            .withResponseCode(new ResponseCode("521"))
            .withQualifiedEventID( new QualifiedEventID()
            .withEventID("event1")
            .withModificationNumber( 0L ))
        def badEventResponse = new EventResponses.EventResponse()
            .withResponseCode(new ResponseCode("200"))
            .withQualifiedEventID( new QualifiedEventID()
            .withEventID("invalidEvent")
            .withModificationNumber( 0L ))

        def noEventResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("200")) ) )
        def goodEventResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) )
                .withEventResponses( new EventResponses()
                    .withEventResponses( goodResponse ) )
                .withVenID( "ven1" ))
        def badCodeResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) )
                .withEventResponses( new EventResponses()
                    .withEventResponses( badCodeResponse ) )
                .withVenID( "ven1" ))
        def badEventResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) )
                .withEventResponses( new EventResponses()
                    .withEventResponses( badEventResponse ) )
                .withVenID( "ven1" ))
        def badVenResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) )
                .withEventResponses( new EventResponses()
                    .withEventResponses( goodResponse ) )
                .withVenID( "ven2" ))
        
        assert eiEventService.verifyOadrCreated( noEventResponseOadrCreatedObject )[0] == "200"
        assert eiEventService.verifyOadrCreated( noEventResponseOadrCreatedObject )[1] == "OK"
        assert eiEventService.verifyOadrCreated( goodEventResponseOadrCreatedObject )[0] == "200"
        assert eiEventService.verifyOadrCreated( goodEventResponseOadrCreatedObject )[1] == "OK"
        assert eiEventService.verifyOadrCreated( badCodeResponseOadrCreatedObject )[0] == "521"
        assert eiEventService.verifyOadrCreated( badCodeResponseOadrCreatedObject )[1] == "eventResponse contained a non-200 response: $badCodeResponse"
        assert eiEventService.verifyOadrCreated( badEventResponseOadrCreatedObject )[0] == "404"
        assert eiEventService.verifyOadrCreated( badEventResponseOadrCreatedObject )[1] == "Event not found"
        assert eiEventService.verifyOadrCreated( badVenResponseOadrCreatedObject )[0] == "409"
        assert eiEventService.verifyOadrCreated( badVenResponseOadrCreatedObject )[1] == "Invalid VEN ID"
        
    }
    
    /**
     * 5:Test handleOadrRequest
     * Bean injection does not work thus oadrDistributeEvent.vtnID == null
     */
    void testHandleOadrRequest() {
        def eiEventService = new EiEventService()
        def oadrRequestEventObject = 
            new OadrRequestEvent().withEiRequestEvent( new EiRequestEvent()
                .withReplyLimit( 5L )
                .withVenID( "ven1" )
                .withRequestID( "I am ID" ) )
        def oadrDistributeEvent = eiEventService.handleOadrRequest(oadrRequestEventObject)
        assert oadrDistributeEvent.eiResponse.requestID == "I am ID"
        assert oadrDistributeEvent.oadrEvents[0].eiEvent.eventDescriptor.eventID == "event1"
        assert oadrDistributeEvent.oadrEvents[0].eiEvent.eventDescriptor.priority == 4L
        assert oadrDistributeEvent.oadrEvents[1].eiEvent.eventDescriptor.eventID == "event3"
        assert oadrDistributeEvent.oadrEvents[1].eiEvent.eventDescriptor.priority == 3L   
        assert VenTransactionLog.findWhere(UID: "I am ID") != null 
    }
    
    /**
     * 6:Test persistFromRequestEvent
     * 
     */
    void testPersistFromRequestEvent() {
        def eiEventService = new EiEventService()
        def events = Event.executeQuery("select e from Event e, Ven v where  v.venID = :vID and e.program in elements(v.programs) and e.endDate > :d",
            [vID: "ven1" , d: new Date()])
        def oadrRequestEventObject =
            new OadrRequestEvent().withEiRequestEvent( new EiRequestEvent()
                .withReplyLimit( 5L )
                .withVenID( "ven1" )
                .withRequestID( "I am ID" ) )
        eiEventService.persistFromRequestEvent(oadrRequestEventObject, events)
        Ven.findWhere(venID: "ven1").venStatuses.each { v ->
            assert v.time.format( "dd/MM/yyyy HH:mm" ) == new Date().format( "dd/MM/yyyy HH:mm" )
        }
    }

    /**
     * 7: Test persistFromCreatedEvent
     */
    void testPersistFromCreatedEvent() {
        def eiEventService = new EiEventService()
        def optType = OptTypeType.OPT_OUT
        def goodResponse = new EventResponses.EventResponse()
            .withResponseCode(new ResponseCode("200"))
            .withQualifiedEventID( new QualifiedEventID()
                .withEventID("event1")
                .withModificationNumber( 0L ))
            .withOptType(optType)
       def goodEventResponseOadrCreatedObject =
            new OadrCreatedEvent().withEiCreatedEvent( new EiCreatedEvent()
                .withEiResponse( new EiResponse()
                    .withResponseCode( new ResponseCode("500")) )
                .withEventResponses( new EventResponses()
                    .withEventResponses( goodResponse ) )
                .withVenID( "ven1" ))
            
        eiEventService.persistFromCreatedEvent(goodEventResponseOadrCreatedObject)
        
        Ven.findWhere(venID: "ven1").venStatuses.each { v ->
            assert v.time.format( "dd/MM/yyyy HH:mm" ) == new Date().format( "dd/MM/yyyy HH:mm" )
            assert v.getStatusText() == optType.value()
            
        }
    }

    /**
     * 8: Test handleOadrResponse
     */
    def testHandleOadrResponse() {
        def eiEventService = new EiEventService()
        def oadrResponse = new OadrResponse()
            .withEiResponse(new EiResponse()
                .withRequestID("ven1ID")
                .withResponseCode(new ResponseCode("200")))
        eiEventService.handleOadrResponse( oadrResponse )
        
        Ven.findWhere(venID: "ven1").venStatuses.each { venStatus ->
            assert venStatus.optStatus == StatusCode.DISTRIBUTE_SENT
            assert venStatus.time.format( "dd/MM/yyyy HH:mm" ) == new Date().format( "dd/MM/yyyy HH:mm" )
        }
        
    }

}

package com.enernoc.open.oadr2.vtn



import grails.test.mixin.*
import org.junit.*
import grails.test.mixin.domain.DomainClassUnitTestMixin;
/**
 * Unit test for VenStatus Controller
 * @author Yang Xiang
 * 
 */
@TestFor(VenStatusController)
@TestMixin(DomainClassUnitTestMixin)
class VenStatusControllerTests {

    /**
     * Initial setup for VenStatus tests. Adds data into a mock database
     */
    void setUp() {
        mockDomain(Program)
        mockDomain(Ven)
        mockDomain(Event)
        mockDomain(VenStatus)
        def pro1 = new Program(name:"Program1", marketContext:"http://URI1.com").save(flush: true, failOnError: true)
        def ven1 = new Ven(programs: [pro1], venID:"VEN1", name:"ven-one", clientURI:"http://URI1.com").save(flush: true, failOnError: true)
        def ven2 = new Ven(programs: [pro1], venID:"VEN2", name:"ven-two", clientURI:"http://URI2.com").save(flush: true, failOnError: true)
        def event1 = new Event(program: pro1, eventID: "valid", startDate: new Date(), endDate: new Date().next()).save(flush: true, failOnError: true)
        def venstatus1 = new VenStatus( event: event1, ven: ven1, optStatus: StatusCode.OPT_IN, time: new Date()).save(flush: true, failOnError: true)
        def venstatus2 = new VenStatus( event: event1, ven: ven2, optStatus: StatusCode.OPT_IN, time: new Date()).save(flush: true, failOnError: true)

    }
    
    /**
     * 1: Test default index method
     */
    void testIndex() {
        controller.index()
        
        assert response.redirectedUrl == '/venStatus/venStatuses'
    }
    
    /**
     * 2: Test venStatuses method
     */
    void testVenStatuses() {
        def event = Event.findWhere(eventID: "valid")
        controller.params.eventID = event.eventID
        def model = controller.venStatuses()

        assert model.venStatusList == event.venStatuses
        assert model.eventList == Event.list()
        assert model.event == event.eventID
    }
    
    /**
     * 3: Test deleteStatus method
     */
    void testDeleteStatus() {
        controller.params.id = 1
        def eventID = VenStatus.get(1).event.eventID
        controller.deleteStatus()

        assert response.redirectedUrl == '/venStatus/venStatuses?eventID=valid'
        assert VenStatus.get(1) == null
    }
}

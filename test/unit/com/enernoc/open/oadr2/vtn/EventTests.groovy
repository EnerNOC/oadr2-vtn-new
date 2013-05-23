package com.enernoc.open.oadr2.vtn



import grails.test.mixin.*
import org.junit.*

/**
 * Unit test for Event
 * @author Yang Xiang
 * 
 */
@TestFor(Event)
@Mock([Event, Program])
class EventTests {

    /**
     * Initial setup for Event tests. Adds data into a mock database
     */
    void setUp() {
        mockDomain(Program, [
            [name:"Program1", marketContext:"http://URI1.com"],
            [name:"Program2", marketContext:"http://URI2.com"],
            [name:"Program3", marketContext:"http://URI3.com"] ])
        
        mockDomain(Event, [
            [program: Program.findWhere(name: "Program1"), priority: 1, eventID: "valid1", startDate: new Date(), endDate: new Date().next()],
            [program: Program.findWhere(name: "Program2"), priority: 33, eventID: "valid2", startDate: new Date(), endDate: new Date().next()],
            [program: Program.findWhere(name: "Program3"), priority: 5, eventID: "valid3", startDate: new Date(), endDate: new Date().next()],
            [program: Program.findWhere(name: "Program1"), priority: 22, eventID: "valid11", startDate: new Date().next(), endDate: new Date().next().next()],
            [program: Program.findWhere(name: "Program2"), priority: 13, eventID: "valid21", startDate: new Date().next(), endDate: new Date().next().next()] ])
          
    }
    
    /**
     * Test Event with no input parameters   
     */
    void testNullEvent() {
        def nullEvent = new Event()
        assert !nullEvent.validate()
        assert "nullable" == nullEvent.errors["program"].code
        assert "nullable" == nullEvent.errors["eventID"].code
        assert 0L == nullEvent.priority
        assert "nullable" == nullEvent.errors["startDate"].code
        assert "nullable" == nullEvent.errors["endDate"].code
        assert false == nullEvent.cancelled
        assert 1L == nullEvent.intervals
        assert 0L == nullEvent.modificationNumber
    }
    
    /**
     * Test Event blank, unique and min constraints
     */
    void testConstraintEvent() {
        def badEvent = new Event(
                program: Program.findWhere(name: "Program3"),
                eventID: "",
                startDate: new Date(),
                endDate: new Date().next(),
                priority: -1L,
                intervals: 0L,
                modificationNumber: -1L
                )
        assert !badEvent.validate()
        assert "blank" == badEvent.errors["eventID"].code
        badEvent.eventID = "valid3"
        assert !badEvent.validate()
        assert "unique" == badEvent.errors["eventID"].code
        assert "min.notmet" == badEvent.errors["priority"].code
        assert "min.notmet" == badEvent.errors["intervals"].code
        assert "min.notmet" == badEvent.errors["modificationNumber"].code        
    }
    
    /**
     * Test custom validators in Event
     */
    void testValidEvent() {
        def sDate = new Date().next()
        def eDate = sDate.next()
        def badValidateEvent = new Event(
                program: Program.findWhere(name: "Program3"),
                eventID: "valid31",
                startDate: eDate,
                endDate: sDate,
                priority: 1L,
                intervals: 2L,
                modificationNumber: 0L
                )
        assert !badValidateEvent.validate()
        assert "validator.invalid" == badValidateEvent.errors["startDate"].code
        assert "validator.invalid" == badValidateEvent.errors["endDate"].code
        badValidateEvent.startDate = eDate
        badValidateEvent.endDate = eDate
        assert "validator.invalid" == badValidateEvent.errors["startDate"].code
        assert "validator.invalid" == badValidateEvent.errors["endDate"].code
        badValidateEvent.startDate = sDate
        badValidateEvent.endDate = eDate
        assert badValidateEvent.validate()
        badValidateEvent.program = Program.findWhere(name: "Program1")
        assert !badValidateEvent.validate()
        assert "validator.invalid" == badValidateEvent.errors["program"].code        
    }
    
    /**
     * Test compareTo through list.sort()
     */
    void testCompareTo() {
        def eventList = Event.list().sort()
        def orderedList = []
        orderedList << Event.findWhere(eventID: "valid2")
        orderedList << Event.findWhere(eventID: "valid3")
        orderedList << Event.findWhere(eventID: "valid1")
        orderedList << Event.findWhere(eventID: "valid11")
        orderedList << Event.findWhere(eventID: "valid21")
        assert eventList == orderedList
      }
}
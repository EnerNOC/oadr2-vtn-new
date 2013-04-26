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
            [name:"Program2", marketContext:"http://URI2.com"] ])
        
        mockDomain(Event, [
            [program: Program.findWhere(name: "Program1"), eventID: "valid", startDate: new Date(), endDate: new Date().next()] ])
          
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
                program: Program.findWhere(name: "Program2"),
                eventID: "",
                startDate: new Date(),
                endDate: new Date().next(),
                priority: -1L,
                intervals: 0L,
                modificationNumber: -1L
                )
        assert !badEvent.validate()
        assert "blank" == badEvent.errors["eventID"].code
        badEvent.eventID = "valid"
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
        def sDate = new Date()
        def eDate = sDate.next()
        def badValidateEvent = new Event(
                program: Program.findWhere(name: "Program2"),
                eventID: "valid2",
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
}

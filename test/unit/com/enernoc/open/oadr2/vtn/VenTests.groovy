package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import org.junit.*

/**
 * Unit test for VEN
 * @author Yang Xiang
 * 
 */
@TestFor(Ven)
@Mock([Ven, Program])
class VenTests {
    
    /**
     * Initial setup for VEN tests. Adds data into a mock database
     */
    void setUp() {
        mockDomain(Program, [ [name:"Program1", marketContext:"http://URI1.com"] ])
        
        mockDomain(Ven, [
            [programs: Program.findWhere(name: "Program1"), name: "ven-one", venID: "VEN1", clientURI: "http://URI1.com"] ])
          
    }
    
    /**
     * Test VEN with no input parameters
     */
    void testNullVen() {
        def nullVen = new Ven()
        assert !nullVen.validate()
        assert "nullable" == nullVen.errors["name"].code
        assert "nullable" == nullVen.errors["venID"].code
        assert "nullable" == nullVen.errors["programs"].code
        
    }
    
    /**
     * Test VEN blank, unique and url constraints
     */
    void testConstraintEvent() {
        def blankVen = new Ven(programs: Program.findWhere(name: "Program1"), name: "", venID: "")
        assert !blankVen.validate()
        assert "blank" == blankVen.errors["venID"].code
        
        def badVen = new Ven(programs: Program.findWhere(name: "Program1"), name: "", venID: "VEN1")
        assert !badVen.validate()
        assert "unique" == badVen.errors["venID"].code
        badVen.venID = "ven"
        assert badVen.validate()
    }
    
    def testClientURI() {
        def ven = new Ven(programs: Program.findWhere(name: "Program1"), 
            name: "henry", venID: "bob", clientURI: " ")
        assert ! ven.validate()
        assert 'blank' == ven.errors['clientURI'].code
        
        ven.clientURI = "http://google.com/asfd"
        assert ven.validate()
        
        ven.clientURI = ":[]'pblah"
        assert ! ven.validate()
        assert 'invalid' == ven.errors['clientURI'].code
        
        ven.clientURI = "ftp://google.com"
        assert ! ven.validate()
        assert 'invalidscheme' == ven.errors['clientURI'].code
        
        ven.clientURI = "xmpp:ven123@enernoc.com/bob"
        assert ven.validate()
    }

}

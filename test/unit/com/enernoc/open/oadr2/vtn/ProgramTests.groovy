package com.enernoc.open.oadr2.vtn



import grails.test.mixin.*
import org.junit.*

/**
 * Unit test for Program
 * @author Yang Xiang
 * 
 */
@TestFor(Program)
@Mock([Program])
class ProgramTests {

    /**
     * Initial setup for Program tests. Adds data into a mock database
     */
    void setUp() {
        mockDomain(Program, [ [name:"Program1", marketContext:"http://URI1.com"] ])
    }
    
    /**
     * Test Program with no input parameters
     */
    void testNullProgram() {
        def nullProgram = new Program()
        assert !nullProgram.validate()
        assert "nullable" == nullProgram.errors["name"].code
        assert "nullable" == nullProgram.errors["marketContext"].code
    }
    
    /**
     * Test Program blank, unique and url constraints
     */
    void testConstraintEvent() {   
        def blankProgram = new Program(name: "", marketContext: "")
        assert !blankProgram.validate()
        assert "blank" == blankProgram.errors["name"].code
        assert "blank" == blankProgram.errors["marketContext"].code
        
        def badProgram = new Program(name: "Program1", marketContext: "URIFail")
        assert !badProgram.validate()
        assert "unique" == badProgram.errors["name"].code
        assert "url.invalid" == badProgram.errors["marketContext"].code
        badProgram.name = "valid"
        badProgram.marketContext = "http://Valid.com"
        assert badProgram.validate()
    }
}

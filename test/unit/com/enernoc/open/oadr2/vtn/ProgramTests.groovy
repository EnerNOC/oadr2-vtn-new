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
        mockDomain(Program, [ [programName:"Program1", programURI:"http://URI1.com"] ])
    }
    
    /**
     * Test Program with no input parameters
     */
    void testNullProgram() {
        def nullProgram = new Program()
        assert !nullProgram.validate()
        assert "nullable" == nullProgram.errors["programName"].code
        assert "nullable" == nullProgram.errors["programURI"].code
    }
    
    /**
     * Test Program blank, unique and url constraints
     */
    void testConstraintEvent() {   
        def blankProgram = new Program(programName: "", programURI: "")
        assert !blankProgram.validate()
        assert "blank" == blankProgram.errors["programName"].code
        assert "blank" == blankProgram.errors["programURI"].code
        
        def badProgram = new Program(programName: "Program1", programURI: "URIFail")
        assert !badProgram.validate()
        assert "unique" == badProgram.errors["programName"].code
        assert "url.invalid" == badProgram.errors["programURI"].code
        badProgram.programName = "valid"
        badProgram.programURI = "http://Valid.com"
        assert badProgram.validate()
    }
}

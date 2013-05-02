package com.enernoc.open.oadr2.vtn



import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin;
import org.junit.*

/**
 * Unit test for Program Controller
 * @author Yang Xiang
 * 
 */
@TestFor(ProgramController)
@TestMixin(DomainClassUnitTestMixin)
class ProgramControllerTests {
    def messageSource
    /**
     * set default mock data
     */
    @Before
    void setUp() {
        mockDomain(Program) 
        mockDomain(Ven)
        
        def pro1 = new Program(name:"Program1", marketContext:"http://URI1.com")
        def pro2= new Program(name:"Program2", marketContext:"http://URI2.com")
        def ven1 = new Ven(programs: pro1, name: "ven-one", venID: "VEN1", clientURI: "http://URI1.com").save(flush: true, failOnError: true)
        def ven2 = new Ven(programs: [pro1, pro2], name: "ven-two", venID: "VEN2", clientURI: "http://URI2.com").save(flush: true, failOnError: true)
        pro1.vens = [ven1, ven2]
        pro1.save(flush: true, failOnError: true)
        pro2.vens = [ven2]
        pro2.save(flush: true, failOnError: true)
        controller.messageSource = [getMessage: { errors, locale -> return "message" }]

    }
    
    /**
     * 1: Test default index method
     */
    void testIndex() {
       controller.index()
       
       assert response.redirectedUrl == '/program/programs'
    }
    
    /**
     * 2: Test programs method
     */
    void testPrograms() {
        def model = controller.programs()

        assert model.programList == Program.listOrderByName(order:"desc")
    }
    
    /**
     * 3: Test blankProgram upon initial call
     */
    void testBlankProgram() {
        def model = controller.blankProgram()
        
        assert model.program.name == new Program().name
        assert model.program.marketContext == new Program().marketContext
        
    }
    
    /**
     * 4: Test blankProgram upon a chained call from newProgram
     */
    void testChainedBlankProgram() {
        controller.flash.chainModel = [program: new Program(name:"chainProgram", marketContext:"chain.com")]
        def model = controller.blankProgram()

        assert model.program == null

    }

    /**
     * 5: Test successful newProgram
     */
    void testSuccessfulNewProgram() {
        def program3 = new Program(name: "Program3", marketContext: "http://URI3.com") 
        controller.params.name = program3.name
        controller.params.marketContext = program3.marketContext
        controller.newProgram()
        
        assert response.redirectedUrl == '/program/programs'
        assert controller.flash.message == "Success, your Program has been created"
        assert Program.findWhere(name: "Program3") != null

    }

    /**
     * 6: Test fail newProgram
     */
    void testInvalidNewProgram() {
        def programBad = new Program(name: "Program3", marketContext: "http://URI3")
        controller.params.name = programBad.name
        controller.params.marketContext = programBad.marketContext
        controller.newProgram()

        assert controller.response.redirectedUrl == '/program/blankProgram'
        assert controller.flash.message == "Please fix the errors below: "
        assert controller.flash.chainModel.program.name == programBad.name
        assert controller.flash.chainModel.program.marketContext == programBad.marketContext
        assert Program.findWhere(name: "Program4") == null
        

    }
    /**
     * 7: Test editProgram upon initial call
     */
    void testEditProgram() {
        controller.params.id = 1
        def model = controller.editProgram()
        
        assert model.program.name == Program.get( 1 ).name
        assert model.program.marketContext == Program.get( 1 ).marketContext
        
    }
    
    /**
     * 8: Test editProgram upon a chained call from updateProgram
     */
    void testChainedEditProgram() {
        controller.flash.chainModel = [program: new Program(name:"chainProgram", marketContext:"chain.com")]
        def model = controller.editProgram()

        assert model.program == null

    }

    /**
     * 9: Test successful updateProgram
     */
    void testSuccessfulUpdateProgram() {
        def program1 = new Program(name: "Program1-1", marketContext: "http://URI1-1.com")
        controller.params.id = 1
        controller.params.name = program1.name
        controller.params.marketContext = program1.marketContext
        controller.updateProgram()

        assert response.redirectedUrl == '/program/programs'
        assert controller.flash.message == "Success, your Program has been updated"
        assert Program.get(1).name == program1.name
        assert Program.get(1).marketContext == program1.marketContext
        
    }

    /**
     * 10: Test fail updateProgram
     */
    void testInvalidUpdateProgram() {
        def programBad = new Program(name: "Program2", marketContext: "http://URI2")
        controller.params.id = 1
        controller.params.name = programBad.name
        controller.params.marketContext = programBad.marketContext
        controller.updateProgram()

        assert controller.response.redirectedUrl == '/program/editProgram'
        assert controller.flash.message == "Please fix the errors below: "
        assert controller.flash.chainModel.program.name == programBad.name
        assert controller.flash.chainModel.program.marketContext == programBad.marketContext

    }
    
    /**
     * 11: Test deleteProgram 
     */
    void testDeleteProgram() {
        controller.params.id = 1
        controller.deleteProgram()
        
        assert Ven.findWhere(venID: "VEN1") == null
        assert Ven.findWhere(venID: "VEN2") != null
        assert Program.get( 1 ) == null
        assert controller.response.redirectedUrl == '/program/programs'
        
    }
}

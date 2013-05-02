package com.enernoc.open.oadr2.vtn

import static org.junit.Assert.*
import org.junit.*
/**
 * Integration test for VenController
 * @author Yang Xiang
 *
 */
class VenControllerTests {

    @Before
    void setUp() {
        def pro1 = new Program(name:"Program1", marketContext:"http://URI1.com")
        def pro2 = new Program(name:"Program2", marketContext:"http://URI2.com")
        def pro3 = new Program(name:"Program3", marketContext:"http://URI3.com")

        def Ven1 = new Ven(venID:"VEN1", name:"ven-one", clientURI:"http://URI1.com")
        pro1.addToVens(Ven1)
        pro1.save( failOnError:true );

        def Ven2 = new Ven(venID:"VEN2", name:"ven-two", clientURI:"http://URI2.com")
        pro2.addToVens(Ven2)
        pro3.addToVens(Ven2)
        pro2.save( failOnError:true );
        pro3.save( failOnError:true );

    }

    @Test
    /**
     * 1: Test default index method
     */
    void testIndex() {
        def controller = new VenController()
        controller.index()
        
        assert controller.response.redirectedUrl == '/ven/vens'
    }

    /**
     * 2: Test vens method
     */
    void testVens() {
        def controller = new VenController()
        def model = controller.vens()
        
        assert model.venList == Ven.listOrderByVenID(order:"desc")
        
    }

    /**
     * 3: Test blankVEN upon initial call
     */
    void testBlankVEN() {
        def controller = new VenController()
        def model = controller.blankVEN()

        assert model.ven.venID == new Ven().venID
        assert model.ven.name == new Ven().name
        assert model.ven.programs == new Ven().programs
        assert model.ven.clientURI == new Ven().clientURI

    }

    /**
     * 4: Test blankVEN upon a chained call from newVEN
     */
    void testChainedBlankVEN() {
        def controller = new VenController()
        controller.flash.chainModel = [ven: new Ven(venID:"VEN2", name:"ven-two", clientURI:"http://URI2.com")]
        def model = controller.blankVEN()

        assert model.ven == null

    }

    /**
     * 5: Test successful newVEN
     */
    void testSuccessfulNewVEN() {
        def Ven3 = new Ven(venID:"VEN3", name:"ven-three", clientURI:"http://URI3.com")
        def controller = new VenController()
        controller.params.programID = ["1", "2"]
        controller.params.venID = Ven3.venID
        controller.params.name = Ven3.name
        controller.params.clientURI = Ven3.clientURI
        controller.newVEN()
        
        assert controller.response.redirectedUrl == '/ven/vens'
        assert controller.flash.message == "Success, your VEN has been created"
        assert Ven.findWhere(name: "ven-three") != null

    }

    /**
     * 6: Test fail newVEN
     */
    void testInvalidNewVEN() {
        def Ven4 = new Ven(venID:"VEN4", name:"ven-four", clientURI:"http://URI4.com")
        def controller = new VenController()
        controller.params.programID = []
        controller.params.venID = Ven4.venID
        controller.params.name = Ven4.name
        controller.params.clientURI = Ven4.clientURI
        controller.newVEN()

        assert controller.response.redirectedUrl == '/ven/blankVEN'
        assert controller.flash.message == "Please fix the errors below: "
        assert controller.flash.chainModel.ven.programs.isEmpty()
        assert controller.flash.chainModel.ven.venID == Ven4.venID
        assert controller.flash.chainModel.ven.name == Ven4.name
        assert controller.flash.chainModel.ven.clientURI == Ven4.clientURI

    }
    
    /**
     * 7: Test editVEN upon initial call
     */
    void testEditVEN() {
        def controller = new VenController()
        controller.params.id = 1
        def model = controller.editVEN()

        assert model.currentVen.programs == Ven.get( 1 ).programs
        assert model.currentVen.venID == Ven.get( 1 ).venID
        assert model.currentVen.name == Ven.get( 1 ).name
        assert model.currentVen.clientURI == Ven.get( 1 ).clientURI

    }

    /**
     * 8: Test editVEN upon a chained call from updateVEN
     */
    void testChainedEditVEN() {
        def controller = new VenController()
        controller.flash.chainModel = [currentVen: new Ven(venID:"VEN4", name:"ven-four", clientURI:"http://URI4.com")]
        def model = controller.editVEN()

        assert model.currentVen == null

    }

    /**
     * 9: Test successful updateVEN
     */
    void testSuccessfulUpdateVEN() {
        def controller = new VenController()
        controller.params.id = 1
        def Ven4 = new Ven(venID:"VEN4", name:"ven-four", clientURI:"http://URI4.com")
        controller.params.programID = ["2", "3"]
        controller.params.venID = Ven4.venID
        controller.params.name = Ven4.name
        controller.params.clientURI = Ven4.clientURI
        controller.updateVEN()

        assert controller.response.redirectedUrl == '/ven/vens'
        assert controller.flash.message == "Success, your VEN has been updated"
        assert Ven.get(1).programs.contains(Program.get( 2 )) && Ven.get(1).programs.contains(Program.get( 3 ))
        assert Ven.get(1).venID == Ven4.venID
        assert Ven.get(1).name == Ven4.name
        assert Ven.get(1).clientURI == Ven4.clientURI

    }

    /**
     * 10: Test fail updateVEN
     */
    void testInvalidUpdateVEN() {
        def controller = new VenController()
        controller.params.id = 1
        def Ven = new Ven(venID:"VENFAIL", name:"ven-fail", clientURI:"http://URI")
        controller.params.programID = []
        controller.params.venID = Ven.venID
        controller.params.name = Ven.name
        controller.params.clientURI = Ven.clientURI
        controller.updateVEN()


        assert controller.response.redirectedUrl == '/ven/editVEN'
        assert controller.flash.message == "Please fix the errors below: "
        assert controller.flash.chainModel.currentVen.programs.isEmpty()
        assert controller.flash.chainModel.currentVen.venID == Ven.venID
        assert controller.flash.chainModel.currentVen.name == Ven.name
        assert controller.flash.chainModel.currentVen.clientURI == Ven.clientURI

    }

    /**
     * 11: Test deleteVEN
     */
    void testDeleteVEN() {
        def controller = new VenController()
        controller.params.id = 1
        controller.deleteVEN()

        assert Ven.get( 1 ) == null
        assert controller.response.redirectedUrl == '/ven/vens'

    }

}

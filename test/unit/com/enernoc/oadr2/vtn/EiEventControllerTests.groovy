package com.enernoc.oadr2.vtn

import grails.test.mixin.*

import org.junit.*

import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.OadrDistributeEvent

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(EiEventController)
//@Mock([EiEventService])
class EiEventControllerTests {

    void testPost() {
        def eiEventSvc = mockFor(EiEventService)
        eiEventSvc.demand.handleOadrPayload { Object payload ->
            return new OadrDistributeEvent()
                .withVtnID( "Hi" )
                .withRequestID( UUID.randomUUID().toString() )
                .withEiResponse( new EiResponse() )
        }
        controller.eiEventService = eiEventSvc.createMock()
        
        def requestBody = """<?xml version="1.0" encoding="UTF-8"?> 
<oadr:oadrRequestEvent xmlns:emix="http://docs.oasis-open.org/ns/emix/2011/06" 
  xmlns:ei="http://docs.oasis-open.org/ns/energyinterop/201110" 
  xmlns:pyld="http://docs.oasis-open.org/ns/energyinterop/201110/payloads" 
  xmlns:oadr="http://openadr.org/oadr-2.0a/2012/07" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <pyld:eiRequestEvent>
        <pyld:requestID>String</pyld:requestID>
        <ei:eventID>test-event-one</ei:eventID>
        <emix:marketContext>http://www.altova.com</emix:marketContext>
        <ei:venID>test-customer-one</ei:venID>
        <pyld:eventFilter>all</pyld:eventFilter>
        <pyld:replyLimit>0</pyld:replyLimit>
    </pyld:eiRequestEvent>
</oadr:oadrRequestEvent>
         """
        
        controller.metaClass.request = new InputStreamRequest( requestBody )
        controller.request.method = "POST"
        controller.request.addHeader "Accept", "application/xml"

        controller.post()
        eiEventSvc.verify()
        def resp = controller.response.xml
        
        assertEquals resp.name(), "oadrDistributeEvent" 
    }
}

package com.enernoc.open.oadr2.ven

import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrResponse
import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.OadrResponse
import com.enernoc.open.oadr2.model.ResponseCode

import com.enernoc.open.oadr2.xmpp.JAXBManager

/**
 * This serves as a fake VEN endpoint for HTTP push operations so
 * you can simulate HTTP pushes without standing up a VEN.
 * @author tnichols
 */
class DummyVenController {

    static allowedMethods = [push:"POST"]
    static defaultAction = 'push'
    
    JAXBManager jaxb
    
    public DummyVenController() {
        jaxb = new JAXBManager()
    }
    
    // TODO make a simple fake VEN console to change 
    // opt status & reply
    
    def push() {
        def venID = params.venID
        request.reader.mark()
        def payload = request.reader.text
        request.reader.reset()
        log.debug "------- DUMMY VEN HTTP PUSH to $venID:\n$payload"
        log.debug "--------------------------------------"
        
        def marshaller = jaxb.createMarshaller()
        def unmarshaller = jaxb.context.createUnmarshaller()
        
        def payloadObj = unmarshaller.unmarshal request.reader
        
        // assume it's a oadrDistributeEvent
        if ( payloadObj instanceof OadrDistributeEvent ) {
        
            def oadrResponse = new OadrResponse()
                .withEiResponse(new EiResponse()
                    .withRequestID(payloadObj.requestID)
                    .withResponseCode(new ResponseCode(200))
                    .withResponseDescription("Dummy response"))
                
            response.contentType = "application/xml"
            marshaller.marshal eiResponse, response.writer
        }
        else 
            log.warn "Unknown payload type ${payloadObj.class}"
    }
}

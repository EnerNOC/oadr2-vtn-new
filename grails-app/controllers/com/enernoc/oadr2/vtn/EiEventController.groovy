package com.enernoc.oadr2.vtn

import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

import com.enernoc.open.oadr2.xmpp.JAXBManager

/**
 * Controller to respond for OADR requests with an XML payload
 */
public class EiEventController {
    
//    static allowedMethods = [post:'POST']
    
    static JAXBManager jaxbManager // this is threadsafe
    
    static { // default initializer
        try {
            jaxbManager = new JAXBManager()
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating JAXB context", e)
        }
    }

    EiEventService eiEventService

    /**
     * Returns a Result object that will be returned via the PlayFramework
     * containing the payload based upon the incoming HTTP request
     * 
     * @return the Result to be rendered by PlayFramework 
     * @throws JAXBException
     */
    def post() throws JAXBException {
        try {
            // FIXME this logic, as well as auth, should go into a filter.
            // Unless we want all OADR services to be handled by a single controller
            def accept = request.getHeader("Accept") 
            if ( accept != 'application/xml' ) {
                log.warn "OADR service got unexpected Accept header: $accept"
                // TODO return error?
            }
            if ( request.contentType != 'application/xml' ) {
                log.warn "OADR service got unexpected Content-Type header: ${request.contentType}"
                // TODO return error?
            }
            
            Unmarshaller unmarshaller = jaxbManager.context.createUnmarshaller()
            Object payload = unmarshaller.unmarshal( request.reader )
            def eiResponse = eiEventService.handleOadrPayload(payload)
            Marshaller marshaller = jaxbManager.createMarshaller()
            response.contentType = "application/xml"
            marshaller.marshal eiResponse, response.outputStream
            response.outputStream.flush()
            return null
        }
        catch ( Exception e ) {
            log.error "Unexpected exception in EiEventController", e
            // TODO error handling, always return a valid XML payload.
            render status: 500, contentType: "text/plain", text: "${e.class}: ${e.message}" 
        }
    }
    
    def index() {
        render status: 406, contentType: "text/plain", 
            text: "You must POST an oadrRequestEvent or oadrCreatedEvent"
    }
}

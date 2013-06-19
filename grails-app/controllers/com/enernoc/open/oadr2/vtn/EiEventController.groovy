package com.enernoc.open.oadr2.vtn

import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

import com.enernoc.open.oadr2.xmpp.JAXBManager



/**
 * Controller class to handle HTTP Requests
 *
 * @authors Thom Nichols, Yang Xiang
 *
 */

public class EiEventController {
    
    boolean debug = true
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
     * handles a HTTP POST 
     * unmarshalls and re-marshalls xml
     * @return null 
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
            def rawPayload = request.reader
            if ( debug ) {
                rawPayload = request.reader.text
                log.debug "<<< $rawPayload"
                rawPayload = new StringReader(rawPayload)
            }
            Object payload = unmarshaller.unmarshal( rawPayload )
            def eiResponse = eiEventService.handleOadrPayload(payload)
            Marshaller marshaller = jaxbManager.createMarshaller()
            response.contentType = "application/xml"
            
            def out = new StringWriter() 
            marshaller.marshal eiResponse, out
            out = out.toString()
            if ( debug ) {
                log.debug ">>> $out"
            }
            
            response.contentLength = out.length()
            response << out
            return null
        }
        catch ( Exception e ) {
            log.error "Unexpected exception in EiEventController", e
            // TODO error handling, always return a valid XML payload.
            render status: 500, contentType: "text/plain", text: "${e.class}: ${e.message}" 
        }
    }
    
    /**
     * Default render if user attempts to access the URL path to controller
     * 
     * @return render text
     */
    def index() {
        render status: 406, contentType: "text/plain", 
            text: "You must POST an oadrRequestEvent or oadrCreatedEvent"
    }
}

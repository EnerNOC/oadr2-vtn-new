package com.enernoc.open.oadr2.vtn

import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import com.enernoc.open.oadr2.xmpp.JAXBManager

import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrResponse

/**
 * VenResponse Bot to mock a Ven response after a 
 * EiEventPush
 * @author Yang Xiang
 *
 */
class VenResponseBotController {
    
    
    static JAXBManager jaxbManager // this is threadsafe
    
    static { // default initializer
        try {
            jaxbManager = new JAXBManager()
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating JAXB context", e)
        }
    }

    EiEventService eiEventService

    def index() {
        render "I AM A BOT"
    }
    /**
     * handles a HTTP POST
     * unmarshalls and re-marshalls xml
     * @return null
     * @throws JAXBException
     */
    def httpResponse() throws JAXBException {
        log.debug "httpResponse"
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
            if (payload instanceof OadrDistributeEvent) {
                def oadrDistribute = payload as OadrDistributeEvent
                def eiResponse = new OadrResponse()
                    .withEiResponse( payload.eiResponse)
                Marshaller marshaller = jaxbManager.createMarshaller()
                response.contentType = "application/xml"
                marshaller.marshal eiResponse, response.outputStream
                response.outputStream.flush()
            }
            else
                log.error "OadrDistributeEvent expected"
            return null
        }
        catch ( Exception e ) {
            log.error "Unexpected exception in EiEventController", e
            // TODO error handling, always return a valid XML payload.
            render status: 500, contentType: "text/plain", text: "${e.class}: ${e.message}"
        }
    }

    /**
     * handles a XMPP Post
     * 
     */
    def xmppResponse() {
        //TODO implement xmppResponse
    }
}

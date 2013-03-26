package com.enernoc.oadr2.vtn;

import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

import com.enernoc.open.oadr2.xmpp.JAXBManager

/**
 * Controller to respond for OADR requests with an XML payload
 * @author jlajoie
 */
public class EiEventController {
    
    JAXBManager jaxbManager
    {
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
    def handle() throws JAXBException {
        Unmarshaller unmarshaller = jaxbManager.getContext().createUnmarshaller()
        Object payload = unmarshaller.unmarshal( request.inputStream )
		// TODO handle unexpected errors gracefully, always return an XML response
        Object eiResponse = eiEventService.handleOadrPayload(payload)
        Marshaller marshaller = jaxbManager.createMarshaller()
        response.contentType = "application/xml"
        marshaller.marshal eiResponse, response.outputStream
    }    
}

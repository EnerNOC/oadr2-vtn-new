package org.enernoc.oadr2.vtn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.enernoc.open.oadr2.xmpp.JAXBManager
import org.apache.commons.logging.LogFactory

/**
 * Controller to respond for OADR requests with an XML payload
 * @author jlajoie
 *
 */
public class EiEventController{
    
	private static final log = LogFactory.getLog(this)
	
    static JAXBManager jaxbManager;
    static{
        try {
            jaxbManager = new JAXBManager("org.enernoc.open.oadr2.model");
        } catch (JAXBException e) {
            log.error("Could not initialize JAXBManager in EiEvents", e);
        }
    }
    
    static EiEventService eiEventService = EiEventService.getInstance();

    /**
     * Returns a Result object that will be returned via the PlayFramework
     * containing the payload based upon the incoming HTTP request
     * 
     * @return the Result to be rendered by PlayFramework 
     * @throws JAXBException
     */
    //@Transactional
    def sendHttpResponse() throws JAXBException{
        Unmarshaller unmarshaller = jaxbManager.getContext().createUnmarshaller();
        Object payload = unmarshaller.unmarshal(request.inputStream);
        Object eiResponse = eiEventService.handleOadrPayload(payload);
        Marshaller marshaller = jaxbManager.createMarshaller();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(eiResponse, outputStream);
        response.setContentType("application/xml");
        render(outputStream.toByteArray());
    }
    
    
}

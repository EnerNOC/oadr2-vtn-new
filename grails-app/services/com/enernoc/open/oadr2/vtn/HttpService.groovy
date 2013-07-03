package com.enernoc.open.oadr2.vtn

import javax.xml.bind.Marshaller

import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType

import com.enernoc.open.oadr2.model.OadrResponse
import com.enernoc.open.oadr2.xmpp.JAXBManager
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

/**
 * Sends "Push" OpenADR payloads via HTTP
 * @author tnichols
 */
public class HttpService {
    
    EiEventService eiEventService
    Marshaller marshaller
    Unmarshaller unmarshaller
    String URI
    
    public HttpService() {
        JAXBManager jaxb = new JAXBManager()
        this.marshaller = jaxb.createMarshaller()
        this.unmarshaller = jaxb.context.createUnmarshaller()
    }

    void send( Object payload, String uri ) {
        log.debug "HttpService PUSH to $uri"
        StringWriter sw = new StringWriter()
        this.marshaller.marshal(payload, sw) //TODO make sure this is threadsafe
        // TODO client cert
        
        try {
            Request.Post(uri)
                    .bodyString(sw.toString(), ContentType.APPLICATION_XML)
                    .execute()
                    .handleResponse( { HttpResponse resp -> 
                        // need to pass request URI
                        this.handleResponse uri, resp
                    } as ResponseHandler )
        }
        catch ( ex ) {
            // TODO handle error in eiEventService
            log.error "HTTP PUSH Error to $uri", ex
        }
    }

    Object handleResponse( String uri, HttpResponse resp ) {
        Object payload = unmarshaller.unmarshal( resp.entity.content )
        log.debug "HTTP PUSH response: $payload"
        
        if ( payload instanceof OadrResponse ) {
            eiEventService.handleOadrResponse( (OadrResponse) payload, uri )
        }
        else
            log.error "OadrResponse payload is expected"
    }
}

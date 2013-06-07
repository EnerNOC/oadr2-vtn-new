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
public class HttpService implements ResponseHandler {
    def eiEventService = new EiEventService()
    Marshaller marshaller
    Unmarshaller unmarshaller
    String URI
    public HttpService() {
        JAXBManager jaxb = new JAXBManager()
        this.marshaller = jaxb.createMarshaller()
        this.unmarshaller = jaxb.context.createUnmarshaller()
    }

    void send( Object payload, String uri ) {
        log.debug "HttpService Send"
        StringWriter sw = new StringWriter()
        this.marshaller.marshal(payload, sw)
        this.URI = uri //TODO make sure this is threadsafe
        // TODO client cert
        Request.Post(uri)
                .bodyString(sw.toString(), ContentType.APPLICATION_XML)
                .execute().handleResponse( this )
    }

    Object handleResponse( HttpResponse resp ) {
        Object payload = unmarshaller.unmarshal( resp.entity.content )
        if ( payload instanceof OadrResponse) {
            log.debug "RESPONSE IS BEING HANDLED"
            eiEventService.handleOadrResponse( (OadrResponse) payload, URI )
        }
        else
            log.error "OadrResponse payload is expected"
        log.info "Response $resp sent"
    }
}

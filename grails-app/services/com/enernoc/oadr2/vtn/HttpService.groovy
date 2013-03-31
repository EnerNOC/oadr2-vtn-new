package com.enernoc.oadr2.vtn

import javax.xml.bind.Marshaller

import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType

import com.enernoc.open.oadr2.xmpp.JAXBManager

/**
 * Sends "Push" OpenADR payloads via HTTP
 * @author tnichols
 */
public class HttpService implements ResponseHandler {
    Marshaller marshaller

    public HttpService() {
        JAXBManager jaxb = new JAXBManager()
        this.marshaller = jaxb.createMarshaller()
    }

    void send( Object payload, String uri ) {
        StringWriter sw = new StringWriter()
        this.marshaller.marshal(payload, sw)
        // TODO client cert
        Request.Post(uri)
                .bodyString(sw.toString(), ContentType.APPLICATION_XML)
                .execute().handleResponse( this )
    }

    Object handleResponse( HttpResponse resp ) {
        log.debug "Response: $resp"
    }
}

package protocol;

import javax.persistence.Embeddable;

import models.VEN;

import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.OadrCreatedEvent;
import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.enernoc.open.oadr2.model.OadrRequestEvent;
import org.enernoc.open.oadr2.model.OadrResponse;

/**
 * IProtocol is the interface for the BaseProtocol class
 * forming the contract of requiring the Protocols to have certain send methods
 * 
 * @author Jeff LaJoie
 *
 */
@Embeddable
public interface IProtocol {
    
    public enum ProtocolType{ XMPP, HTTP }
    //change VEN to a String URI
    public void send(String uri, OadrResponse oadrResponse, String pid);
    public void send(String uri, EiEvent eiEvent);
    public void send(String uri, OadrDistributeEvent oadrDistributeEvent);
    public void send(String uri, OadrCreatedEvent oadrCreatedEvent);
    public void send(String uri, OadrRequestEvent oadrRequestEvent);
    public void send(String uri, Object oadrObject);

}
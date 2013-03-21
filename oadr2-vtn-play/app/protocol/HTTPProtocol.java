package protocol;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.OadrCreatedEvent;
import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.enernoc.open.oadr2.model.OadrRequestEvent;
import org.enernoc.open.oadr2.model.OadrResponse;

import play.Logger;
import service.XmppService;

/**
 * HTTPProtocol on how to handle the Push portion of HTTP
 * 
 * @author Jeff LaJoie
 *
 */
public class HTTPProtocol extends BaseProtocol{
    
    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Events");
    static EntityManager entityManager = entityManagerFactory.createEntityManager();
    static XmppService xmppService = XmppService.getInstance();

    public HTTPProtocol(){
        this.setProtocolType(ProtocolType.HTTP);
    }

    @Override
    public void send(String uri, OadrResponse oadrResponse, String pid) {
    }

    @Override
    public void send(String uri, EiEvent eiEvent) {
        
    }

    @Override
    public void send(String uri, OadrDistributeEvent oadrDistributeEvent) {
        
    }

    @Override
    public void send(String uri, OadrCreatedEvent oadrCreatedEvent) {
        
    }

    @Override
    public void send(String uri, OadrRequestEvent oadrRequestEvent) {
        
    }
    
    @Override
    public void send(String uri, Object oadrObject){
    }

}
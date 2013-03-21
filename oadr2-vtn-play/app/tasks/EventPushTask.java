package tasks;

import org.enernoc.open.oadr2.model.OadrResponse;

import protocol.IProtocol;
import protocol.ProtocolRegistry;
import service.XmppService;

/**
 * Runnable task to be submitted to the PushService
 * 
 * @author Jeff LaJoie
 *
 */
public class EventPushTask implements Runnable{
    

    Object oadrObject = null;
    String uri = null;
    String pid = null;
    
    XmppService xmppService = XmppService.getInstance();
    ProtocolRegistry protocolRegistry = ProtocolRegistry.getInstance();
    
    public EventPushTask(String uri, Object oadrObject){
        this.oadrObject = oadrObject;
        this.uri = uri;
    }

    /**
     * Called when the Runnable is executed by the thread pool,
     * sends the object to the jid w/ or w/o a packet id
     */
    @Override
    public void run() {
        IProtocol protocol = protocolRegistry.getProtocol(uri);
        if(pid != null){
            protocol.send(uri, (OadrResponse)oadrObject, pid);
        }
        else{
            protocol.send(uri, oadrObject);
        }
    }

}

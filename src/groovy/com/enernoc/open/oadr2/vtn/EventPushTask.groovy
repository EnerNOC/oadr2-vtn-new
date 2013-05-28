package com.enernoc.open.oadr2.vtn


/**
 * Runnable task to be submitted to the PushService
 */
public class EventPushTask implements Runnable {
    
    Object payload
    String uri
    PushService pushService
    
    public EventPushTask(String uri, Object payload, PushService pushService ){
        this.payload = payload
        this.uri = uri
        this.pushService = pushService
    }

    /**
     * Called when the Runnable is executed by the thread pool,
     * sends the object to the jid w/ or w/o a packet id
     */
    @Override
    public void run() {
        URI parsed = new URI( this.uri );
        
        if ( parsed.scheme in ["http", "https"] )
            this.pushService.httpService.send( this.payload, this.uri )
            
        else if ( parsed.scheme == "xmpp" )
            this.pushService.xmppService.send( this.payload, this.uri ) 
    }
}

package com.enernoc.open.oadr2.vtn

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import javax.xml.bind.JAXBException

import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.ResponseCode
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent
import com.enernoc.open.oadr2.vtn.EventPushTask

/**
 * Establish a thread pool for asynchronous sending of push events.
 * TODO make this configurable; define an interface so a true queueing
 * mechanism can be used.
 * 
 * @author Thom Nichols
 */
public class PushService {

    static transactional = true

    String vtnID // injected

    final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>()
    ThreadPoolExecutor threadPool = null

    public PushService() {
        this.threadPool = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS, queue)
        threadPool.prestartAllCoreThreads()
    }

    /**
     * Takes the event and VENs to send the event to and adds them to the already running 
     * thread pool of the PushService class
     * 
     * @param e - Event to be sent
     * @param vens - VENs to receive the event
     * @throws JAXBException - thrown if an error occurs marhsalling/unmarhsalling an OADR object
     */
    public void pushNewEvent( EiEvent e, List<Ven> vens ) throws JAXBException{
        vens.each { ven ->
            OadrDistributeEvent payload = new OadrDistributeEvent()

            payload.withVtnID( this.vtnID )
                    .withRequestID( UUID.randomUUID().toString() )
                    .withOadrEvents(new OadrEvent().withEiEvent(e))

            queue.add( new EventPushTask( ven.clientURI, payload ) )
        }
    }

}

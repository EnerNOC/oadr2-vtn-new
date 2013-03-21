package service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import models.VEN;

import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.EiResponse;
import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.enernoc.open.oadr2.model.ResponseCode;
import org.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent;

import play.db.jpa.Transactional;

import tasks.EventPushTask;

/**
 * Establish a thread pool for asynchronous sending of push events
 * 
 * @author Jeff LaJoie
 *
 */
public class PushService{
    
    final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    ThreadPoolExecutor threadPool = null;    
    
    public PushService(){        
        threadPool = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS, queue);    
        threadPool.prestartAllCoreThreads();
    }
    
    /**
     * Takes the event and VENs to send the event to and adds them to the already running 
     * thread pool of the PushService class
     * 
     * @param e - Event to be sent
     * @param vens - VENs to receive the event
     * @throws JAXBException - thrown if an error occurs marhsalling/unmarhsalling an OADR object
     */
    @Transactional
    public void pushNewEvent(EiEvent e, List<VEN> vens) throws JAXBException{       
        for(VEN v : vens){
            OadrDistributeEvent distribute = new OadrDistributeEvent()
            .withOadrEvents(new OadrEvent().withEiEvent(e));
            
            distribute.setEiResponse(new EiResponse().withRequestID("Request ID")
                    .withResponseCode(new ResponseCode("200"))
                    .withResponseDescription("Response Description"));
            distribute.getOadrEvents().add(new OadrEvent().withEiEvent(e));
            distribute.setRequestID("Request ID");
            distribute.setVtnID("VTN ID");
           queue.add(new EventPushTask(v.getClientURI(), distribute));     
        }
    }

}

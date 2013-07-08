package com.enernoc.open.oadr2.vtn

import grails.converters.JSON

import javax.xml.bind.JAXBException

import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiResponse
import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.ResponseCode
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent

/**
 * RabbitMQ worker that enqueues OADR payloads when an event 
 * needs to be pushed to VENs
 * 
 * @author Thom Nichols
 */
public class EventDistributeService {

    static transactional = true
    static rabbitQueue = 'oadr.push.event'
    
    def payloadPushService

    def eventChanged( eventID ) {
        log.debug "+++++ Enqueueing Event ${eventID} for push +++++"
        rabbitSend rabbitQueue, eventID
    } 
    
    /**
     * msg should be an event ID, from which we generate the OADR payloads
     * for VENs that require push
     * @param eventID
     * @throws JAXBException
     */
    public void handleMessage(long eventID) throws JAXBException {
        Event.withSession {
            def event = Event.get(eventID)
            if ( ! event ) {
                log.error "Unknown event ID in $rabbitQueue queue: $eventID"
                return
            }
            
            def vens = event.program.vens 
/*            def vens = Ven.executeQuery('''select v from Event e, Ven v 
                where ? in elements(v.programs) 
                      and v.clientURI is not null''', event.program)
*/          
            
            vens.each { ven ->
                if ( ! ven.clientURI ) return // TODO this is inefficient, use query above
                
                Ven.withTransaction { txn ->
                    try {
                        prepareVenStatus event, ven
                        payloadPushService.enqueueEventForVen event, ven
                    }
                    catch ( ex ) {
                        log.warn "Error pushing event ${event} to VEN ${ven} : $ex", ex
                        txn.setRollbackOnly()
                    }
                }
            }
        }
    }
    
    /**
     * Prepares the VENs by creating a VENStatus object for each and setting the OptStatus to Pending 1
     *
     * @param v - List of VENs to be traversed and will be used to construct a VENStatus object
     * @param event - Event containing the EventID which will be used for construction of a VENStatus object
     */
    protected void prepareVenStatus( Event event, Ven v) {
        
        def venStatus = new VenStatus()
        venStatus.optStatus = StatusCode.PENDING_DISTRIBUTE
        event.addToVenStatuses venStatus
        v.addToVenStatuses venStatus
        venStatus.time = new Date()
        if ( venStatus.validate() ) {
            v.save()
            log.debug "Created new VenStatus for Event: ${event.eventID}, VEN: ${v.venID}"
        }
        // TODO raise exception if VenStatus couldn't be created!
        else log.warn "Validation error for venStatus ${venStatus}"
    }
}

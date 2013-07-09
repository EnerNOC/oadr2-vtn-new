package com.enernoc.open.oadr2.vtn

import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent
import com.enernoc.open.oadr2.model.ResponseRequiredType;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonException

class PayloadPushService {

    static transactional = true
    static rabbitQueue = 'oadr.push.payload'
    
    // injected:
    EiEventService eiEventService
    HttpService httpService
    XmppService xmppService
    
    /**
     * called by clients who want to enqueue an event to be
     * pushed to the given VEN
     *
     * @param e - Event to be sent
     * @param vens - VENs to receive the event
     */
    public void enqueueEventForVen( Event event, Ven ven ) {
        if ( ! ven.clientURI ) {
            log.error "ven $ven should not be pushed"
            return
        }
        
        def payload = new JsonBuilder()
        payload.call {
            type "event"
            eventID event.id
            venID ven.id
        }
        rabbitSend rabbitQueue, payload.toString()
    }
    
    /**
     * Dequeue a message, grab the URI and payload, and send it!
     */
    public void handleMessage(String payload) {
        
        try {
            def msg = new JsonSlurper().parseText(payload)
            
            Event.withTransaction { txn ->
                
                switch ( msg.type ) {
                    case 'event':
                        handleDistributeEvent msg
                        break;
                    default:
                        log.warn "Unknown message type! $msg"
                }
            }
        }
        catch ( JsonException ex ) {
            log.error "Error parsing json from dequeued payload: $payload", ex
        }
        
    }
    
    protected void handleDistributeEvent( msg ) {
        def ven = Ven.get( msg.venID )
        def event = Event.get( msg.eventID )
        if ( ! ven ) {
            log.error "Unknown VEN ${msg.venID}!"
            return
        }
        if ( ! event ) {
            log.error "Unknown event ${msg.eventID}!"
            return
        }
        
        log.debug "Sending event $event to VEN: $ven"
        
        // TODO currently all VENs get pushed the same event payload:
        def payload = eiEventService.buildDistributeEvent( event )

        def venStatus = VenStatus.findWhere( event: event, ven: ven  )
        
        def venLog = new VenTransactionLog()
        venLog.venID = ven.venID
        venLog.uri = ven.clientURI
        venLog.type = 'push_request'
        venLog.request = payload.toString()
        venLog.requestID = payload.requestID

        try {
            this.send payload, ven.clientURI
            
            venStatus.optStatus = StatusCode.DISTRIBUTE_SENT
            log.debug "Event pushed to VEN: ${ven.clientURI}"
        }
        catch ( ex ) {
            log.error "Error sending payload to ven ID ${ven.id} (URI: ${ven.clientURI}): ${ex.message}"
            venLog.error = ex.message
            venStatus.optStatus = StatusCode.DISTRIBUTE_ERROR
            // TODO update venLog
        }
        
        venStatus.save()
        venLog.save()
    }
        
    protected send( payload, uri ) {        
        URI parsed = new URI( uri );
        if ( parsed.scheme in ["http", "https"] )
            httpService.send payload, uri
        else if ( parsed.scheme == "xmpp" )
            xmppService.send payload, uri
    }

}

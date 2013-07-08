package com.enernoc.open.oadr2.vtn

import com.enernoc.open.oadr2.model.OadrDistributeEvent
import com.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class PayloadPushService {

    static transactional = true
    static rabbitQueue = 'oadr.push.payload'
    
    String vtnID // injected
    def eiEventService
    
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
        
        rabbitSend(
            rabbitQueue,
            new JsonBuilder().call {
                type "event"
                eventID event.id
                venID ven.id
            }.toString()
        )
    }
    
    /**
     * Dequeue a message, grab the URI and payload, and send it!
     */
    public void handleMessage(String payload) {
        
        def msg = new JsonSlurper().parseText(payload)
        
        switch ( msg.type ) {
            case 'event':
                handleDistributeEvent msg
                break;
            default:
                log.warn "Unknown message type! $msg"
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
        def eiEvent = eiEventService.buildEiEvent(event)

        def payload = new OadrDistributeEvent()
                .withVtnID( this.vtnID )
                .withRequestID( UUID.randomUUID().toString() )
                .withOadrEvents( 
                    new OadrEvent( eiEvent ) )

        def venStatus = VenStatus.findWhere( event: event, ven: ven  )
        
        def venLog = new VenTransactionLog()
        venLog.venID = ven.venID
        venLog.uri = ven.clientURI
        venLog.type = 'push_request'
        venLog.body = payload.toString() // TODO
        venLog.requestID = payload.requestID

        try {
            this.send payload, ven.clientURI
            
            venStatus.optStatus = StatusCode.DISTRIBUTE_SENT
            log.debug "Event pushed to VEN: ${ven.clientURI}"
        }
        catch ( ex ) {
            log.e "Error sending payload to ven ID ${ven.id} (URI: ${ven.clientURI}): ${ex.message}"
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

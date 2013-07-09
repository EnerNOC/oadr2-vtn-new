package com.enernoc.open.oadr2.vtn

import javax.xml.bind.DatatypeConverter

class EventSignalController {

    def eventDistributeService
    
    static defaultAction = 'edit'
    static allowedMethods = [edit:'GET', update:'POST',delete:"POST"]
    
    def edit() {
        def eventID = params.eventID
        def event = Event.findById(eventID, [fetch:[signals:'select']])
        if ( ! event ) {
            response.sendError 404, "No Event for ID $eventID"
            return 
        }
        log.info "EVENT: $event"
        event.signals.each {
            log.info "Signal: ${it?.class}, $it"
        }
        
        if ( ! event.signals ) {
            event.signals << new EventSignal(
                intervals: [ new EventInterval(
                    durationMillis : event.endDate.time - event.startDate.time,
                    level: 1.0F    
                )]
            )
            event.discard() // prevent from causing a save error.
        }

        return [
            event: event, 
            eventJSON: [
                id : event.id,
                start : event.startDate,
                end : event.endDate,
                signals : event.signals.collect { sig -> 
                    def end = event.startDate.time
                    
                    return [
                        id : sig.id,
                        name : sig.name,
                        type : sig.type.toString(),
                        intervals : sig.intervals.collect {
                            end += it.durationMillis
                            return [
                                id: it.id,
                                value: it.level,
                                duration : it.durationMillis,
                                endTime : end
                            ]
                        }
                    ]
                }
            ]
        ]   
    }
    
    
    def update() {
        def data = request.JSON
        def eventID = params.eventID
        log.debug "Creating signals: $data for $eventID"
        
        def event = Event.findById eventID, [fetch:[signals:'select']]
        this.parseEventSignals( data, event ).each {
            event.addToSignals it
        }
        
        if ( event.signals.any { it.id != -1 } )  // if this is an update, not a save
            event.modificationNumber +=1
        
        if ( event.validate() ) { //signals.every { it.validate() } ) {
            
            /* event must be flushed before messages are enqueued, otherwise
             * workers will get a stale event object
             */
            event.save flush: true 
            eventDistributeService.eventChanged event.id
            
            log.debug "Event $event saved with ${event.signals.size()} signals"
            flash.message = "Saved event signals"
            render( contentType: 'text/json' ) {
                location = g.createLink( controller: 'event', id: eventID)
                msg = "OK"
            }
        }
        
        else { // validation error; render JSON error response:
            
            render( contentType: 'text/json', status: 406 ) {
                log.warn "Signal validation errors: ${event.errors}"
                msg = "validation error: ${event.errors}"
            }
        }
    }
    
    def delete() {
        def signal = EventSignal.get(params.id) 
        if ( signal ) {
            signal.delete()
            // FIXME event should be marked as "dirty" to be pushed to VENs;
            // increment event modification number
            render( contentType: 'text/json' ) { msg = "OK" } 
            return
        }
        
        render( contentType: 'text/json', code: 404 ) {
            msg = "Could not find signal ID ${params.id}"
        }
    }
    
    /**
     * Request data is a JSON array that looks like:
     * <code>
     * [
     *   { "name":"simple",
     *     "type":"LEVEL",
     *     "id":2,
     *     "intervals":[
     *       { "end":"2013-05-10T16:56:00.000Z",
     *         "val":"1","id":0},
     *       { "end":"2013-05-10T17:56:00.000Z",
     *         "val":"2","id":1}
     *     ]
     *   }
     * ]
     * </code>
     * @param requestData
     * @return
     */
    protected parseEventSignals( signals, event ) {
        return signals.collect { sigData ->
            def sigID = sigData['id']
            def signal = ( sigID ) ? EventSignal.get( sigID ) : new EventSignal()
            
            signal.name = sigData['name']
            signal.type = SignalType.valueOf( sigData['type'] )

            def lastIntervalEnd = event.startDate
            signal.intervals = sigData['intervals'].collect { intervalData ->
                def intervalID = intervalData['id']
                def interval = intervalID ? EventInterval.get( intervalID ) : new EventInterval()
                
                def endDt = DatatypeConverter.parseDateTime( intervalData['end'] ).time 
                interval.durationMillis = endDt.time - lastIntervalEnd.time
                interval.level = intervalData['val'] as float
                
                lastIntervalEnd = endDt
                
                interval.signal = signal
                interval
            }
            
            signal 
        }
    }
}
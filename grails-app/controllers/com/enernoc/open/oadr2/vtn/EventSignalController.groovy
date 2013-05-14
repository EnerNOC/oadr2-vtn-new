package com.enernoc.open.oadr2.vtn

import javax.xml.bind.DatatypeConverter

class EventSignalController {

    def pushService
    def eiEventService
    
    static defaultAction = 'edit'
    static allowedMethods = [edit:'GET', update:'POST']
    
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
                                level: it.level,
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
        event.signals = this.parseEventSignals data, event
        
        if ( event.signals.every { it.validate() } ) {
            event.save flush:true // TODO update modification number?
            def eiEvent = eiEventService.buildEiEvent event
            pushService.pushNewEvent eiEvent, event.program.vens.collect { it }
            
            flash.message = "Saved event signals"
            render( contentType: 'text/json' ) {
                location = g.createLink( controller: 'event', id: eventID)
                msg = "OK"
            }
        }
        
        else { // validation error; render JSON error response:
            
            render( contentType: 'text/json', statusCode: 406 ) {
                msg = "validation error: ${event.errors}"
            }
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
     *         "level":"1","id":0},
     *       { "end":"2013-05-10T17:56:00.000Z",
     *         "level":"2","id":1}
     *     ]
     *   }
     * ]
     * </code>
     * @param requestData
     * @return
     */
    protected parseEventSignals( signals, event ) {
        Thread.sleep( 3000 )
        return signals.collect { sigData ->
            def sigID = sigData['id']
            def signal = ( sigID ) ? EventSignal.get( sigID ) : new EventSignal()
            
            signal.name = sigData['name']
            signal.type = SignalType.valueOf( sigData['type'] )
            signal.event = event

            def lastIntervalEnd = event.startDate
            signal.intervals = sigData['intervals'].collect { intervalData ->
                def intervalID = intervalData['id']
                def interval = intervalID ? EventInterval.get( intervalID ) : new EventInterval()
                
                def endDt = DatatypeConverter.parseDateTime( intervalData['end'] ).time 
                interval.durationMillis = endDt.time - lastIntervalEnd.time
                interval.level = intervalData['level'] as float
                
                lastIntervalEnd = endDt
                
                interval.signal = signal
                interval
            }
            
            signal 
        }
    }
}
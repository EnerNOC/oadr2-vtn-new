package com.enernoc.open.oadr2.vtn

import grails.converters.JSON
import org.hibernate.FetchMode as FM

class EventSignalController {

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
        
        def event = Event.findById(eventID, [fetch:[signals:'select']])
        
        pushService.pushNewEvent eiEvent, event.program.vens.collect { it }
        
        flash.message = "Saved event signals"
        render( contentType: 'text/json' ) {
            location = g.createLink( controller: 'event', id: eventID)
            msg = "OK"
        }
    }
}

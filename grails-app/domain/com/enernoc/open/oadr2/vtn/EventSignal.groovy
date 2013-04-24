package com.enernoc.open.oadr2.vtn

import javax.xml.datatype.Duration;

class EventSignal {

    static belongsTo = [event: Event]
    static hasMany = [intervals: EventInterval]
    static fetchMode = [intervals:'eager']
    
    String signalID = UUID.randomUUID().toString()
    String name = "simple"
    SignalType type = SignalType.LEVEL
    
    static constraints = {
        event nullable: false
        signalID blank: false 
        name blank: false
        type nullable: false
        intervals validator: EventSignal.&validateIntervals
    }
    
    public EventSignal getCurrentValue() {
        Date now = new Date()
        
        // TODO not sure if this is correct:
        if ( event.cancelled || now > event.endDate )
            return null
        
        if  ( now < event.startDate )
            return null
        
        // at this point assume we're somewhere inside the event window
        def intervalEnd = event.startDate.time
        for ( EventInterval interval : event.intervals ) {
            intervalEnd += interval.durationMillis
            if ( intervalEnd > now.time ) // we're in this interval
                return interval.level
        }
        log.warn "Couldn't find an interval for event $event"
        return null
    }
    
    static validateIntervals( intervals, EventSignal signal ) {
      if ( intervals.size() < 1 ) return false
      def eventLength = signal.event.durationMillis
      
      def intervalDuration = 0
      intervals.each { intervalDuration += it.durationMillis }
      return eventLength == intervalDuration
    } 
}



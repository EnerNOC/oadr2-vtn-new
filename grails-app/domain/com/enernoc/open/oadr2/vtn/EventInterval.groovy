package com.enernoc.open.oadr2.vtn

import javax.xml.datatype.Duration;

class EventInterval {

    static belongsTo = [signal: EventSignal]
    
    long durationMillis
    String uid = UUID.randomUUID().toString()
    float level
    
    static constraints = {
        signal nullable: false 
        uid blank: false
        durationMillis min: 0L
        level validator: { val, interval ->
            switch ( interval.signal.type ) {
                case SignalType.LEVEL:
                    return ((int)val) == val && (int)val in 0..5
                default:
                    return true
            }
        }
    }
    
    Duration getDuration() {
        // TODO normalize
        this.signal.event.dtf.newDuration this.durationMillis
    }
}



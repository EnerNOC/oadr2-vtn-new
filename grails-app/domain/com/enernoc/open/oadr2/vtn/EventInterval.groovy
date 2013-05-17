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
            if ( interval.signal.type == SignalType.LEVEL ) {
                if ( ((int)val) != val ) return "notinteger"  
                if ( ! (int)val in 0..3 ) return "range"
            }
        }
    }
    
    Duration getDuration() {
        // TODO normalize
        this.signal.event.dtf.newDuration this.durationMillis
    }
}



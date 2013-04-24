package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(EventSignal)
class EventSignalTests {

    void testValidation() {
        mockForConstraintsTests Event
        mockForConstraintsTests EventSignal
        mockForConstraintsTests EventInterval
        
        Event e = new Event( 
            name: "hi",
            signals: [
                new EventSignal(
                    name: 'simple',
                    type: SignalType.LEVEL,
                    intervals: [
                        new EventInterval(
                            level: 1.0F,
                            durationMillis: 1000
                        )
                    ]
                )
            ]
        )
        
        assertTrue e.signals.all { it.validate() }
    }
}

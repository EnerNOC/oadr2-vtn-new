package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin;

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(EventSignal)
@TestMixin(DomainClassUnitTestMixin)
class EventSignalTests {

    void testValidation() {
        mockDomain Event
        mockDomain EventSignal
        mockForConstraintsTests EventInterval
        
        def start = new Date()
        def end = start + 1
        def duration = end.time - start.time
        
        def event = new Event( 
            name: "hi",
            startDate: start,
            endDate: end
        )
        
        def signal = new EventSignal(
            name: 'simple',
            type: SignalType.LEVEL
        )
        
        event.addToSignals signal 
        
        def interval = new EventInterval(
                level: 1.0F,
                durationMillis: duration,
                signal: signal
            )
        signal.intervals = [interval]
        
        assert event.signals.size()
        
        def errors = event.signals.collect { 
            it.validate() ? null : it.errors
        }
        assert errors.every { it == null }, "Signal did not validate: $errors"
    }
}

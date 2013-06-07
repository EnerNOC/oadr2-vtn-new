package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(EventSignal)
class EventIntervalTests {

    void testValidation() {
        mockForConstraintsTests EventSignal
        mockForConstraintsTests EventInterval
        
        def signal = new EventSignal(type:SignalType.LEVEL) 
        
        def interval = new EventInterval(
            level: 1,
            durationMillis: 1000,
            signal: signal
        )
        
        assert interval.validate(), "Interval did not validate: ${interval.errors}"
        
        interval.level = 6
        assert interval.validate(), "Interval level range validation should have failed: ${interval.errors}"
        
        interval.level = 1.1
        assert !interval.validate(), "Interval level float validation should have failed: ${interval.errors}"
        
        signal.type = SignalType.PRICE_RELATIVE
        assert interval.validate(), "Price level validation should not have failed: ${interval.errors}"
    }
}

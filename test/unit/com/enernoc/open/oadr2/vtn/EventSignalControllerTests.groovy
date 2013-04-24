package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(EventSignalController)
class EventSignalControllerTests {

    void testEdit() {
        Event e = new Event( 
            eventID: "hi",
            startDate: new Date(),
            endDate: new Date() + 1,
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
        ).save()
        
        def controller = new EventSignalController()
        
        controller.params = [ id: 1]
        def model = controller.edit()
    }
}

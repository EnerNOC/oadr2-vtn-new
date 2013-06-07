package com.enernoc.open.oadr2.vtn

import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin;
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(EventSignalController)
@TestMixin(DomainClassUnitTestMixin)
class EventSignalControllerTests {

    def event 
    
    @Before
    void setUp() {
        mockDomain Program
        mockDomain Event
        mockDomain EventSignal
        mockDomain EventInterval
        
        def start = new Date()
        def end = start + 1
        def duration = end.time - start.time
        
        def program = new Program(
            name : "FCM",
            marketContext : "http://www.enernoc.com/OpenADR/FCM"
        )
        
        def event = new Event(
            eventID: "hi",
            program: program,
            startDate: start,
            endDate: end
        )
        
        def signal = new EventSignal(
            name: 'simple',
            type: SignalType.LEVEL,
            event: event
        )
        
        def interval = new EventInterval(
                level: 1.0F,
                durationMillis: duration,
                signal: signal
            )
        
        program.events = [event]
        event.signals = [signal]
        signal.intervals = [interval]

        program.save flush:true, failOnError:true
        event.save flush: true, failOnError:true
        signal.save flush: true, failOnError:true
        interval.save flush: true, failOnError:true
        
        this.event = event
    }
    
    def testEdit() {
        def controller = new EventSignalController()
        
        println "Using event ID: ${this.event.id}"
        controller.metaClass.params = [ eventID: this.event.id]
        
        def model = controller.edit()
        
        // if event wasn't found, no model is returned instead we've aborted w/ a 404:
        assert controller.response.status == 200

        assert model != null, "model is null!"
        assert model.event?.class == Event, "Model did not contain Event instance"
        assert model.eventJSON?.signals?.size() > 0, "event JSON did not contain signals"
    }
}

package com.enernoc.open.oadr2.vtn;

import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EventStatusEnumeratedType


/**
 * Comparator to determine ordering of the OadrEvents
 * Expected ordering is
 * 1. Active events have priority
 * 2. Within Active, priority is determine by the EventDescriptor.Priority
 * 3. If both have equal EventDescriptor.Priority, the earlier start time is the higher priority
 * 4. Pending events are sorted by earlier start time
 * @author jlajoie
 */
public class OadrEventComparator implements Comparator<EiEvent> {
    public int compare( EiEvent eventOne, EiEvent eventTwo ) {
		def descriptor1 = eventOne.eiEvent.eventDescriptor
        def descriptor2 = eventOne.eiEvent.eventDescriptor
        boolean eventOneIsActive = descriptor1.eventStatus == EventStatusEnumeratedType.ACTIVE
        boolean eventTwoIsActive = descriptor2.eventStatus == EventStatusEnumeratedType.ACTIVE
        int comparedEventPriority = descriptor1.priority.compareTo( descriptor2.priority )
        int comparedEventDt = eventOne.eiEvent.eiActivePeriod.properties.dtstart.dateTime.value.compare(
                eventTwo.eiEvent.eiActivePeriod.properties.dtstart.dateTime.value );                

        if ( eventOneIsActive ) {
            if ( eventTwoIsActive ) {
                if ( comparedEventPriority == 0 ) {
                    return comparedEventDt
                }
                return comparedEventPriority
            }
            return -1
        }
        else if( eventTwoIsActive ) return 1
        else return comparedEventDt
    }                
}

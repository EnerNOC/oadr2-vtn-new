package com.enernoc.open.oadr2.vtn

import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar

import com.enernoc.open.oadr2.model.DateTime
import com.enernoc.open.oadr2.model.Dtstart
import com.enernoc.open.oadr2.model.DurationPropType
import com.enernoc.open.oadr2.model.DurationValue
import com.enernoc.open.oadr2.model.EiActivePeriod
import com.enernoc.open.oadr2.model.EiEvent
import com.enernoc.open.oadr2.model.EiEventSignal
import com.enernoc.open.oadr2.model.EiEventSignals
import com.enernoc.open.oadr2.model.EventDescriptor
import com.enernoc.open.oadr2.model.Interval
import com.enernoc.open.oadr2.model.Intervals
import com.enernoc.open.oadr2.model.MarketContext
import com.enernoc.open.oadr2.model.Properties
import com.enernoc.open.oadr2.model.EventDescriptor.EiMarketContext



/**
 * Model class for Events that persists unto the database
 * Events will always be enrolled in a Program and may link to multiple VenStatuses
 * May be converted into EiEvent for XML handling 
 * 
 * @authors Thom Nichols, Yang Xiang
 *
 */
class Event {

    private DatatypeFactory _dtf
    
    static belongsTo = [marketContext: Program]
    static hasMany = [venStatus: VenStatus]
    String eventID
    long priority
    Date startDate
    Date endDate
    boolean cancelled
    long intervals = 1
    long modificationNumber = 0L

    static constraints = {
        eventID blank: false, unique: true
        priority min: 0L
        startDate validator : { val, obj ->
            obj.endDate != null && val < obj.endDate 
        }
        endDate validator : { val, obj ->
            obj.startDate != null && val > obj.startDate \
                && val > new Date() // don't allow events in the past
        }
        intervals min: 1L
        modificationNumber min: 0L
        marketContext validator : { val,obj ->
            obj.isConflicting()
        }
    }
    
    /**
     * Modified constructor which sets the current EiEvent to an Event for
     * editing purposes
     * 
     * @param event - the EiEvent to be cast to an Event wrapper class
     */
    public Event(EiEvent event){
        eiEvent = event
        this.eventID = event.eventDescriptor.eventID
        this.priority = event.eventDescriptor.priority
        this.startDate = event.eiActivePeriod.properties.dtstart.dateTime.value
        def duration = event.eiActivePeriod.properties.duration.duration.value
        this.endDate = this.startDate + duration
    }
    
    public String getStatus() {
        if ( this.cancelled ) return "Cancelled"
        def now = new Date()
        if ( this.endDate < now ) return "Completed"
        if ( this.startDate < now ) return "Active"
        return "Pending"
        // TODO determine if "far" or "near"
    }
    
    /**
     * Creates DatatypeFactory instance
     * @return DatatypeFactory
     */
    protected DatatypeFactory getDtf() {
        if ( this._dtf == null ) {
            try {
                this._dtf = DatatypeFactory.newInstance()
            } catch (DatatypeConfigurationException ex) {
                throw new RuntimeException("Error creating DatatypeFactory", ex)
            }
        }
        return this._dtf
    }
    
    public XMLGregorianCalendar getXmlStart() {
        return this.dtf.newXMLGregorianCalendar(this.startDate.toCalendar()).normalize()
    }

    public XMLGregorianCalendar getXmlEnd() {
        return this.dtf.newXMLGregorianCalendar(this.endDate.toCalendar()).normalize()
    }
    
    public Duration getEventDuration() {
        def durationMillis = this.endDate.time - this.startDate.time
        return this.dtf.newDuration( durationMillis )
    }
    
    /**
     * Unwraps the fields of the Event form to an EiEvent object
     * 
     * @return the unwrapped EiEvent with certain fields from the form filled
     */
    public EiEvent toEiEvent() {
        return new EiEvent()
            .withEventDescriptor(new EventDescriptor()
                .withEventID(this.eventID)
                .withPriority(this.priority)
                .withCreatedDateTime(new DateTime(this.xmlStart))
                .withModificationNumber(0))
            .withEiActivePeriod(new EiActivePeriod()
                .withProperties(new Properties()
                    .withDtstart(new Dtstart(new DateTime(this.xmlStart)))
                    .withDuration(new DurationPropType(new DurationValue(
                        this.eventDuration.toString())))))
            .withEiEventSignals(new EiEventSignals()
                .withEiEventSignals(new EiEventSignal()
                    .withIntervals(new Intervals()
                        .withIntervals(new Interval()
                            .withDuration( new DurationPropType(new DurationValue(
                                this.eventDuration.toString())))))))
    }

    /**
     * compares if two events are enrolled in the same program with overlapping times
     * 
     * @return boolean value 
     */
    private boolean notConflicting() {
        //Event.where breaks if null exists, thus an indirect id to designate a value
        def tempID = this.id
        if (tempID == null) tempID = -1
        def activePrograms = Event.where {
            marketContext == this.marketContext
            endDate > this.startDate
            startDate < this.endDate
            id != tempID
            cancelled != true }.count()
        return activePrograms == 0
    }
}
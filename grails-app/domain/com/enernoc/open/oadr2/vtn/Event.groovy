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
import com.enernoc.open.oadr2.model.ObjectFactory
import com.enernoc.open.oadr2.model.PayloadFloat
import com.enernoc.open.oadr2.model.Properties
import com.enernoc.open.oadr2.model.SignalPayload



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
    
    static belongsTo = [program: Program]
    static hasMany = [venStatuses: VenStatus, signals: EventSignal]
     
    String eventID
    long priority
    Date startDate
    Date endDate
    long tolerance
    long notification
    long rampUp
    long recovery
    boolean cancelled
    long modificationNumber = 0L
    
    // TODO event target, group, resource IDs

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
        modificationNumber min: 0L
        program validator : { val,obj ->
            obj.notConflicting()
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
    
    protected makeDuration( Date date, long offset ) {
        def durationMillis = date.time + offset
        def duration = this.dtf.newDuration durationMillis
        GregorianCalendar cal = new GregorianCalendar()
        cal.time = date
        duration.normalizeWith cal
        return duration
    }
    
    public long getDurationMillis() {
        return this.endDate.time - this.startDate.time
    }

    public Duration getDuration() {
        return makeDuration( this.startDate, this.durationMillis )
    }
    
    public Duration getToleranceDuration() {
        return makeDuration( this.startDate, this.tolerance )
    }
    
    public Duration getNotificationDuration() {
        return makeDuration( this.startDate, this.notification )
    }
    
    public Duration getRampUpDuration() {
        return makeDuration( this.startDate, this.rampUp )
    }
    
    public Duration getRecoveryDuration() {
        return makeDuration( this.endDate, this.recovery )
    }
    
    /**
     * Unwraps the fields of the Event form to an EiEvent object
     * 
     * @return the unwrapped EiEvent with certain fields from the form filled
     */
    public EiEvent toEiEvent() {
        ObjectFactory of = new ObjectFactory()
        def intervals = this.intervals.collect {
            new Interval()
                .withDuration( new DurationPropType( 
                    new DurationValue( it.duration.toString() ) ) )
                .withStreamPayloadBase( of.createSignalPayload( 
                    new SignalPayload( new PayloadFloat( it.level ) ) ) )
                .withUid( it.uid )
        }
        
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
                        this.duration.toString())))))
            .withEiEventSignals(new EiEventSignals()
                .withEiEventSignals(new EiEventSignal()
                    .withIntervals(new Intervals()
                        .withIntervals(intervals))))
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
            program == this.program
            endDate > this.startDate
            startDate < this.endDate
            id != tempID
            cancelled != true }.count()
        return activePrograms == 0
    }
}
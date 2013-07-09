package com.enernoc.open.oadr2.vtn

class EventIntervalController {
    
    static allowedMethods = [delete:"POST"]
    
    def delete() {
        def interval = EventInterval.get( params.id )
        
        if ( interval ) {
            interval.delete()
            // FIXME event should be marked as dirty to be pushed to VENs;
            // increment event modification number
            render( contentType: 'text/json' ) { msg = "OK" } 
            return
        }
        
        render( contentType: 'text/json', code: 404 ) {
            msg = "Could not find interval ID ${params.id}"
        }
    }    
}
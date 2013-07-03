package com.enernoc.open.oadr2.vtn


/**
 * Model class for Program that persists unto the database
 * Program may have multiple VENs and Event enrolled
 * 
 * @author Yang Xiang
 *
 */
class Program {
    String name
    String marketContext

    static hasMany = [vens:Ven, events:Event]

    static constraints = {
        name blank: false, unique: true
        marketContext blank:false, validator: { val, obj ->
            /* Note: the built-in `url` validator only allows
               what appear to be 'real' domains, not localhost or 
               *.local, which are valid URLs.  So we're doing our
               own URL validation here: */
            try {
                new URL(val)
                return true
            }
            catch ( e ) {
                return "url.invalid"
            }
        }
    }  
    
    public String toString(){
        name
    }
}

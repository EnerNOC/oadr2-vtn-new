package com.enernoc.open.oadr2.vtn


/**
 * Model class for VEN that persists unto the database
 * Ven may have multiple Programs and VenStatuses
 * 
 * @author Yang Xiang
 *
 */
class Ven {

    static VALID_CLIENT_URI_SCHEMES = ['http', 'https', 'xmpp']
    
    String venID
    String name
    String clientURI

    static belongsTo = Program
    static hasMany = [programs: Program, venStatuses: VenStatus]

    static constraints = {
        venID blank: false, unique: true
        clientURI blank: false, nullable: true, validator: {val ->
            if ( val==null ) return true
            if ( val.size() == 0 ) return "blank"
            
            try {
                def uri = new URI(val)
                if ( ! (uri.scheme in VALID_CLIENT_URI_SCHEMES) )
                    return "invalidscheme"
            }
            catch(ex) {
                return "invalid"
            }
            return true
        }
        programs nullable: false
    }
 
}

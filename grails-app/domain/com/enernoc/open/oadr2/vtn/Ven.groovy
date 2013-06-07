package com.enernoc.open.oadr2.vtn


/**
 * Model class for VEN that persists unto the database
 * Ven may have multiple Programs and VenStatuses
 * 
 * @author Yang Xiang
 *
 */
class Ven {

    String venID
    String name
    String clientURI

    static belongsTo = Program
    static hasMany = [programs: Program, venStatuses: VenStatus]

    static constraints = {
        venID blank: false, unique: true
        clientURI nullable: true, validator: {val ->
            val ==~ /\b(https?|xmpp):\/\/[-\w+&#\/%?@\.=~_|!:,;]*/
        }
        programs nullable: false
    }
 
}

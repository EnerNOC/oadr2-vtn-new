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
    String programID
    String venName
    String clientURI

    static belongsTo = Program
    static hasMany = [program: Program, venStatus: VenStatus]

    static constraints = {
        venID blank: false, unique: true
        clientURI nullable: true, url:true
        program nullable: false
    }    
}

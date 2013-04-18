package com.enernoc.open.oadr2.vtn


/**
 * Model class for Program that persists unto the database
 * Program may have multiple VENs and Event enrolled
 * 
 * @author Yang Xiang
 *
 */
class Program {
    String programName
    String programURI

    static hasMany = [ven:Ven, event:Event]

    static constraints = {
        programName blank: false, nullable: false, unique: true
        programURI blank:false, url:true
    }    
}

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
        marketContext blank:false, url:true
    }  
    
    public String toString(){
        name
    }
}

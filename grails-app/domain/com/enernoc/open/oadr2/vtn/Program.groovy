package com.enernoc.open.oadr2.vtn

class Program {
    String programName
    String programURI

    static hasMany = [ven:Ven, event:Event]

    static constraints = {
        programName blank: false, nullable: false, unique: true
        programURI blank:false, url:true
    }    
}

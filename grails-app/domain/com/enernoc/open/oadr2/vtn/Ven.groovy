package com.enernoc.open.oadr2.vtn

class Ven {

    String venID
    String programID
    String venName
    String clientURI

    static belongsTo = Program
    static hasMany = [program: Program]

    static constraints = {
        venID blank: false, unique: true
        clientURI nullable: true, url:true
    }    
}

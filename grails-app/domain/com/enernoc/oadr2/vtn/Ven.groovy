package com.enernoc.oadr2.vtn

class Ven {

    String venID
    String programID
    String venName
    String clientURI

    static belongsTo = Program
    static hasMany = [program: Program]

    static constraints = {
        venID(blank: false, validator: {val, obj ->
            obj.UniqueVenID()    
        })
        clientURI(nullable: true, url:true)

        //select a program id from one of the available programs

    }
    
    private boolean UniqueVenID() {
        boolean result = true
        def venList = Ven.list()
        venList.each { v ->
            if (v.id != this.id) {
                if (v.venID == this.venID) {
                    result = false
                }
            }
        }
        return result
    }
}

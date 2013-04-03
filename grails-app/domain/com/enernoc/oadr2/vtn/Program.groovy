package com.enernoc.oadr2.vtn

class Program {
    //@Required(message = "Must enter a valid Program Name")
    String programName
    //@Required(message = "Must enter a valid Program URI")
    String programURI

    static hasMany = [ven:Ven, event:Event]

    static constraints = {
        programName (blank: false, validator: {val, obj ->
            obj.uniqueProgramName()
        })
        programURI (blank:false)
    }
    
    private boolean uniqueProgramName() {
        boolean result = true
        def programList = Program.list()
        programList.each { p ->
            if (p.id != this.id) {
                if (p.programName == this.programName) {
                    result = false
                }
            }
        }
        return result
        
    }
}

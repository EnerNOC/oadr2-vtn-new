package com.enernoc.oadr2.vtn

class Program {
	//@Required(message = "Must enter a valid Program Name")
	String programName;
	//@Required(message = "Must enter a valid Program URI")
	String programURI;
	
	static hasMany = [ven:Ven, event:Event]
	
    static constraints = {
		programName (blank: false, unique: true)
		programURI (blank:false)
    }
}

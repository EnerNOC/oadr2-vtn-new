package com.enernoc.oadr2.vtn

class HomeController {

    def index() {
        def programs = Program.list()
        def vens = Ven.list()
        def events = Event.list()
        render view : "/index", model:[
            programList : programs,
            venList : vens,
            eventList : events ] 
    }
}

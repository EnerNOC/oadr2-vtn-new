package com.enernoc.open.oadr2.vtn

/**
 * Controller for index page
 * 
 * @author Thom Nichols
 * 
 */
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

import grails.util.Environment

import com.enernoc.open.oadr2.vtn.Program
import com.enernoc.open.oadr2.vtn.Ven


/**
 * BootStrap class with initially persisted data
 * domain matches those of the test cases in the readme.txt
 * @author Yang Xiang
 */
class BootStrap {
    
    def grailsApplication

    def init = { servletContext ->
//        log.info  "ENV: ------------- ${Environment.current.name}"
        if ( Environment.current.name != 'development' ) {
            log.info "+++++ Not in dev, Skipping Bootstrap!"
            return
        }
        try {
            if ( Ven.count() ) {
                log.info "+++++ Bootstrap data appears to already be created!"
                return
            }
            
            def hostname = java.net.InetAddress.getLocalHost().getHostName()
            // FIXME assuming scheme and port :(
            def serverURL = "http://${hostname}:8080/${grailsApplication.metadata['app.name']}"
            
            def pro1 = new Program(name:"Program A", marketContext:"$serverURL/program/a")
            def pro2 = new Program(name:"Program B", marketContext:"$serverURL/program/b")
            def pro3 = new Program(name:"Program C", marketContext:"$serverURL/program/c")

            def ven1 = new Ven(venID:"ven1", name:"Site 1", clientURI:"$serverURL/OADRTest/ven/ven1")
            def ven2 = new Ven(venID:"ven2", name:"Site 2", clientURI:"$serverURL/OADRTest/ven/ven2")
            def ven3 = new Ven(venID:"ven3", name:"Site 3")

            pro1.addToVens ven1
            pro2.addToVens ven2
            pro3.addToVens ven3
            
            pro1.save()
            pro2.save()
            pro3.save()
            log.info "+++++ Created bootstrap data!"
        }
        catch ( ex ) {
            log.warn "Bootstrap error (maybe these already exist?", ex
        }
    }
    
    def destroy = {
    }
}

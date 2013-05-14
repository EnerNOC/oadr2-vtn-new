import grails.util.Environment

import com.enernoc.open.oadr2.vtn.Program
import com.enernoc.open.oadr2.vtn.Ven


/**
 * BootStrap class with initially persisted data
 * domain matches those of the test cases in the readme.txt
 * @author Yang Xiang
 */
class BootStrap {

    def init = { servletContext ->
//        log.info  "ENV: ------------- ${Environment.current.name}"
        if ( Environment.current.name != 'development' ) {
            log.info "Not in dev, Skipping Bootstrap!"
            return
        }
        try {
            if ( Ven.count() ) {
                log.info "Bootstrap data appears to already be created!"
                return
            }
            
    		def pro1 = new Program(name:"test-program-one", marketContext:"http://test-uri-one.com")
    		def pro2 = new Program(name:"test-program-two", marketContext:"http://test-uri-two.com")
    		def pro3 = new Program(name:"test-program-three", marketContext:"http://test-uri-three.com")
    		//pro1.save()
    		//pro2.save()
    	
    		def Ven1 = new Ven(venID:"test-customer-one", name:"test-name-one", clientURI:"http://test-client-uri-one.com")
    		pro1.addToVens(Ven1)
    		pro1.save();
    		
    		def Ven2 = new Ven(venID:"test-customer-two", name:"test-name-two", clientURI:"http://test-client-uri-two.com")
    		pro2.addToVens(Ven2)
    		pro2.save();
    		
    		def Ven3 = new Ven(venID:"test-customer-three", name:"test-name-three", clientURI:"http://test-client-uri-three.com")
    		pro3.addToVens(Ven3)
    		pro3.save();
        }
        catch ( ex ) {
            log.warn "Bootstrap error (maybe these already exist?", ex
        }
	}
    
    def destroy = {
    }
}

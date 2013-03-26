import com.enernoc.oadr2.vtn.Program
import com.enernoc.oadr2.vtn.Ven
import com.enernoc.oadr2.vtn.XmppService


/**
 * BootStrap class with initially persisted data
 * domain matches those of the test cases in the readme.txt
 * @author Yang Xiang
 */
class BootStrap {

	def xmppService
    def init = { servletContext ->
		def pro1 = new Program(programName:"test-program-one", programURI:"test-uri-one")
		def pro2 = new Program(programName:"test-program-two", programURI:"test-uri-two")
		def pro3 = new Program(programName:"test-program-three", programURI:"test-uri-three")
		//pro1.save()
		//pro2.save()
	
		def Ven1 = new Ven(venID:"test-customer-one", programID:pro1.programName, venName:"test-name-one", clientURI:"test-client-uri-one")
		pro1.addToVen(Ven1)
		pro1.save();
		
		def Ven2 = new Ven(venID:"test-customer-two", programID:pro2.programName, venName:"test-name-two", clientURI:"test-client-uri-two")
		pro2.addToVen(Ven2)
		pro2.save();
		
		def Ven3 = new Ven(venID:"test-customer-three", programID:pro3.programName, venName:"test-name-three", clientURI:"test-client-uri-three")
		pro3.addToVen(Ven3)
		pro3.save();
	}
    def destroy = {
    }
}

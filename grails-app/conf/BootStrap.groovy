import com.enernoc.open.oadr2.vtn.Program
import com.enernoc.open.oadr2.vtn.Ven


/**
 * BootStrap class with initially persisted data
 * domain matches those of the test cases in the readme.txt
 * @author Yang Xiang
 */
class BootStrap {

    def init = { servletContext ->
		def pro1 = new Program(programName:"test-program-one", programURI:"http://test-uri-one.com")
		def pro2 = new Program(programName:"test-program-two", programURI:"http://test-uri-two.com")
		def pro3 = new Program(programName:"test-program-three", programURI:"http://test-uri-three.com")
		//pro1.save()
		//pro2.save()
	
		def Ven1 = new Ven(venID:"test-customer-one", programID:pro1.programName, venName:"test-name-one", clientURI:"http://test-client-uri-one.com")
		pro1.addToVen(Ven1)
		pro1.save();
		
		def Ven2 = new Ven(venID:"test-customer-two", programID:pro2.programName, venName:"test-name-two", clientURI:"http://test-client-uri-two.com")
		pro2.addToVen(Ven2)
		pro2.save();
		
		def Ven3 = new Ven(venID:"test-customer-three", programID:pro3.programName, venName:"test-name-three", clientURI:"http://test-client-uri-three.com")
		pro3.addToVen(Ven3)
		pro3.save();
	}
    def destroy = {
    }
}

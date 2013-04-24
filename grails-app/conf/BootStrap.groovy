import com.enernoc.open.oadr2.vtn.Program
import com.enernoc.open.oadr2.vtn.Ven


/**
 * BootStrap class with initially persisted data
 * domain matches those of the test cases in the readme.txt
 * @author Yang Xiang
 */
class BootStrap {

    def init = { servletContext ->
		def pro1 = new Program(name:"test-program-one", marketContext:"http://test-uri-one.com")
		def pro2 = new Program(name:"test-program-two", marketContext:"http://test-uri-two.com")
		def pro3 = new Program(name:"test-program-three", marketContext:"http://test-uri-three.com")
		//pro1.save()
		//pro2.save()
	
		def Ven1 = new Ven(venID:"test-customer-one", programID:pro1.name, venName:"test-name-one", clientURI:"http://test-client-uri-one.com")
		pro1.addToVens(Ven1)
		pro1.save();
		
		def Ven2 = new Ven(venID:"test-customer-two", programID:pro2.name, venName:"test-name-two", clientURI:"http://test-client-uri-two.com")
		pro2.addToVens(Ven2)
		pro2.save();
		
		def Ven3 = new Ven(venID:"test-customer-three", programID:pro3.name, venName:"test-name-three", clientURI:"http://test-client-uri-three.com")
		pro3.addToVens(Ven3)
		pro3.save();
	}
    def destroy = {
    }
}

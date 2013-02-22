import org.enernoc.oadr2.vtn.*

class BootStrap {

    def init = { servletContext ->
		def pro1 = new Program(programName:"test1", programURI:"testing.com")
		def pro2 = new Program(programName:"test2", programURI:"testing-number-two.com")
		//pro1.save()
		pro2.save()
	
		def Ven1 = new Ven(venID:"firstVen", programID:pro1.programName, venName:"firstVen")
		pro1.addToVen(Ven1)
		pro1.save();
    }
    def destroy = {
    }
}

class UrlMappings {

	static mappings = {
        
        name eventSignal: "/event/$eventID/signals" {
            controller = "eventSignal"
            action = [GET:'edit',POST:'update']
        }
        
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
		"/OpenADR2/Simple/EiEvent"(controller : "eiEvent") {
            action = [POST:"post"]
        }
        
        "/OADRTest/template/$service/$template"(controller: "OADRTest", action: "template")

		"/"( controller : "home" )
		
		"404"( controller : 'error' ) 
		"500"( controller : 'error' )
        "/debug"(view:"/debug")
	}
}

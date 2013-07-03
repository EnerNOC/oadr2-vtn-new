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
        
        // test/debug endpoints:
        "/OADRTest/template/$service/$template"(controller: "OADRTest", action: "template")
        "/OADRTest/ven/$venID?"( controller:"DummyVenController" ) {
             action = [POST:'push']
        }

		"/"( controller : "home" )
		
		"404"( controller : 'error' ) 
		"500"( controller : 'error' )
        "/debug"(view:"/debug")
	}
}

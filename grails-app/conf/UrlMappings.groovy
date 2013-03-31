class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
		"/OpenADR2/Simple/EiEvent"(controller : "eiEvent") {
            action = [POST:"post"]
        }

		"/"( view : "/index" )
		
		"404"( controller : 'error' ) 
		"500"( controller : 'error' )
	}
}

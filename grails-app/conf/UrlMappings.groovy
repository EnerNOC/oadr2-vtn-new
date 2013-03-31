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
		
		"404"( view : 'error' ) 
		"500"( view : 'error' )
	}
}

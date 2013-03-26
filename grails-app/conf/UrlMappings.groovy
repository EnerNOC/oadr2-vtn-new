class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
		"/OpenADR2/Simple/EiEvent"(controller : "EiEventController", action : "handle")

		"/"(view:"/index")
		"500"(view:'/error')
		"404"(view:'/error')
	}
}

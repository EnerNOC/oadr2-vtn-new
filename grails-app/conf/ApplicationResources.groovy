modules = {
    application {
		resource url: '/css/custom.css'
        resource url:'/js/application.js'
    }
    
    fontAwesome {
        dependsOn 'bootstrap'
        resource url: '/css/font-awesome.min.css'
    }
    
    eventIntervals {
        dependsOn 'backbone', 'formTime'
        
        resource url: '/js/eventIntervals.js'
    }
    
	formTime {
		dependsOn 'jquery'

		resource url: '/css/datepicker.css'
		resource url: '/css/bootstrap-timepicker.min.css'
		resource url: '/js/bootstrap-datepicker.js', attrs:[type:'js']
		resource url: '/js/bootstrap.js', attrs:[type:'js']
		resource url: '/js/bootstrap-timepicker.min.js', attrs:[type:'js']
	}
    backbone {
        dependsOn 'jquery'
        
        resource url: '/js/underscore-min.js'
        resource url: '/js/ICanHaz.min.js'
        resource url: '/js/backbone-min.js'
    }
}
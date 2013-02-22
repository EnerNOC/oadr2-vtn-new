modules = {
    application {
        resource url:'js/application.js'
    }
	formTime {
		dependsOn 'jquery'

		resource url: '/css/datepicker.css'
		resource url: '/css/bootstrap-timepicker.min.css'
		resource url: '/js/bootstrap-datepicker.js'
		resource url: '/js/bootstrap.js'
		resource url: '/js/bootstrap-timepicker.min.js'
		
		
		
	}
}
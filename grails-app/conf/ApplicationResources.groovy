modules = {
    application {
		resource url: '/css/custom.css'
        resource url:'/js/application.js'
    }
	formTime {
		dependsOn 'jquery'

		resource url: '/css/datepicker.css'
		resource url: '/css/bootstrap-timepicker.min.css'
		resource url: '/js/bootstrap-datepicker.js', attrs:[type:'js']
		resource url: '/js/bootstrap.js', attrs:[type:'js']
		resource url: '/js/bootstrap-timepicker.min.js', attrs:[type:'js']
	}
}
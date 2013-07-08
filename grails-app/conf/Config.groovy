// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

vtnID = "ENOCtestVTN1"

xmppSvc {
    jid = '<your account here>@gmail.com'
    passwd = '<your password here>'
    resource = vtnID
    host = 'talk.google.com'
    port = 5222
    serviceName = 'gmail.com'
}

oadrTest.templatesFolder = "oadrTemplates"
//xmpp.debug = true

if ( xmpp.debug ) {
	System.setProperty "smack.debugEnabled", "true"
	System.setProperty "smack.debuggerClass", "org.jivesoftware.smack.debugger.ConsoleDebugger"
}

grails.project.groupId = 'com.enernoc.open.oadr2.vtn'
grails.mime.file.extensions = false
grails.mime.use.accept.header = false
grails.mime.types = [
	all:           '*/*',
	atom:          'application/atom+xml',
	css:           'text/css',
	csv:           'text/csv',
	form:          'application/x-www-form-urlencoded',
	html:          ['text/html','application/xhtml+xml'],
	js:            'text/javascript',
	json:          ['application/json', 'text/json'],
	multipartForm: 'multipart/form-data',
	rss:           'application/rss+xml',
	text:          'text/plain',
//	xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
grails.resources.debug=true

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false
// see: http://grails.org/doc/2.1.0/guide/single.html#configGORM
grails.gorm.failOnError=true

grails.servlet.version = "3.0"

environments {
	development {
		grails.logging.jul.usebridge = true
	}
	production {
		grails.logging.jul.usebridge = false
		// TODO: grails.serverURL = "http://www.changeme.com"
	}
}

// log4j configuration
log4j = {
	appenders {
	    console name:'stdout', layout:pattern(conversionPattern: '%d{ISO8601} %p %c{2} %m%n')
	}

	warn  'org.codehaus.groovy.grails.web.servlet',        // controllers
			'org.codehaus.groovy.grails.web.pages',          // GSP
			'org.codehaus.groovy.grails.web.sitemesh',       // layouts
			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping',        // URL mapping
			'org.codehaus.groovy.grails.commons',            // core / classloading
			'org.codehaus.groovy.grails.plugins',            // plugins
			'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

    error 'grails.app.services.org.grails.plugin.resource',
          'grails.app.taglib.org.grails.plugin.resource',
          'grails.app.resourceMappers.org.grails.plugin.resource'
            
	info 'grails.app'
    
    debug 'grails.app.controllers',
          'grails.app.services',
          'grails.app.filters'
//          'org.hibernate.SQL'
          
//    trace 'org.hibernate.type'
}

rabbitmq {
    connectionfactory {
        username = 'guest'
        password = 'guest'
        hostname = 'localhost'
    }
	
    queues = {
        /* when an event is created or modified, a worker queries the VENs 
         * that should be pushed to and generates the oadrDistributeEvent payloads
         */
        "oadr.push.event"()
        /* For oadrDistributeEvent payloads that perform a (blocking) HTTP push, we 
         * use a worker queue
         */
        "oadr.push.payload"()
    }
}

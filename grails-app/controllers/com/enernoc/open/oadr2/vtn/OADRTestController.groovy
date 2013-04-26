package com.enernoc.open.oadr2.vtn

import javax.xml.bind.JAXBException;

import com.enernoc.open.oadr2.xmpp.JAXBManager;

import grails.converters.JSON

/**
 * TODO Auth on ALL OF THESE METHODS
 * @author tnichols
 */
class OADRTestController {
    
    static allowedMethods = [execute:"POST"]
    
    EiEventService eiEventService // injected
    
    static JAXBManager jaxbManager // this is threadsafe
    
    static { // default initializer
        try {
            jaxbManager = new JAXBManager()
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating JAXB context", e)
        }
    }

    private getTemplatesFolder() {
        grailsApplication.config.oadrTest.templatesFolder
    }
    
    def index() {
        render view: "index", model : [
            services: this.services,
          ]
    }
    
    def getServices() {
        def services = []
        new File(templatesFolder).eachDir {
            services << it.name
        }
        log.debug("services: $services")
        return services
    }
    
    def templates() {
        log.debug("Getting templates for service ${params.id}")
        
        def templates = []
        new File(templatesFolder,params.id).eachFile {
            if ( it.name.endsWith('.xml') ) templates << it.name
        }
        def model = [:]
        model["templates"] = templates 
        render model as JSON
    }
    
    def programs() {
        render(contentType: "text/json") {
            programs = Program.list(limit:10).collect {
                [ name : it.marketContext,
                  id  : it.id,
                  marketContext : it.marketContext
                ]
            }
        }
    }
    
    def vens() {
        def programID = params.programID
        log.debug("Vens for program ID: $programID")
        //TODO This requires going through the array of programs to find venList. Current implementation fails
        def venList = Ven.find { programs.id == programID }
        render(contentType: "text/json") {
            vens = venList.collect {
                [ name : it.name,
                  id  : it.venID
                ]
            }
        }
    }
    
    def events() {
        def programID = params.programID
        log.debug("Events for program ID: $programID")
        def eventList = Event.find { program.id == programID }
        render(contentType: "text/json") {
            events = eventList.collect {
                [ id  : it.venID ]
            }
        }
    }
    
    /**
     * Expected request format: urlencoded form
     * Expected params: 
     *    serviceName: OpenADR service name, e.g. "EiEvent"
     *    requestTxt: XML request payload
     *    
     * Response format: 'text/json'
     * Response: {
     *    status: "OK" or "Error",
     *    response: "<xml payload....>"
     * }
     */
    def execute() {
        def unmarshaller = this.jaxbManager.context.createUnmarshaller()
        def service = params.serviceName // TODO handle services other than EiEvent
        log.info("Submitted request to $service")
        log.debug(params.requestTxt)
        def payload = unmarshaller.unmarshal( new StringReader(params.requestTxt) )
        try {
            def response = eiEventService.handleOadrPayload(payload)
            def marshaller = jaxbManager.createMarshaller()
            def out = new StringWriter()
            marshaller.marshal( response, out )
            render( contentType: 'text/json') {
                status = "OK"
                data = out.toString()
            }
        }
        catch( Exception ex ) {
            log.warn("Service error", ex)
            render( status: 501, contentType: 'text/json' ) {
                status = "Error"
                msg = ex.message
            }
        }
    }
    
    /**
     * AJAX call to get a request template.
     * Expected request format: URL path parameters:
     *    "/OADRTest/template/$service/$template"
     *    
     * Expected params:
     *    service: OpenADR service name, e.g. "EiEvent"
     *    template: XML request template filename
     */
    def template() {
        // TODO this is potentially super-dangerous to allow 
        // filesystem access, could allow a malicious user to access system files.
        def service = params.service
        def fileName = params.template
        log.debug("Fetching template $fileName")
        if( fileName.indexOf( ".." ) > -1 ||
            service.indexOf( ".." ) > -1 ) {
            render status: 401, text: "Invalid path"
            return
        }
        render contentType: "text/plain", 
            text: new File([templatesFolder,service,fileName].join( File.separator )).text 
    }
}
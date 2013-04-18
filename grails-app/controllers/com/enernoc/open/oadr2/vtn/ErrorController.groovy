package com.enernoc.open.oadr2.vtn

import org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException;

import grails.util.Environment

/**
 * @author Thom Nichols
 *
 */
class ErrorController {

    /**
     * This attempts to detect the request type so e.g. we don't return an ugly HTML page 
     * to a JSON or XML request.
     */
	def index() {
        def code = request.'javax.servlet.error.status_code'
        def msg = request.'javax.servlet.error.message'
		log.warn "Error $code: ($request.exception) $msg"
        
        def contentType = request.getHeader("Accept")
        contentType ?: request.getHeader("Content-Type") 
        if ( request.xhr && contentType == '*/*' ) contentType = 'text/plain' 
        log.debug "Serving error for content type: $contentType"
        switch ( contentType ) {
            case "text/xml":
            case "application/xml":
                render contentType : "application/xml", text: "<error code='$code'>$msg</error>"
                break
            case "application/json":
            case "text/json":
                render( contentType : "text/json" ) {
                    [code : code, message : msg] 
                }
                break
            case "text/plain":
                if( request.exception.class == GrailsWrappedRuntimeException )
                    request.exception = request.exception.cause
                render contentType : "text/plain", 
                    text : "Error $code: $msg.  Exception: ${request.exception}"
                break
            default: // render HTML
                render view : '/error'
        }
	}
}
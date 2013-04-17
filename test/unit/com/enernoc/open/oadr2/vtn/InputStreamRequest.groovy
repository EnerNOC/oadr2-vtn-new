package com.enernoc.open.oadr2.vtn

import java.io.IOException
import java.io.InputStream

import javax.servlet.ServletInputStream

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest

/**
 * This class is necessary for testing controllers that read from the request's 
 * raw InputStream or Reader 
 * @author tnichols
 *
 */
public class InputStreamRequest extends GrailsMockHttpServletRequest {
    
    ServletInputStream inputStream
    BufferedReader reader
    
    public InputStreamRequest( String body ) {
        this.inputStream = new WrappedInputStream( body )
        this.reader = new BufferedReader( new InputStreamReader( this.inputStream ) )
    }
    
    public getReader
    
    class WrappedInputStream extends ServletInputStream {

        InputStream wrapped
        
        WrappedInputStream( String body ) {
            this.wrapped = new ByteArrayInputStream( body.bytes )
        }
        
        WrappedInputStream( InputStream body ) {
            this.wrapped = body
        }
        
        @Override
        public int read() throws IOException {
            return this.wrapped.read()
        }
    }
}

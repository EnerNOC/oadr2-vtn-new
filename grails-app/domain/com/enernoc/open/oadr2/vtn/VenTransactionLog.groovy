package com.enernoc.open.oadr2.vtn

/**
 * Class to record interactions with a VEN
 * @author Yang Xiang
 */
class VenTransactionLog {

    static int MAX_LEN = 255
    
    String venID
    String requestID
    String type
    Date date = new Date()
    String uri
    String request
    String response
    String error
    
    static constraints = {
      error nullable: true
      uri nullable: true
      response nullable: true
      type inList: ['push_request', 'push_response', 'pull_request', 'pull_response']
    }
    
    // truncate these fields if necessary
    void setRequest( String req) {
        if ( req && req.size() > MAX_LEN )
            this.request = req.substring( 0, MAX_LEN )
        else this.request = req
    }
    void setResponse( String resp ) {
        if ( resp && resp.size() > MAX_LEN )
            this.response = resp.substring( 0, MAX_LEN )
        else this.response = resp
    }
    void setError( String err ) {
        if ( err && err.size() > MAX_LEN )
            this.error = err.substring( 0, MAX_LEN )
        else this.error = err
    }
}

package com.enernoc.open.oadr2.vtn

/**
 * Class to record interactions with a VEN
 * @author Yang Xiang
 */
class VenTransactionLog {

    String venID
    String requestID
    String type
    Date date = new Date()
    String uri
    String request
    String response
    String error = null
    
    static constraints = {
      error nullable: true
      uri nullable: true
      response nullable: true
      type inList: ['push_request', 'push_response', 'pull_request', 'pull_response']
    }
}

package com.enernoc.open.oadr2.vtn

//import com.enernoc.open.oadr2.model.OadrDistributeEvent
//import com.enernoc.open.oadr2.model.OadrResponse

/**
 * A log to record 
 * @author Yang Xiang
 *
 */
class VenTransactionLog {

    String venID
    String UID
    boolean push
    Date sentDate
    //OadrDistributeEvent payload TODO cannot store entire object need alternative
   // response TODO cannot store entire object need alternative
    Date responseDate
    
    static constraints = {
      //  response nullable: true
        responseDate nullable: true
    }
}

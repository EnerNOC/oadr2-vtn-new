package com.enernoc.open.oadr2.vtn
/**
 * Contains a hashmap that stores the ID and payloads of each IQ
 * for OadrResponse purposes
 * 
 * @author Yang Xiang
 *
 */
class IQCache {
    
    private static HashMap<String, Object> payloadCache = new HashMap<String, Object>()
    
    public static void put(String id, Object payload) {
        payloadCache.put(id, payload)
    }
    
    public static void get(String id) {
        payloadCache.get( id )
    }
    
}

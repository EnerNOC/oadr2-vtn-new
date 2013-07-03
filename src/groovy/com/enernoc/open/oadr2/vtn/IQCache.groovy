package com.enernoc.open.oadr2.vtn

/**
 * Contains a hashmap that stores the ID and payloads of each IQ
 * for OadrResponse purposes
 * 
 * @author Yang Xiang
 */
class IQCache {
    
    private HashMap<String, Object> payloadCache = new HashMap<String, Object>()
    
    public IQCache() {}
    
    public void put(String id, Object payload) {
        payloadCache.put id, payloa
    }
    
    public Object get(String id) {
        payloadCache.get id
    }
    
}

package protocol;

import java.util.HashMap;
import java.util.Map;

import play.Logger;

/**
 * Registry to determine which type of protocol a VEN is
 * 
 * 
 * @author jlajoie
 *
 */
public class ProtocolRegistry {
    
    private Map<String, IProtocol> registry = new HashMap<String, IProtocol>();
    private static volatile ProtocolRegistry instance;

    /**
     * Lazy invocation of ProtocolRegistry
     * @return the single instance of ProtocolRegistry
     */
    public static ProtocolRegistry getInstance(){
        if(instance == null){
            synchronized(ProtocolRegistry.class){
                if(instance == null){
                    instance = new ProtocolRegistry();                    
                }
            }
        }
        return instance;
    }
    
    /**
     * Determines which type of protocol VEN is based on the uri
     * 
     * @param uri - The String uri of a VEN for push functionality
     * @return the IProtocol of the VEN
     */
    public IProtocol getProtocol(String uri){
        if(!registry.containsKey(uri)){
            addProtocol(uri);
        }
        return registry.get(uri);
    }
    
    /**
     * Add the URI and a protocol to the ProtocolRegistry
     * 
     * @param uri - The String uri of a VEN for push functionality
     */
    public void addProtocol(String uri){
        if(uri.length() > 0){
            if(uri.contains("http")){
                registry.put(uri, new HTTPProtocol());
            }
            else{
                Logger.info("URI is: " + uri);
                registry.put(uri, new XMPPProtocol());
            }
        }
    }
}

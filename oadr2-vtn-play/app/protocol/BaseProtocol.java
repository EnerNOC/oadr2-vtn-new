package protocol;

import javax.persistence.Embeddable;


/**
 * Abstract class which serves as a Super class to the HTTPProtocol
 * and XMPPProtocol, as well as being extensible for other protocols
 * to be added later
 * 
 * @author jlajoie
 *
 */
@Embeddable
public abstract class BaseProtocol implements IProtocol{  
    
    private ProtocolType protocolType;

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
    
}
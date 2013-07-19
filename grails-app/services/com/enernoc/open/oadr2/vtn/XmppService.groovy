package com.enernoc.open.oadr2.vtn

import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.DatatypeFactory

import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.Packet

import com.enernoc.open.oadr2.xmpp.JAXBManager
import com.enernoc.open.oadr2.xmpp.OADR2IQ
import com.enernoc.open.oadr2.xmpp.OADR2PacketExtension
import com.enernoc.open.oadr2.xmpp.OADR2PacketFilter
import com.enernoc.open.oadr2.model.OadrDistributeEvent;

/**
 * XMPPService is used to establish and hold the XMPPConnection
 * to be used for sending and creating events
 *
 * @author Thom Nichols, Yang Xiang
 */
public class XmppService implements PacketListener {

    String jid
    String xmppPasswd
    String xmppHost
    int xmppPort
    String xmppServiceName
    String xmppResource
    
    String OADR2_XMLNS = OadrDistributeEvent.class.getAnnotation(XmlRootElement.class).namespace()
    ConnectionConfiguration connConfig
    XMPPConnection xmppConn

    EiEventService eiEventService
    
    IQCache iqCache

    Marshaller marshaller

    /**
     * Constructor to establish the XMPP connection
     *
     * @throws XMPPException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws JAXBException
     */
    public XmppService() {
        JAXBManager jaxb = new JAXBManager()
        this.marshaller = jaxb.createMarshaller()
        this.iqCache = new IQCache()
    }

    public void processPacket(Packet packet) {        
        log.debug "Got packet: $packet"
        def payload = packet.getExtension(OADR2_XMLNS)?.payload
        
        if ( packet.getError() != null ) {
            log.warn "Got error packet #${packet.packetID} from ${packet.from}: ${packet.error}"
            def request = iqCache.get( packet.packetID )
            // TODO process error
            return
        }
        
        def response = eiEventService.handleOadrPayload( payload )
        if ( response )	send response, packet.from, packet.getPacketID()
    }

    /**
     * Establish a connection for the XMPP server
     *
     * @throws XMPPException
     */
    void connect() {
        if ( ! this.jid ) {
            log.warn "+----------------------------------------------------+"
            log.warn "JID not configured!  XMPP service will not be enabled!"
            log.warn "+----------------------------------------------------+"
            return
        }
        try {
            if ( ! this.jid && ! this.xmppHost )
                this.xmppHost = this.jid.split('@')[-1]
            if ( this.xmppPort <= 1 ) xmppPort = 5222

            this.connConfig = this.xmppServiceName ? 
                new ConnectionConfiguration( this.xmppHost, this.xmppPort, 
                    (String) this.xmppServiceName )
                : new ConnectionConfiguration( this.xmppHost, this.xmppPort )

            connConfig.compressionEnabled = true
            connConfig.SASLAuthenticationEnabled = true
    
            this.xmppConn = new XMPPConnection(this.connConfig)
            xmppConn.connect()
            log.info "XMPP connected to ${xmppConn.host}"
    
            xmppConn.addPacketListener this, new OADR2PacketFilter()
                
            xmppConn.login this.jid, this.xmppPasswd, this.xmppResource
            log.info "Logged in as ${xmppConn.user}"
        }
        catch ( Exception ex ) {
            // don't abort if there's an XMPP error
            log.error "XMPP conntion error", ex
        }
    }

    void disconnect() {
        if ( ! this.xmppConn?.connected ) {
            log.warn "XMPP not connected"
            return
        }
        this.xmppConn.disconnect()
    }
    

    /**
     * Sends an object to a JID
     *
     * @param o - the OpenADR payload to be sent
     * @param jid - the Jid to receive the object
     */
    def send( Object payload, String jid ) {
        this.send payload, jid, null
    }


    /**
     * Send an object to a jid with the specified packetId
     *
     * @param o - the Object to be sent
     * @param jid - the Jid to receive the object
     * @param packetId - the packetId the packet must contain
     */
    def send( Object payload, String jid, String packetID ) {
        IQ iq = new OADR2IQ(new OADR2PacketExtension(payload, this.marshaller))
        iq.to = uriToJid( jid )
        
        if ( packetID ) { // this is a response
            iq.packetID = packetID
            iq.type = IQ.Type.RESULT            
        }
        else {
            iq.type = IQ.Type.SET
            iqCache.put iq.getPacketID(), payload
        }
        log.debug "XMPP send #${iq.packetID} to $jid: $payload"
        
        xmppConn.sendPacket iq
    }
    
    public static uriToJid( uri ) {
        if ( uri.startsWith( 'xmpp:' ) )
            return new URI(uri).schemeSpecificPart    
        return uri
    }
}
package com.enernoc.oadr2.vtn

import javax.xml.bind.Marshaller
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

/**
 * XMPPService is used to establish and hold the XMPPConnection
 * to be used for sending and creating events
 *
 * @author Jeff LaJoie
 *Converted from Play to Grails by Yang Xiang
 */
public class XmppService implements PacketListener {

    String jid
    String xmppPasswd
    String xmppHost
    int xmppPort
    String xmppServiceName
    String xmppResource

    ConnectionConfiguration connConfig
    XMPPConnection xmppConn

    EiEventService eiEventService

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
    }

    public void processPacket(Packet packet) {
        log.debug "Got packet: $packet"
        def payload = packet.getExtension(OADR2_XMLNS)?.payload

        def response = eiEventService.handleOadrPayload( payload )
        log.debug "Got response: $response"

        if ( response )	send payload, packet.from, packet.id
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

        if ( ! this.jid && ! this.xmppHost )
            this.xmppHost = this.jid.split('@')[-1]
        if ( this.xmppPort <= 1 ) xmppPort = 5222

        this.connConfig = new ConnectionConfiguration(
                this.xmppHost, this.xmppPort, (String)this.xmppServiceName )
        connConfig.compressionEnabled = true
        connConfig.SASLAuthenticationEnabled = true

        this.xmppConn = new XMPPConnection(this.connConfig)
        xmppConn.connect()
        log.info "XMPP connected to ${xmppConn.host}"

        xmppConn.addPacketListener this, new OADR2PacketFilter()

        xmppConn.login this.jid, this.xmppPasswd, this.xmppResource
        log.info "Logged in as ${xmppConn.user}"
    }

    void disconnect() {
        if ( ! this.xmppConn?.connected )
            log.warn "XMPP not connected"
        return
        this.xmppConn.disconnect()
    }

    /**
     * Sends an object to a JID
     *
     * @param o - the OpenADR payload to be sent
     * @param jid - the Jid to receive the object
     */
    def send( Object payload, String jid ) {
        this.send payload, jid
    }

    /**
     * Send an object to a jid with the specified packetId
     *
     * @param o - the Object to be sent
     * @param jid - the Jid to receive the object
     * @param packetId - the packetId the packet must contain
     */
    def send( Object payload, String jid, String packetID ) {
        log.debug "XMPP send to $jid: # $packetID: $payload"
        IQ iq = new OADR2IQ(new OADR2PacketExtension(payload, this.marshaller))
        if ( packetID ) { // this is a response
            iq.id = packetID
            iq.type = IQ.Type.RESULT
        }
        else iq.type = IQ.Type.SET
        iq.to jid
        this.xmppConn.sendPacket iq
    }
}
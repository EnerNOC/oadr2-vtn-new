package service;

import java.io.ByteArrayInputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

import jaxb.JAXBManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import play.Play;
import play.Configuration;
import play.Logger;
import play.db.jpa.Transactional;
import service.oadr.EiEventService;
import xmpp.OADR2IQ;
import xmpp.OADR2PacketExtension;

import com.google.inject.Inject;

/**
 * XMPPService is used to establish and hold the XMPPConnection
 * to be used for sending and creating events
 * 
 * @author Jeff LaJoie
 *
 */
public class XmppService {

    private static volatile XmppService instance = null;
        
    static final String OADR2_XMLNS = "http://openadr.org/oadr-2.0a/2012/07";
    
    static JAXBManager jaxbManager;
    static{
        try {
            jaxbManager = new JAXBManager("org.enernoc.open.oadr2.model");
        } catch (JAXBException e) {
            Logger.error("Could not initialize JAXBManager in EiEvents", e);
        }
    }
    
    static EiEventService eiEventService = EiEventService.getInstance();
    private ConnectionConfiguration connConfig;
    
    private XMPPConnection vtnConnection;
    
    @Inject static PushService pushService;// = new PushService();
    
    private String vtnUsername;
    private String vtnPassword;
    private String resource;
    
    private Marshaller marshaller;
    DatatypeFactory xmlDataTypeFac;
    
    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Events");
    static EntityManager entityManager = entityManagerFactory.createEntityManager();
    
    /**
     * Constructor to establish the XMPP connection
     * 
     * @throws XMPPException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws JAXBException
     */
    public XmppService() throws XMPPException, JAXBException{
        //Add for debugging
        //Connection.DEBUG_ENABLED = true;
    	String domain ="Yangs-MacBook-Pro.local";
    	this.vtnUsername = "yangxiang";
    	this.vtnPassword = "password";
    	this.resource = "vtn";
    	this.connConfig = new ConnectionConfiguration(domain, 5222);
    	if ( this.vtnUsername != null && this.vtnPassword != null ) {
    		try {
	    		this.vtnConnection = connect(this.vtnUsername, this.vtnPassword, this.resource);
				}
				catch ( XMPPException ex ) {
					Logger.error( "Error connecting to XMPP server", ex );
				}
    	}
    	else Logger.warn("XMPP username or password not set, No XMPP service is available");
        
      this.marshaller = new JAXBManager().createMarshaller();
    }
    
    
    /**
     * Singleton getter for when Guice injection is not possible
     * 
     * @return the singleton XmppService
     */
    public static XmppService getInstance(){
        if(instance == null){
            synchronized(XmppService.class){
                if(instance == null){
                    try {
                        instance = new XmppService();
                    } catch (XMPPException e) {
                        Logger.error("XMPPException creating XMPPService.", e);
                    } catch (JAXBException e) {
                        Logger.error("JAXBException creating XMPPService.", e);
                    }
                }
            }
        }
        return instance;
    }
    
   
    /**
     * Adds a packet listener to the connection that handles all incoming packets
     * 
     * @return a PacketListener to be added to a connection
     */
    @Transactional
    public PacketListener oadrPacketListener(){
        return new PacketListener(){
            @Override
            @Transactional
            public void processPacket(Packet packet){
            	Logger.info("packet1/3");
                PacketExtension extension = packet.getExtension(OADR2_XMLNS);
				Unmarshaller unmarshaller = null;
				try {
					unmarshaller = jaxbManager.getContext().createUnmarshaller();
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Object payload = null;
				try {
					payload = unmarshaller.unmarshal(new ByteArrayInputStream(extension.toXML().getBytes()));
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Object eiResponse = eiEventService.handleOadrPayload(payload);
                if(eiResponse != null){
                	Logger.info("packet3/3");

                    sendObjectToJID(eiResponse, packet.getFrom());
                }
            }         
        };
    }
    
    /**
     * A packet filter to only accept packets with the OADR2_XMLNS
     * 
     * @return a PacketFilter to be added to a PacketListener
     */
    public PacketFilter oadrPacketFilter(){
        return new PacketFilter(){
            @Override
            public boolean accept(Packet packet){    
            	Logger.info("Filtering...");
                return packet.getExtension(OADR2_XMLNS) != null;
            }
        };
    }
    
    /**
     * Establish a connection for the XMPP server
     * 
     * @param username - Username to connect with
     * @param password - Password to connect with according to the username specified
     * @param resource - Resource to connect the XMPP to
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws XMPPException
     */
    public XMPPConnection connect(String username, String password, String resource) throws XMPPException {
       XMPPConnection connection = new XMPPConnection(connConfig);
       if(!connection.isConnected()){
           connection.connect();
           if(connection.getUser() == null && !connection.isAuthenticated()){
               connection.login(username, password, resource);
               connection.addPacketListener(oadrPacketListener(), oadrPacketFilter());
           }
       }
       return connection;
    }

    public boolean isConnected() {
			return this.vtnConnection != null && this.vtnConnection.isConnected();
		}
    
    /**
     * Sends an object to a JID
     * 
     * @param o - the Object to be sent
     * @param jid - the Jid to receive the object
     */
    public void sendObjectToJID(Object o, String jid){
        IQ iq = new OADR2IQ(new OADR2PacketExtension(o, marshaller));
        iq.setTo(jid);
        iq.setType(IQ.Type.SET);
        vtnConnection.sendPacket(iq);
    }
    
    /**
     * Send an object to a jid with the specified packetId
     * 
     * @param o - the Object to be sent
     * @param jid - the Jid to receive the object
     * @param packetId - the packetId the packet must contain
     */
    public void sendObjectToJID(Object o, String jid, String packetId){
        IQ iq = new OADR2IQ(new OADR2PacketExtension(o, marshaller));
        iq.setTo(jid);
        iq.setPacketID(packetId);
        iq.setType(IQ.Type.RESULT);
        vtnConnection.sendPacket(iq);
    }
    
}

package org.enernoc.oadr2.vtn;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;

import org.enernoc.open.oadr2.xmpp.JAXBManager;

import org.jivesoftware.smack.ChatManager
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Presence.Type
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import org.apache.commons.logging.LogFactory;
import org.enernoc.open.oadr2.xmpp.OADR2IQ;
import org.enernoc.open.oadr2.xmpp.OADR2PacketExtension;

/**
 * XMPPService is used to establish and hold the XMPPConnection
 * to be used for sending and creating events
 *
 * @author Jeff LaJoie
 *Converted from Play to Grails by Yang Xiang
 */
public class XmppService {
	private static final log = LogFactory.getLog(this)
	
		private static volatile XmppService instance = null;
			
		static final String OADR2_XMLNS = "http://openadr.org/oadr-2.0a/2012/07";
		
		private ConnectionConfiguration connConfig = new ConnectionConfiguration("Yangs-MacBook-Pro.local", 5222);
		
		private static XMPPConnection vtnConnection;
		
		private ChatManager chatManager;
		
		private MessageListener messageListener;
		
		def PushService pushService;// = new PushService();
		static EiEventService eiEventService = EiEventService.getInstance();
		
		//TODO add these to a config file like spring config or something, hardcoded for now
		private String vtnUsername = "yangxiang";
		private String vtnPassword = "password";
		private String JID = "bob@yangs-macbook-pro.local/Yangs-MacBook-Pro";
		
		private Marshaller marshaller;
		DatatypeFactory xmlDataTypeFac;
		
		
		/**
		 * Constructor to establish the XMPP connection
		 *
		 * @throws XMPPException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws JAXBException
		 */
		public XmppService() throws XMPPException, InstantiationException, IllegalAccessException, JAXBException{
			//Add for debugging
			//Connection.DEBUG_ENABLED = true;
			if(vtnConnection == null){
				vtnConnection = connect(vtnUsername, vtnPassword, "vtn");
			}
			
			JAXBManager jaxb = new JAXBManager();
			marshaller = jaxb.createMarshaller();
		}
	
	/**
	 * Singleton getter for when Guice injection is not possible
	 *
	 * @return the singleton XmppService
	 */
	public static XmppService getInstance(){
		System.out.println("instance");

		if(instance == null){
			synchronized(XmppService.class){
				if(instance == null){
					try {
						instance = new XmppService();
					} catch (XMPPException e) {
						log.error("XMPPException creating XMPPService.", e);
					} catch (InstantiationException e) {
						log.error("InstantiationException creating XMPPService.", e);
					} catch (IllegalAccessException e) {
						log.error("IllegalAccessException creating XMPPService.", e);
					} catch (JAXBException e) {
						log.error("JAXBException creating XMPPService.", e);
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
	//@Transactional
	public PacketListener oadrPacketListener(){
		return new PacketListener(){
	  //      @Override
		//    @Transactional
			public void processPacket(Packet packet){
				log.info("Listening to incoming packets1");
				def extension = packet.getExtension(OADR2_XMLNS) //as OADR2PacketExtension
				if (extension instanceof PacketExtension) {
				log.info("Listening to incoming packets2");
				}
				Object payload = eiEventService.handleOadrPayload(extension.getPayload());//I don't event understand how this works
				log.info("Listening to incoming packets3");
				
				if(payload != null){
					log.info("payload is not null");
					sendObjectToJID(payload, packet.getFrom());
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
				log.info("Filtering...");
				
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
	public XMPPConnection connect(String username, String password, String resource) throws InstantiationException, IllegalAccessException, XMPPException{
		System.out.println("xmpp Connection attempted");
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
	
	class MyMessageListener implements MessageListener {
		@Override
		public void processMessage(Chat chat, Message message) {
			System.out.println("I know you are but what am I")
		}
	}
	
	
	
	
	/**
	 * Sends an object to a JID
	 *
	 * @param o - the Object to be sent
	 * @param jid - the Jid to receive the object
	 */
	def sendObjectToJID(Object o, String jid){
		log.info("sendToJid");
		print("sendToJid")
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
	def sendObjectToJID(Object o, String jid, String packetId){
		System.out.println("sendToJid");

		IQ iq = new OADR2IQ(new OADR2PacketExtension(o, marshaller));
		iq.setTo(jid);
		iq.setPacketID(packetId);
		iq.setType(IQ.Type.RESULT);
		vtnConnection.sendPacket(iq);
	}
	
}

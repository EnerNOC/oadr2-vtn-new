package xmpp;

import javax.xml.bind.JAXBException;

import jaxb.JAXBManager;
import jaxb.PullUnmarshaller;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import play.Logger;

public class XMPPExtensionProvider implements PacketExtensionProvider, IQProvider {

    JAXBManager jaxb;
    PullUnmarshaller unmarshaller;

    public XMPPExtensionProvider() {
        try { 
            this.jaxb = new JAXBManager();
            this.unmarshaller = new PullUnmarshaller(jaxb.getContext());
        }
        catch ( JAXBException ex ) {
            throw new RuntimeException("Error initializing JAXB context",ex);
        }
    }
    
    public XMPPExtensionProvider(JAXBManager jaxb) {
        try {
            this.jaxb = jaxb;
            this.unmarshaller = new PullUnmarshaller(jaxb.getContext());
        }
        catch ( JAXBException ex ) {
            throw new RuntimeException("Error initializing JAXB context",ex);
        }
    }
    
    @Override
    public PacketExtension parseExtension(XmlPullParser pullParser) throws Exception {
        return new OADR2PacketExtension( unmarshaller.unmarshalSubTree(pullParser), this.jaxb );        
    }

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        return new OADR2IQ( parseExtension(parser) );
    }
}
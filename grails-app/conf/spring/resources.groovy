import com.enernoc.open.oadr2.vtn.*

beans = {
	
	def cfg = grailsApplication.config

	xmppService(XmppService) { b ->
		jid = cfg.xmppSvc.jid
		xmppPasswd = cfg.xmppSvc.passwd
		xmppResource = cfg.xmppSvc.resource
		xmppHost = cfg.xmppSvc.host
		xmppPort = cfg.xmppSvc.port
		xmppServiceName = cfg.xmppSvc.serviceName
		
		b.autowire = 'byType'
		b.initMethod = 'connect'
		b.destroyMethod = 'disconnect'
	}
	
	eiEventService(EiEventService) { b ->
		vtnID = cfg.vtnID
	}
    
    payloadPushService(PayloadPushService) { b ->
        vtnID = cfg.vtnID
    }
}

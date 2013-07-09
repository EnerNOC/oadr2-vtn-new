import com.enernoc.open.oadr2.vtn.*

beans = {
	
	def cfg = grailsApplication.config

	xmppService(XmppService) { b ->
		jid = cfg.xmppSvc.jid
		xmppPasswd = cfg.xmppSvc.passwd
		xmppResource = cfg.xmppSvc.resource
		xmppHost = cfg.xmppSvc.host ?: null
		xmppPort = cfg.xmppSvc.port ?: 5222
		xmppServiceName = cfg.xmppSvc.serviceName ?: null
		
		b.autowire = 'byType'
		b.initMethod = 'connect'
		b.destroyMethod = 'disconnect'
	}
	
	eiEventService(EiEventService) { b ->
		vtnID = cfg.vtnID
	}
}

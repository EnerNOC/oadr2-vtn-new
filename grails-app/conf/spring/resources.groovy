import com.enernoc.oadr2.vtn.EiEventService
import com.enernoc.oadr2.vtn.XmppService
import com.enernoc.oadr2.vtn.PushService

beans = {

	xmppService(XmppService) { b ->
		jid = 'tmnichols@gmail.com'
		xmppPasswd = 'uikaggruhgjljnhn'
		xmppResource = "vtn1"
		xmppHost = 'talk.google.com'
		xmppPort = 5222
		xmppServiceName = 'gmail.com'
		
		b.autowire = 'byType'
		b.initMethod = 'connect'
		b.destroyMethod = 'disconnect'
	}
	
	eiEventService(EiEventService) { bean ->
		vtnID = grailsApplication.config.vtnID		
	}
	pushService(PushService) { bean ->
		vtnID = grailsApplication.config.vtnID		
	}
}

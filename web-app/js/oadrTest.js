(function() {

	var self = {}

	self.getTemplates = function( evt ) {
		var svc = (evt.target).value
		$('#serviceName').attr('value',svc)
		$.ajax('templates/'+svc, {
			success : function(data,stat,xhr) {
				data.svc = svc
				$('#templates').html(ich.templatesNav(data))
					.find('a').bind('click',self.loadTemplate)
			}
		})
	}

	self.loadTemplate = function( evt ) {
//		console.log("loadTemplate!",evt)
		evt.preventDefault()
		$.ajax($(evt.target).closest('a').attr('href'), {
			dataType : 'text',
			success : function(data, stat, xhr) {
				console.debug('-----data:', typeof data)
				$('#templateTxt').attr('value',data)
				self.updateTemplate()
			}
		})
	}

	self.currentParams = {}

	self.updateTemplate = function() {
		var templateTxt = $('#templateTxt').attr('value')
		$('#requestTxt').attr('value',app.t(templateTxt,self.currentParams))
	}

	self.getVens = function( programID ) {
		$.ajax('vens', {
			data : {programID:programID},
			success : function(data,stat,xhr) {
				$('#vens').html( ich.vensNav(data) )
					.find('a').bind('click',self.selectVen)
			}
		})

	}

	self.getEvents = function( programID ) {
		$.ajax('events', {
			data : {programID:programID},
			success : function(data,stat,xhr) {
				$('#events').html( ich.eventsNav(data) )
					.find('a').bind('click',self.selectEvent)
			}
		})


	}

	self.getPrograms = function() {
		$.ajax('programs', {
			success : function(data,stat,xhr) {
				$('#programs').html( ich.programsNav(data) )
					.find('a').bind('click',self.selectProgram)
			}
		})
	}

	self.selectVen = function(evt) {
		var link = $(evt.target)
		self.currentParams.venID = link.attr('data-id')
		self.updateTemplate()
		evt.preventDefault()
	}

	self.selectEvent = function(evt) {

	}

	self.selectProgram = function(evt) {
		var link = $(evt.target)
		var programID = link.attr('data-id')
		self.currentParams.marketContext = link.attr('data-uri')
		self.updateTemplate()

		self.getEvents(programID)
		self.getVens(programID)
		evt.preventDefault()
	}

	self.sendRequest = function(responseTarget, evt) {
		console.log('Request params:',$(evt.target).serialize())
		$.ajax('execute', {
			type: 'POST',
			data: $(evt.target).serialize(),
			success : function(data,stat,xhr) {
				responseTarget.removeClass('error')
				responseTarget.attr('value',data.data)
			},
			failure : function(err,xhr) {
				console.error(err)
				responseTarget.addClass('error')
				responseTarget.attr('value',err)
			}
		})
		evt.preventDefault()
	}

	// find DOM elements & bind handlers
	self.init = function() {
		$('#templates a').bind('click',self.loadTemplate.curry($('#templateTxt')))
		$('#requestForm').bind('submit',self.sendRequest.curry($('#responseTxt')))
		$('#helpLink').bind('click',function(evt) {
			evt.preventDefault()
			$('#aboutDlg').modal()
		})
		$('#serviceSelect').bind('change',self.getTemplates)
		self.getPrograms()
	}

	if ( typeof( window ) != 'undefined' ) {
		window.oadrTest = self
	}
	else if ( typeof( module ) != 'undefined' ) {
		module.exports = self
	}
	return self
})()

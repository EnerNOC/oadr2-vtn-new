(function($,_,ich,app) {

	var self = {};
	if ( typeof window != 'undefined' )
		window.intervals = self;

	self.validateInterval = function(evt) {
		console.log( "Validating ", this, evt.target )
		
		var model = self.getInterval(this)
		var prev = self.getPreviousInterval(this)
		var err = null

		if ( ! self.validateNumber(model.level) )
			err = "Invalid level"

		if ( prev ) {
			if ( model.end < prev.end ) 
				err = "End must be after previous interval"
		}
		else {
			if ( model.end < self.event.start )
				err = "End must be after event start"
		}

		if ( model.end > self.event.end ) {
			console.log( model.end, self.event.end)
			err = "End must be before event end"
		}

		if ( err ) {
			console.log("Validation error", err, evt.target)
			$(evt.target).parents('.control-group')
				.addClass('error')
				.find('.help-inline').text(err)
		}
		else 
			$(evt.target).parents('.control-group')
				.removeClass('error')
				.find('.help-inline').text('')
	}


	self.getAllIntervals = function(section) {
		var intervals = []
		section.find('.intervalRow').each( function(i,row) {
			intervals.push( self.getInterval( $(row) ) )
		})
		return intervals
	}


	self.getPreviousInterval = function(row) {
		return self.getInterval(row.prev())
	}
	

	self.validateNumber = function(val) {
		return ! isNaN(parseInt(val))
	}


	self.getInterval = function(row) {
		if ( row.length < 1 ) return null
		
		console.debug('row:', row)
		var dateStr = row.find('.dp').val()
		var time = row.find('.tp').val()
		time = time.split(':')

		console.log(dateStr)
		var dateStr = dateStr.split('/') // ugh this sucks.
		var date = new Date(
			parseInt(dateStr[2]), // year
			parseInt(dateStr[1])-1, // month
			parseInt(dateStr[0]), // day
			parseInt(time[0]), // hours
			parseInt(time[1]), // minutes
			0 ) // seconds
		console.log( "parsed date is ", date )
		var level = row.find('input[name=val]').val().trim()
		var id = parseInt(row.find('input[name=intervalID]').val())
		return {
			end : date,
			level : level,
			id : id }
	}


	self.addSignal = function(signal) {
		if ( ! signal ) signal = {}
		var newSection = ich.signalTmpl(signal)

		newSection.find('.addIntervalBtn').on( 'click', function(evt) {
			evt.preventDefault();
			self.addInterval.bind(newSection)({endTime:self.event.end})
		})
		newSection.find('.remove').on('click',function(evt) {
			evt.preventDefault();
			newSection.remove();
		})
		newSection.find('input[name=name]').on('change',function(evt) {
			newSection.find('.nameLabel').text($(evt.target).val())
		})

		if ( signal.intervals )
			_.each(signal.intervals, self.addInterval.bind(newSection))

		$('#signals').append(newSection)
	}


	self.addInterval = function(interval) {
		if ( typeof interval == 'undefined' ) interval = {}
		console.log("Adding interval ")
		var newSection = ich.intervalTmpl(interval)
		var endTime = new Date( interval.endTime )

		newSection.find('input').on('blur',
				self.validateInterval.bind(newSection) )
		newSection.find('.tp').timepicker({ showMeridian: false })
			.data('timepicker').setTime( endTime.getHours() + ":" + endTime.getMinutes() );
		newSection.find('.dp').datepicker({ format: "dd/mm/yyyy" })
			.data('datepicker').setValue( endTime );
		newSection.find('.remove').on('click',function(evt) {
			evt.preventDefault();
			newSection.remove();
		})

		this.find('.intervals').first().append( newSection )
	}


	self.getAllSignals = function(form) {
		var signals = []
		form.find('.signalSection').each( function(i,sig) {
			signals.push( self.getSignal($(sig)) )
		})
		return signals
	}


	self.getSignal = function(section) {
		console.log("Section:",section)
		var sig = {}
		sig.name = section.find('.signalName').first().val()
		sig.type = section.find('.signalType').first().val()
		sig.id = parseInt(section.find('.signalID').first().val())
		sig.intervals = self.getAllIntervals(section)

		return sig
	}

	self.saveSignals = function(evt) {
		evt.preventDefault();

		var signals = self.getAllSignals(this)
		console.log("Sending", signals)

		var $N = window.app.n  

		$.ajax('', {
			type: 'post',
			data: JSON.stringify( signals ),
			contentType: 'application/json',
			processData: false,
			success: function(data,stat,xhr) {
				console.log('Signals success', xhr, stat, data)

				$('.alert-container').html(
					$N('div', {'class':'alert alert-success'}, "Saved! Hold on a sec...") )
				//redirect to new page:
				if ( data.location ) window.setTimeout(function() {
					 window.location = data.location
				}, 1000)
			},
			error: function(xhr,stat,err) {
				console.log("Signals save error", xhr, stat, err)
				var msg = "Unknown error"
				if (typeof err == 'object')
					if ( err.message ) msg = err.message
				else if ( typeof err == 'string' )
					msg = err
				$('.alert-container').html(
					$N('div', {'class':'alert alert-error'}, "Error: " + msg ) )
			}
		})
	}


	self.init = function(_event) {
		_event.start = new Date(_event.start)
		_event.end = new Date(_event.end)
		self.event = _event
		console.log("Event: ", _event);
		_.each(_event.signals, function(signal) {
			self.addSignal( signal )
		})

		$('#addSignalBtn').on('click',function(evt) {
			self.addSignal()
		});

		$('#signalsForm').on('submit', self.saveSignals.bind($('#signals')) );

		$(document).ajaxStart(function() {
			$('.busy-icon').show()
		}).ajaxComplete(function() {
			$('.busy-icon').hide()
		})

	};

	if ( typeof window != 'undefined' ) {
		window.intervals = self;
	}

	return self;
})(jQuery,_,ich,window.app);

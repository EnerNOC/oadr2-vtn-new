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

	self.getPreviousInterval = function(row) {
		return self.getInterval(row.prev())
	}
	
	self.validateNumber = function(val) {
		if ( typeof val == 'number' ) return true
		if ( typeof val == 'string' )
			return /[\d\.]+/.test( val )
		return false
	}

	self.getInterval = function(row) {
		if ( row.size() < 1 ) return null

		var dateStr = row.find('.dp').val()
		var time = row.find('.tp').val()
		time = time.split(':')

		console.log(dateStr)
		var dateStr = dateStr.split('/') // ugh this sucks.
		var date = new Date(
			Number.toInteger(dateStr[2]), // year
			Number.toInteger(dateStr[1])-1, // month
			Number.toInteger(dateStr[0]), // day
			Number.toInteger(time[0]), // hours
			Number.toInteger(time[1]), // minutes
			0 ) // seconds
		console.log( "parsed date is ", date )
		var level = row.find('input[name=val]').val().trim()
		var id = row.find('input[name=intervalID]').val()
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
	};

	if ( typeof window != 'undefined' ) {
		window.intervals = self;
	}

	return self;
})(jQuery,_,ich,window.app);

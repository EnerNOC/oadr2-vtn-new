(function($,_,ich,app) {
	var self = {};
	if ( typeof window != 'undefined' )
		window.intervals = self;

	self.validateInterval = function(evt) {
		console.log( "Validating ", evt.target )
	}

	self.addSignal = function(signal) {
		if ( ! signal ) signal = {}
		var newSection = ich.signalTmpl(signal)

		newSection.find('.addIntervalBtn').on( 'click', function(evt) {
			evt.preventDefault();
			self.addInterval.bind(newSection)({})
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
		
		newSection.find('input').on('blur',
				self.validateInterval.bind(newSection) )
		newSection.find('.tp').timepicker({ showMeridian: false });
		newSection.find('.dp').datepicker({ format: "dd/mm/yyyy" });
		newSection.find('.remove').on('click',function(evt) {
			evt.preventDefault();
			newSection.remove();
		})

		this.find('.intervals').first().append( newSection )
	}


	self.init = function(_event) {
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

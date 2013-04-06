(function() {

	var self = {}

	/**
	 * Node builder.  Call like:
	 * n('div',{class:'top'},'inner text') // or:
	 * n('div',{class:'top'},[n('p',{},'nested element'])
	 */
	self.n = function(e,attrs,inner) {
		if(typeof(e)=='string') e = document.createElement(e);
		if (attrs) for (var k in attrs) e.setAttribute(k,attrs[k]);
		if (inner) {
			if (typeof(inner)=='string') e.textContent = inner;
			else if (inner.call) inner.call(e);
			else for (var i in inner) e.appendChild(inner[i]);
		}
		return $(e);
	}

	/**
	 * Tweet tmpl:
	 * http://mir.aculo.us/2011/03/09/little-helpers-a-tweet-sized-javascript-templating-engine/
	 * Call it like this:
	 * t("Hello {who}!", { who: "JavaScript" });
	 */ 
	self.t = function(s,d) {
		for(var p in d)
			s=s.replace(new RegExp('{'+p+'}','g'), d[p]);
		return s;
	}

	self.scrollTo = function(selector) {
		var offset = $(selector).offset();
		$('html, body').animate({
				scrollTop: offset.top-30,
				scrollLeft: offset.left-20
		});
	}
	
	if (typeof jQuery !== 'undefined') {
		(function($) {
			$('#spinner').ajaxStart(function() {
				$(this).fadeIn();
			}).ajaxStop(function() {
				$(this).fadeOut();
			});
		})(jQuery);
	}

	if ( typeof(Function.prototype.partial) == "undefined" ) {
		Function.prototype.partial = function() {
			var fn = this, args = Array.prototype.slice.call(arguments);
			return function(){
				var arg = 0;
				for ( var i = 0; i < args.length && arg < arguments.length; i++ )
					if ( args[i] === undefined )
						args[i] = arguments[arg++];
				return fn.apply(this, args);
			};
		};
	}

	if ( typeof(Function.prototype.curry) == "undefined" ) {
		Function.prototype.curry = function() {
			var fn = this, args = Array.prototype.slice.call(arguments);
			return function() {
				return fn.apply(this, args.concat(
					Array.prototype.slice.call(arguments)));
			};
		};
	}

	if ( typeof( window ) != 'undefined' ) {
		window.app = self

		// Stub for conole.log for browsers that don't have support
		if ( ! window.console ) {
			var methods = "debug,error,exception,info,log,trace,warn".split(","),
					console = {},
					l = methods.length,
					fn = function() {}

			while (l--) console[methods[l]] = fn
			window.console = console
		}
	}
	else if ( typeof( module ) != 'undefined' ) {
		module.exports = self
	}

	return self
})()

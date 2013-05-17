<%! import grails.converters.JSON %>
<html>
<head>
<meta name="layout" content="bootstrap" />
<r:require module="eventIntervals" />
<style type='text/css'>
.signalSection {
  border-top: solid 1px #ccc
}
.intervals .help-inline {
  font-size: .8em;
  display:block
}
</style>
</head>
<body>
  
<div class='alert-container'>
<g:if test="${errors}">
  <div class="alert alert-error">
    <g:if test="${flash.message}">
      <p>${flash.message}</p>
    </g:if>
    <p>
      <g:each in="${errors}">
        ${it}<br/>
      </g:each>
    </p>
  </div>
</g:if>
<g:elseif test="${flash.message}">
  <div class="alert">${flash.message}</div>
</g:elseif>
</div>
  
<div class='row'>
  <div class='span12'>
		<g:form name='signalsForm' action="." class="form-horizontal">
		  <div class='row'>
		    <div class='span10'>
          <h1>Intervals for Event '<span class='eventName'>${event.eventID}</span>'</h1>
		    </div>
		    
		    <div class='span2'>
		      <p><span class='label'>Start: ${formatDate(format:'dd/MM/yyyy HH:mm', date:event.startDate)}</span></p>
          <p><span class='label'>End: ${formatDate(format:'dd/MM/yyyy HH:mm', date:event.endDate)}</span></p>
		    </div>
		  </div>
             
			<div class='row'>
	   		<div class='span10 '>
  
					<div id='signals'>
					 <!-- Signal templates inserted here -->
					</div>
				</div>
				<div class='span2'>
	        <div class='btn-group btn-group-vertical'>
	          <a href='#submit' id='submitBtn' class="btn btn-primary">
             <i class="icon-save"></i> Save!
	          </a>
	          <g:link controller="event" class="btn btn-inverse">Cancel</g:link>
            <a href='#addSignal' id='addSignalBtn' class='btn btn-success'>
              <i class='icon-plus'></i> Add Signal
            </a>
	        </div>
				</div>
			</div>		
		</g:form>
	</div>
</div>

<script type='text/html' id='signalTmpl'>
  <div class='row signalSection'>
    <div class='pull-right'>
	    <a href='#remove' class='remove'>&times; Remove</a>
	  </div>
	  
	  <h3>Signal: <span class='nameLabel'>{{name}}</span></h3>
	  
	  <div class="control-group">
	    <label class="control-label" for="name">Name: </label>
	    <div class='controls'>
	     <input type="text" class='signalName' name="name" value="{{name}}" placeholder="simple" />
     </div>
	  </div>
	  
	  <div class="control-group">
	    <label class="control-label" for="type">Type: </label>
      <div class='controls'>	    
		    <select name="type" class='signalType' placeholder="Select...">
		      <option value='LEVEL'>Level</option>
		      <option value='PRICE_RELATIVE'>Relative Price</option>
		    </select>
		  </div>
	  </div>
	  
	  <input type='hidden' name='signalID' class='signalID' value='{{id}}' />

    <div class='row'>
      <div class='span10'>
		    <div class='pull-right'>	  
			    <a href='#addInterval' class='addIntervalBtn btn btn-success btn-small'>
			      <i class='icon-plus'></i> Add Interval
			    </a>
		    </div>
		    <h4>Intervals</h4>
		    
			  <table class='table'>
			   <thead>
			     <tr>
			       <th>Value</th>
		         <th>End</th>
		         <th>Actions</th>
			     </tr>
			   </thead>
			   <tbody class='intervals'>
			     <!-- interval templates inserted here -->
			   </tbody>
			  </table>
		  </div>
	  </div>
	  
	  <hr />
  </div>
</script>

<script type='text/html' id='intervalTmpl'>
  <tr class='intervalRow'>
    <td>
      <div class='control-group'>
	      <input type="text" name="val" value="{{value}}" placeholder="1" class='input-small' />    
	      <input type='hidden' name='intervalID' value='{{id}}' />
        <span class='help-inline'></span>
      </div>
    </td>
    <td>
      <div class='bootstrap-timepicker control-group'>
        <div class='input-append'>
		      <g:textField name="endDate" class="dp input-small" value=""/>
		      <g:textField name="endTime" value="" class="tp input-mini" />
		      <span class="add-on"><i class="icon-time"></i></span>
	      </div>
	      <span class='help-inline'></span>
      </div>
    </td>
    <td>
      <a href='#removeInterval' class='remove alert-warning'>&times; Remove</a>
      <a href='#duplicateInterval'><i class='icon icon-copy'></i> Copy</a>
    </td>
  </tr>
</script>

<g:javascript>
$(function() {
  var _event = <%= eventJSON as JSON %>;

  window.intervals.intervalDeleteURL = '${createLink(controller:"eventInterval",action:"delete")}'
  window.intervals.signalDeleteURL = '${createLink(controller:"eventSignal",action:"delete")}'
  window.intervals.init(_event);
});
</g:javascript>
</body>
</html>
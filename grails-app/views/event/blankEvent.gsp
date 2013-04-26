<html>
<head>
<meta name="layout" content="bootstrap" />
<r:require module="formTime" />
<g:javascript>
  $(function() {
 	  $('.dp').datepicker({
 	  format: "dd/mm/yyyy"
 	  });
    $('.tp').timepicker({
    showMeridian: false
    });
  });
</g:javascript>
</head>
<body>
  
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

<h1>Create an Event</h1>
	<fieldset>
		<g:form action="newEvent" class="form-horizontal">
			<h3>Program</h3>
			<div class="control-group ${hasErrors(bean:event,field:'program', 'error')}">
				<label class="control-label" for="selectProgram">Program: </label>
				<div class="controls">
					<g:select name="programID" from="${programsList}" value="${event.program?.id}" optionKey="id" noSelection="[null:'- Choose a Program -']" id="selectProgram"/>
				</div>
			</div>
			<h3>Event Descriptor</h3>
			<div class="control-group ${hasErrors(bean:event,field:'eventID', 'error')}">
				<label class="control-label" for="inputEvent">Event ID: </label>
				<div class="controls">
					<input type="text" name="eventID" id="inputEvent" value="${event.eventID}"
						placeholder="Event ID" />
				</div>
			</div>
			<div class="control-group ${hasErrors(bean:event,field:'priority', 'error')}">
				<label class="control-label" for="inputPriority">Priority: </label>
				<div class="controls">
					<input type="text" name="priority" value="${event.priority}" placeholder="0" id="inputPriority" />
				</div>
			</div>
			<h3>Active Period</h3>
			<div class="control-group ${hasErrors(bean:event,field:'startDate', 'error')}">
				<label class="control-label" for="inputStartDate">Start Date: </label>
				<div class="controls">
				  <div class="input-append bootstrap-timepicker">
						<g:textField name="startDate"  value="${ g.formatDate(date:event.startDate, format:"dd/MM/yyyy") }" 
						 class="dp input-small" placeholder='dd/mm/yyyy' />
	          <g:textField name="startTime" value="${ g.formatDate(date:event.startDate, format:"HH:mm") }" 
	            class="tp input-mini" placeholder='hh:mm' />
	          <span class="add-on"><i class="icon-time"></i></span>
          </div>
				</div>
			</div>
			<div class="control-group ${hasErrors(bean:event,field:'endDate', 'error')}">
				<label class="control-label" for="inputEndDate">End Date: </label>
				<div class="controls">
				  <div class="input-append bootstrap-timepicker">
						<g:textField name="endDate" value="${ g.formatDate(date:event.endDate, format:"dd/MM/yyyy") }" 
						class="dp input-small" placeholder='dd/mm/yyyy' />
	          <g:textField name="endTime" value="${ g.formatDate(date:event.endDate, format:"HH:mm") }" 
	            class="tp input-mini" placeholder='hh:mm' />
	          <span class="add-on"><i class="icon-time"></i></span>
          </div>
				</div>
			</div>
			<div class="control-group">
				<div class="controls form-buttons">
					<input type="submit" value="Create!" class="btn btn-primary" />
					<g:link action="events" class="btn">Cancel</g:link>
				</div>
			</div>
		</g:form>
	</fieldset>
</body>
</html>
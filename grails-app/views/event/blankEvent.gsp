<html>
<head>
<meta name="layout" content="bootstrap" />
<r:require module="formTime" />
<g:javascript>
  $(function() {
 	  $('.dp').datepicker();
    $('.tp').timepicker();
    $('.tp1').timepicker();
  });
</g:javascript>
</head>
<body>
  
  <g:if test="${error}">
    <div class="alert alert-error">
      <g:if test="${flash.message}">
        <p>${flash.message}</p>
      </g:if>
      <p>
        <g:each in="${error}">
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
			<div class="control-group">
				<label class="control-label" for="selectProgram">Program: </label>
				<div class="controls">
					<g:select name="programName" from="${programList}" noSelection="[null:'- Choose a Program -']" id="selectProgram"/>
				</div>
			</div>
			<h3>Event Descriptor</h3>
			<div class="control-group">
				<label class="control-label" for="inputEvent">Event ID: </label>
				<div class="controls">
					<input type="text" name="eventID" id="inputEvent"
						placeholder="Event ID" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputPriority">Priority: </label>
				<div class="controls">
					<input type="text" name="priority" placeholder="0" id="inputPriority" />
				</div>
			</div>
			<h3>Active Period</h3>
			<div class="control-group">
				<label class="control-label" for="inputInterval">Intervals: </label>
				<div class="controls">
					<g:textField name="intervals" placeholder="1" id="inputInterval" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputStartDate">Start Date: </label>
				<div class="controls">
					<g:textField name="startDate" value="${date}" class="dp" id="inputStartDate" />
				</div>
			</div>
			<div class="control-group">
					<label class="control-label" for="inputStartTime">Start Time: </label>
				<div class="controls">
					<div class="input-append bootstrap-timepicker">
						<g:textField name="startTime" value="${time}" class="tp" id="inputStartTime" />
						<span class="add-on"><i class="icon-time"></i></span>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputEndDate">End Date: </label>
				<div class="controls">
					<g:textField name="endDate" value="${date}" class="dp" id="inputEndDate" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputEndTime">End Time: </label>
				<div class="controls">
					<div class="input-append bootstrap-timepicker">
						<g:textField name="endTime" value="${time}" id="inputEndTime" class="tp1" />
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
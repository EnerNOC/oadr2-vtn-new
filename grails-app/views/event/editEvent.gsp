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

  <h1>Edit Event: ${currentEvent.eventID}</h1>
	<fieldset>
		<g:form action="updateEvent" class="form-horizontal">
			<h3>Event Descriptor</h3>
			<div class="control-group">
				<label class="control-label" for="selectProgram">Program: </label>
				<div class="controls">
					<g:select name="programName" from="${programList}" value="${ currentEvent.marketContext.programName }" id="selectProgram" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputEvent">Event ID: </label>
				<div class="controls">
					<input type="text" name="eventID" id="inputEvent" value="${ currentEvent.eventID }" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputPriority">Priority: </label>
				<div class="controls">
					<input type="text" name="priority" value="${ currentEvent.priority }" id="inputPriority" />
				</div>
			</div>
			<h3>Active Period</h3>
			<div class="control-group">
				<label class="control-label" for="inputInterval">Intervals: </label>
				<div class="controls">
					<g:textField name="intervals" value="${ currentEvent.intervals }" id="inputInterval" />
        </div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputStartDate">Start Date: </label>
				<div class="controls">
					<g:textField name="startDate" value="${ g.formatDate(date:currentEvent.startDate, format:"dd/MM/yyyy") }" class="dp" id="inputStartDate" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputStartTime">Start Time: </label>
				<div class="controls">
					<div class="input-append bootstrap-timepicker">
						<g:textField name="startTime" value="${ g.formatDate(date:currentEvent.startDate, format:"HH:mm") }" class="tp" id="inputStartTime" />
						<span class="add-on">
							<i class="icon-time"></i>
						</span>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputEndDate">End Date: </label>
				<div class="controls">
					<g:textField name="endDate" value="${ g.formatDate(date:currentEvent.endDate, format:"dd/MM/yyyy") }" class="dp" id="inputEndDate" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="inputEndTime">End Time: </label>
				<div class="controls">
					<div class="input-append bootstrap-timepicker">
						<g:textField name="endTime" value="${ g.formatDate(date:currentEvent.endDate, format:"HH:mm") }" id="inputEndTime" class="tp" />
						<span class="add-on">
							<i class="icon-time"></i>
						</span>
					</div>
				</div>
			</div>
      <g:hiddenField name="id" value="${currentEvent.id}" />
			<div class="control-group">
				<div class="controls form-buttons">
					<input type="submit" value="Update!" class="btn btn-primary">
					<g:link action=" events " class="btn">Cancel</g:link>
				</div>
			</div>
		</g:form>
	</fieldset>
</body>
</html>
<html>
<head>
<meta name="layout" content="bootstrap" />
<r:require module="formTime" />
<g:javascript>
  $(function() {
    		$('.dp').datepicker();
    		$('#tp').timepicker();
         	$('#tp1').timepicker();
  });

  </g:javascript>
</head>
<body>

	<g:if test="${flash.message == "Fail"}">
		<div class="alert alert-error">
			<p>
				${error}
			</p>
		</div>
	</g:if>

	<h1>Create an Event</h1>
	</br>
	<fieldset>
		<g:form action="newEvent">
			<h3>Program</h3>
			<label>Program: </label>
			<g:select name="programName" from="${programList}"
				noSelection="[null:'- Choose a Program -']" />
			<h3>Event Descriptor</h3>
			<label>Event ID: </label>
			<input type="text" name="eventID" />
			<label>Priority</label>
			<input type="text" value="0" name="priority" />
			<h3>Active Period</h3>
			<label>Intervals: </label>
			<g:textField value="1" name="intervals" />
			<label>State Date: </label>
			<g:textField name="startDate" value="${date}" class="dp" />

			<div class="input-append bootstrap-timepicker">
				<label>Start Time: </label>
				<g:textField name="startTime" value="${time}" class="input-small"
					id="tp" />
				<span class="add-on"><i class="icon-time"></i></span>
			</div>
			<label>End Date: </label>
			<g:textField name="endDate" value="${date}" class="dp" />
			<div class="input-append bootstrap-timepicker">
				<label>End Time: </label>
				<g:textField name="endTime" value="${time}" class="input-small"
					id="tp1" />
				<span class="add-on"><i class="icon-time"></i></span>
			</div>
			<div class="actions">
				<input type="submit" value="Create this event" class="btn primary">
				or
				<g:link action:"events" class="btn">Cancel</g:link>
			</div>

		</g:form>

	</fieldset>



</body>
</html>
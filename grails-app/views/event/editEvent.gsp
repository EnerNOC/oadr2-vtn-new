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
                <g:each in="${error}">
                    ${it}
                    </br>
                </g:each>
            </p>
        </div>
    </g:if>
	<h1>Edit Event: ${currentEvent.eventID}</h1>
	</br>
    <fieldset>
        <g:form action="updateEvent" class="form-horizontal">
            <h3>Event Descriptor</h3>
            <div class="control-group">
                <label class="control-label" for="selectProgram">Program: </label>
                <div class="controls">
                    <g:select name="programName" from="${programList}"
                        value="${ currentEvent.programName }" id="selectProgram"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="inputEvent">Event ID: </label>
                <div class="controls">
                    <input type="text" name="eventID" id="inputEvent"
                        value="${ currentEvent.eventID }" />
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="inputPriority">Priority</label>
                <div class="controls">
                    <input type="text" name="priority" value="${ currentEvent.priority }"
                        id="inputPriority" />
                </div>
            </div>
            <h3>Active Period</h3>
            <div class="control-group">
                <label class="control-label" for="inputInterval">Intervals:
                </label>
                <div class="controls">
                    <g:textField name="intervals" value="${ currentEvent.intervals }" id="inputInterval" />
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="inputStartDate">State
                    Date: </label>
                <div class="controls">
                    <g:textField name="startDate" value="${ currentEvent.startDate }" class="dp" id="inputStartDate" />
                </div>
            </div>
            <div class="control-group">
                    <label class="control-label" for="inputStartTime">Start
                        Time: </label>
                <div class="controls">
                    <div class="input-append bootstrap-timepicker">
                        <g:textField name="startTime" value="${ currentEvent.startTime }" class="tp"
                            id="inputStartTime" />
                        <span class="add-on"><i class="icon-time"></i></span>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="inputEndDate">End Date:
                </label>
                <div class="controls">
                    <g:textField name="endDate" value="${ currentEvent.endDate }" class="dp"
                        id="inputEndDate" />
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="inputEndTime">End Time: </label>
                <div class="controls">
                    <div class="input-append bootstrap-timepicker">
                        <g:textField name="endTime" value="${ currentEvent.endTime }" id="inputEndTime"
                            class="tp1" />
                        <span class="add-on"><i class="icon-time"></i></span>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <div class="actions">
                        <input type="submit" value="Update this event" class="btn primary">
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <g:link action:"events" class="btn">Cancel</g:link>
                    </div>
                </div>
            </div>
        </g:form>
    </fieldset>
</body>
</html>
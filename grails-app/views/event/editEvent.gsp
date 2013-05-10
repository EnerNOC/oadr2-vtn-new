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
    if(document.getElementById("dateDisable") != null) {    
       document.getElementById("inputStartTime").setAttribute("disabled");
       document.getElementById("inputStartDate").setAttribute("disabled");
       document.getElementById("inputStartDate").className = "";
       document.getElementById("inputStartTime").className= "";
     }
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
  <g:if test="${currentEvent.status == "Active" }">
    <div id="dateDisable"></div>
  </g:if>
  
  <h1>Edit Event: ${currentEvent.eventID}</h1>
	<fieldset>
		<g:form action="updateEvent" class="form-horizontal">
			<h3>Event Descriptor</h3>
			<div class="control-group ${hasErrors(bean:currentEvent,field:'program', 'error')}">
				<label class="control-label" for="selectProgram">Program: </label>
				<div class="controls">
					<input type="text" value="${ currentEvent.program }" id="selectProgram" disabled />
				</div>
			</div>
			<div class="control-group ${hasErrors(bean:currentEvent,field:'eventID', 'error')}">
				<label class="control-label" for="inputEvent">Event ID: </label>
				<div class="controls">
					<input type="text" name="eventID" id="inputEvent" value="${ currentEvent.eventID }" />
				</div>
			</div>
			<div class="control-group ${hasErrors(bean:currentEvent,field:'priority', 'error')}">
				<label class="control-label" for="inputPriority">Priority: </label>
				<div class="controls">
					<input type="text" name="priority" value="${ currentEvent.priority }" id="inputPriority" />
				</div>
			</div>
			<h3>Active Period</h3>
			<div class="control-group ${hasErrors(bean:currentEvent,field:'startDate', 'error')}">
				<label class="control-label" for="inputStartDate">Start Date: </label>
				<div class="controls">
          <div class='input-append bootstrap-timepicker'>
						<g:textField name="startDate" value="${ g.formatDate(date:currentEvent.startDate, format:"dd/MM/yyyy") }" 
						 class="dp input-small" placeholder='dd/mm/yyyy' />
	          <g:textField name="startTime" value="${ g.formatDate(date:currentEvent.startDate, format:"HH:mm") }" 
	            class="tp input-mini" placeholder='hh:mm' />
          </div>
				</div>
			</div>
			<div class="control-group ${hasErrors(bean:currentEvent,field:'endDate', 'error')}">
				<label class="control-label" for="inputEndDate">End Date: </label>
				<div class="controls">
				  <div class='input-append'>
						<g:textField name="endDate" value="${ g.formatDate(date:currentEvent.endDate, format:"dd/MM/yyyy") }" 
						 class="dp input-small" />
	          <g:textField name="endTime" value="${ g.formatDate(date:currentEvent.endDate, format:"HH:mm") }" 
	            class="tp input-mini" placeholder='hh:mm' />
	          <span class="add-on"><i class="icon-time"></i></span>
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
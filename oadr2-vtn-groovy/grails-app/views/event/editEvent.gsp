<html>
<head>
<meta name="layout" content="bootstrap"/>
      <r:require module="formTime"/> 
      <g:javascript>
  $(function() {
    		$('.dp').datepicker();
    		$('#tp').timepicker();
         	$('#tp1').timepicker();
  });

  </g:javascript>
</head>
<body>
        <h1>Edit Event: ${currentEvent.eventID}</h1>
        </br>
        <fieldset>
			<g:form action="updateEvent">
			<h3>Program</h3>
	         <label>Program: </label>   
				<g:select name="programName" from="${programList}" value="${currentEvent.programName}"/>
		    <h3>Event Descriptor</h3>    
		    	<label>Event ID: </label><input type="text" value="${currentEvent.eventID}" name="eventID"/>
		    	<label>Priority</label><input type="text" value="${currentEvent.priority}" name="priority"/>			
			<h3>Active Period</h3>
			<label>Intervals: </label><g:textField value="${currentEvent.intervals}" name="intervals"/>			
			<label>State Date: </label><g:textField name="startDate" value="${currentEvent.startDate}" class="dp"/>	
			  
  <div class="input-append bootstrap-timepicker">			  
			<label>Start Time: </label><g:textField name="startTime" value="${currentEvent.startTime}" class="input-small" id="tp" />	
            <span class="add-on"><i class="icon-time"></i></span>
        </div>			
			<label>End Date: </label><g:textField name="endDate" value="${currentEvent.endDate}"  class="dp"/>	
  <div class="input-append bootstrap-timepicker">			  
			<label>End Time: </label><g:textField name="endTime" value="${currentEvent.endTime}" class="input-small" id="tp1" />	
            <span class="add-on"><i class="icon-time"></i></span>
        </div>	
        <g:hiddenField name="id" value="${currentEvent.id}" />
        <div class="actions">
            <g:actionSubmit  value="Update Event"  class="btn primary"/> or 
            <g:link action:"events" class="btn">Cancel</g:link>     
        </div>        
        
			</g:form>
				
		</fieldset>
		


</body>
</html>
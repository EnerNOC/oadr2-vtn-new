<html>
<head>
<meta name="layout" content="bootstrap" />
</head>
<body> 
	<h3>Select Event:</h3>
		<g:form action="venStatuses" class="form-inline form-buttons">
		      <g:select name="eventID" from="${eventList}" value="${event}" class="btn"/>
		      <input type="submit" value="Change Event" class="btn btn-primary"/>
		</g:form>
	<h3>VEN Status</h3>
	<g:render template="venStatusTableTemplate"
		model="[venStatusList: venStatusList]" />
</body>
</html>

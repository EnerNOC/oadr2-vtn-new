<html>
<head>
	<meta name="layout" content="bootstrap"/>
</head>
<body>
		<g:if test="${flash.message == "Fail"}">
			<div class="alert alert-error">
			<p>
		 ${error}
			</p>
			</div>
			</g:if>	
	<h1>
	Event
	</h1>
	<g:if test="${flash.message=="Success"}">
	<p>Success: Your event has been created</p>
	</g:if>
		<div class="span4 offset8">
		<p>
		<g:link action="blankEvent" class="btn">Create Event</g:link>
		</p>
		</div>
	<g:render template="eventTableTemplate" model="[eventList:eventList]"/>
		



</body>
</html>
<html>
<head>
<meta name="layout" content="bootstrap" />
</head>
<body>
	<g:if test="${error}">
		<div class="alert alert-error">
			<p>${error}</p>
		</div>
	</g:if>
	<h1>Event</h1>
	<g:if test="${flash.message}">
    <div class="alert">
		  <p>${flash.message}</p>
		</div>
	</g:if>
	<div class="span4 offset8">
		<p>
			<g:link action="blankEvent" class="btn">Create Event</g:link>
		</p>
	</div>
	
	<g:render template="eventTableTemplate" model="[eventList:eventList]" />

</body>
</html>
<html>
<head>
<meta name="layout" content="bootstrap" />
</head>
<body>
	<div class="row-fluid">
		<div class="span4">
			<h1>VENs</h1>
		</div>
		<div class="span4 offset4">
			<p>
				<g:link action="blankVEN" class="btn">Create VEN</g:link>
			</p>
		</div>
	</div>
	<g:if test="${flash.message == "Success"}">
		<div class="alert alter-success">
			<p>Your VEN has been created</p>
		</div>
	</g:if>
	<g:if test="${flash.message == "Fail"}">
		<div class="alert alert-error">
			<p>
				${error}
			</p>
		</div>
	</g:if>
    <g:render template="venTableTemplate" model="[venList:venList]"/>
</body>

</html>
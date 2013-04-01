<html>
<head>
<meta name="layout" content="bootstrap" />
</head>
<body>
	<div class="row-fluid">
		<div class="span4">
			<h1>Program</h1>
		</div>
		<div class="span4 offset4">
			<p>
				<g:link action="blankProgram" class="btn">Create Program</g:link>
				</button>
			</p>
		</div>
	</div>
	<g:if test="${flash.message == "Success"}">
		<div class="alert alter-success">
			<p>Your program has been created</p>
		</div>
	</g:if>
	<g:if test="${flash.message == "Fail"}">
		<div class="alert alert-error">
			<p>
				${error}
			</p>
		</div>
	</g:if>
    <g:render template="programTableTemplate" model="[programList:programList]"/>
</body>

</html>
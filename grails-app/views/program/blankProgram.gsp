<html>
<head>
<meta name="layout" content="bootstrap" />
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
	
	<h3>Market Context</h3>
	<g:form action="newProgram" class="form-horizontal">
		<div class="control-group">
			<label class="control-label" for="inputProgramName">Program Name: </label>
			<div class="controls">
				<input type="text" name="programName" placeholder="My Name" id="inputProgramName" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="inputProgramURI">URI: </label>
			<div class="controls">
				<input type="text" name="programURI" placeholder="My URI" id="inputProgramURI" />
			</div>
		</div>
		<div class="control-group">
			<div class="controls form-buttons">
				<input type="submit" value="Create!" class="btn btn-primary">
				<g:link action="programs" class="btn">Cancel</g:link>
			</div>
		</div>
	</g:form>
</body>
</html>
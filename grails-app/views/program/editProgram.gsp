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

	<h3>Edit Program: ${ currentProgram.programName }</h3>
	<g:form action="updateProgram" class="form-horizontal">
		<div class="control-group">
			<label class="control-label" for="inputProgramName">Program
				Name: </label>
			<div class="controls">
				<input type="text" name="programName" value="${ currentProgram.programName }" id="inputProgramName" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="inputProgramURI">URI: </label>
			<div class="controls">
				<input type="text" name="programURI" value="${ currentProgram.programURI }" id="inputProgramURI" />
			</div>
		</div>
		<g:hiddenField name="id" value="${currentProgram.id}" />
		<div class="control-group">
			<div class="controls form-buttons">
			  <input type="submit" value="Update!" class="btn btn-primary"/>
				<g:link action="programs" class="btn">Cancel</g:link>
			</div>
		</div>
	</g:form>
</body>
</html>
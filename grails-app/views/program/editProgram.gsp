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
	<g:form action="updateProgram">
		<label>Program Name: </label>
		<input type="text" name="programName" value="${ currentProgram.programName }"/>
		</br>
		<label>URI: </label>
		<input type="text" name="programURI" value="${ currentProgram.programURI }"/>
		</br>
		<g:hiddenField name="id" value="${currentProgram.id}" />
		<div class="actions">
			<input type="submit" value="Update this program" class="btn primary">
			or
			<g:link action="programs" class="btn">
            Cancel
            </g:link>
		</div>
	</g:form>

</body>

</html>
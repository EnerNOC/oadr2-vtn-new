<html>
<head>
<meta name="layout" content="bootstrap" />
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

	<h3>Edit Program: ${ currentProgram.programName }</h3>
	<g:form action="updateProgram" class="form-horizontal">
		<div class="control-group ${hasErrors(bean:currentProgram,field:'programName', 'error')}">
			<label class="control-label" for="inputProgramName">Program Name: </label>
			<div class="controls">
				<input type="text" name="programName" value="${ currentProgram.programName }" id="inputProgramName" />
			</div>
		</div>
		<div class="control-group ${hasErrors(bean:currentProgram,field:'programURI', 'error')}">
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
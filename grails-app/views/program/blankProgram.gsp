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
	
	<h3>Market Context</h3>
	<g:form action="newProgram" method="POST" class="form-horizontal">
		<div class="control-group ${hasErrors(bean:program,field:'programName', 'error')}">
			<label class="control-label" for="inputProgramName">Program Name: </label>
			<div class="controls">
				<input type="text" name="programName" placeholder="My Name" id="inputProgramName"
				  value='${program.programName}' />
			</div>
		</div>
		<div class="control-group ${hasErrors(bean:program,field:'programURI', 'error')}">
			<label class="control-label" for="inputProgramURI">URI: </label>
			<div class="controls">
				<input type="text" name="programURI" placeholder="My URI" id="inputProgramURI" 
				  class="${hasErrors(bean:program,field:'programURI', 'error')}"
				  value='${program.programURI}'/>
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
<html>
<head>
<meta name="layout" content="bootstrap" />
<link rel="stylesheet"
	href="${resource(dir: 'js', file: 'bootstrap-datepicker.js')}"
	type="text/javascript">
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
	<h3>Create a VEN</h3>
	<g:form action="newVEN">
		<label>Program: </label>
		<g:select name="programID" from="${programsList}"
			noSelection="[null:'- Choose a Program -']" />
		<label>VEN Name: </label>
		<input type="text" name="venName" />
		</br>
		<label>VEN ID: </label>
		<input type="text" name="venID" />
		</br>
		<label>Client URI: </label>
		<input type="text" name="clientURI" />
		</br>
		<div class="actions">
			<input type="submit" value="Create this VEN" class="btn primary">
			or
			<g:link action="vens" class="btn">
            Cancel
            </g:link>
		</div>
	</g:form>

</body>

</html>
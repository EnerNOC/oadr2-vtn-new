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
    <h3>Edit VEN: ${ currentVen.venID }</h3>
    <g:form action="updateVEN">
        <label>Program: </label>
        <g:select name="programID" from="${programsList}" value="${ currentVen.programID }"/>
        <label>VEN Name: </label>
        <input type="text" name="venName" value="${ currentVen.venName }"/>
        </br>
        <label>VEN ID: </label>
        <input type="text" name="venID" value="${ currentVen.venID }"/>
        </br>

        <div class="clearfix">
            <label>Node Type</label>
            <div class="input">
                <input type="radio" name="group1" value="Pull" id="Pull"
                    onclick="inputDisable()">Pull &nbsp&nbsp <input
                    type="radio" name="group1" value="Push" id="Push" checked
                    onclick="inputEnable()">Push <br></br>
            </div>
        </div>
        <label>Client URI: </label>
		<input id="clientURI" type="text" name="clientURI"
			value="${ currentVen.clientURI }" />
		</br>
		<g:hiddenField name="id" value="${currentVen.id}" />
		<div class="actions">
            <input type="submit" value="Update this VEN" class="btn primary">
			or
			<g:link action="vens" class="btn">
            Cancel
            </g:link>
		</div>
	</g:form>

    <g:javascript>          
        function inputEnable() {        
            document.getElementById("clientURI").removeAttribute("disabled");            
        }          
        function inputDisable() {        
            document.getElementById("clientURI").setAttribute("disabled");
        }
    </g:javascript>

</body>

</html>
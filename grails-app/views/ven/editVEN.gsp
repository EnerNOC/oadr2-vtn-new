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
    <g:form action="updateVEN" class="form-horizontal">
        <div class="control-group">
            <label class="control-label" for="selectProgram">Program: </label>
            <div class="controls">
                <g:select name="programID" from="${programsList}"
                    value="${ currentVen.programID }" id="selectProgram" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="inputVenName">VEN Name: </label>
            <div class="controls">
                <input type="text" name="venName" value="${ currentVen.venName }"
                    id="inputVenName" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="inputVenID">VEN ID: </label>
            <div class="controls">
                <input type="text" name="venID" value="${ currentVen.venID }" id="inputVenID" />
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="radio"> 
                <input type="radio" name="group1"
                    value="Pull" id="Pull" onclick="inputDisable()">Node Pull
                </label> 
                <label class="radio"> 
                <input type="radio" name="group1"
                    value="Push" id="Push" checked onclick="inputEnable()">Node
                    Push
                </label>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="inputClientURI">Client URI:
            </label>
            <div class="controls">
                <input id="clientURI" type="text" name="clientURI"
                    value="${ currentVen.clientURI }" id="inputClientURI" />
            </div>
        </div>
		<g:hiddenField name="id" value="${currentVen.id}" />
		<div class="control-group">
            <div class="controls">
                <div class="actions">
                    <input type="submit" value="Update this VEN" class="btn primary">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <g:link action="vens" class="btn">
            Cancel
            </g:link>
                </div>
            </div>
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
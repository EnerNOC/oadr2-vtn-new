<html>
	<head>
	<meta name="layout" content="bootstrap"/>
		</head>
		<body>
			<h3>Market Context</h3>
			<g:form action="newProgram">
				<label>Program Name: </label><input type="text" name="programName"/>
				</br>
				<label>URI: </label><input type="text" name="programURI"/>
				</br>
			 <div class="actions">
            <input type="submit" value="Create this program" class="btn primary"> or 
            <g:link action="programs" class="btn">
            Cancel
            </g:link>
        </div>
      </g:form>
        
		</body>
		
</html>
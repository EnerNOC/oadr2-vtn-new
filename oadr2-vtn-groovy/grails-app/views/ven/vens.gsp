<html>
	<head>
	<meta name="layout" content="bootstrap"/>
		</head>
		<body>
		<div class="row-fluid">
		<div class="span4">
		<h1>VENs</h1>
		</div>
		<div class="span4 offset4">
		<p>
		<g:link action="blankVEN" class="btn">Create VEN</g:link>
		</p>
		</div>
		</div>
		<g:if test="${flash.message == "Success"}">
			<div class="alert alter-success">
			<p> 
			Your VEN has been created
			</p>
			</div>
			</g:if>
		<g:if test="${flash.message == "Fail"}">
			<div class="alert alert-error">
			<p>
		 ${error}
			</p>
			</div>
			</g:if>	
        <table class="table table-striped">
            <thead>
                <tr>
                	<th>VEN Name</th>
                	<th>VEN ID</th>
                	<th>Client URI</th>
                	<th>Program Name</th>    
                </tr>
            </thead>
            <tfoot>
            </tfoot>
            <tbody>
				<g:each var="ven" in="${venList}">
            	<tr>
	            	<td>${ven.venName}</td>
	            	<td>${ven.venID}</td>
	                <td>${ven.clientURI}</td>
	            	<td>${ven.programID}</td>	  	  
	            	<td>
	            	<g:form action="deleteVEN" params="[id : ven.id]">
						<input type="submit" value="Delete" class="btn btn-danger" onClick="return confirmSubmit()">
					</g:form>
					</td>
            	</tr>
            	</g:each>     	
            </tbody>
        </table>
		</body>
		
</html>
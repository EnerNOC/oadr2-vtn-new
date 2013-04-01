
<table class="table table-striped">
	<thead>
		<tr>
			<th>Program Name</th>
			<th>Context URI</th>
		</tr>
	</thead>
	<tfoot>
	</tfoot>
	<tbody>
		<g:each var="program" in="${programList}">
			<tr>
				<td>
					${program.programName}
				</td>
				<td>
					${program.programURI}
				</td>
				<td><g:form controller="Program" action="deleteProgram" params="[id : program.id]">
						<input type="submit" value="Delete" class="btn btn-danger"
							onClick="return confirmSubmit()">
					</g:form></td>
			</tr>
		</g:each>
	</tbody>
</table>
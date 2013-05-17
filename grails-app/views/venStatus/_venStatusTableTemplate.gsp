<html>
<table class="table table-striped">
	<thead>
		<tr>
			<th>VEN ID</th>
			<th>Event ID</th>
			<th>Program</th>
			<th>Status</th>
			<th>Last Response</th>
			<th></th>
		</tr>
	<tfoot></tfoot>
	<tbody>
		<g:each in="${venStatusList}" var="venStatus">
			<tr>
				<td>
					${venStatus.ven.venID}
				</td>
				<td>
					${venStatus.event.eventID}
				</td>
				<td>
					${venStatus.event.program.name}
				</td>
				<td><g:if test="${venStatus.getStatusText() == "Opt Out"}">
						<a disabled="disabled" class="btn btn-danger">Opted Out</a>
					</g:if> <g:else>
						<g:if test="${venStatus.getStatusText() == "Opt In"}">
							<a disabled="disabled" class="btn btn-success">Opted In</a>
						</g:if>
						<g:else>
						  <a disabled="disabled" class="btn">${venStatus.getStatusText() }</a>
						</g:else>
					</g:else></td>
				<td>
					${venStatus.displayTime()}
				</td>
				<td><g:form action="deleteStatus" params="[id : venStatus.id]">
						<input type="submit" value="Delete" class="btn btn-danger"
							onClick="return confirmSubmit()">
					</g:form></td>
			</tr>
		</g:each>

	</tbody>
</table>
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
				<td>${venStatus.venID}</td>
				<td>${venStatus.eventID}</td>
				<td>${venStatus.program}</td>
				<td>
				<g:if test="${venStatus.optStatus == "OPT_OUT"}">
						<a disabled="disabled" class="btn danger">Opted Out</a>
			    </g:if>
			    <g:else>
			    	<g:if test="${venStatus.optStatus == "OPT_IN"}">
							<a disabled="disabled" class="btn success">Opted In</a>
					</g:if>
					<g:else>
							<g:if test="${venStatus.optStatus == "Pending 1"}">
								<a disabled="disabled" class="btn">Pending 1</a>
							</g:if>
							<g:else>
								<a disabled="disabled" class="btn">Pending 2</a>
							</g:else>
						</g:else>
					</g:else>
				</td>
				<td>${venStatus.displayTime()}</td>
				<td>
				     <g:form action="deleteStatus" params="[id : venStatus.id]">
						<input type="submit" value="Delete" class="btn btn-danger" onClick="return confirmSubmit()">
					</g:form>
				</td>	
			</tr>
		</g:each>
		
	</tbody>
</table>

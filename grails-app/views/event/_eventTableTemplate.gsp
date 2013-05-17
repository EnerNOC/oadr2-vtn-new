
<table class="table table-striped">
	<thead>
		<tr>
			<th>Event ID</th>
			<th>Priority</th>
			<th>Status</th>
			<th>Start</th>
			<th>End</th>
			<th>Market Context</th>
			<th>Response Required</th>
		</tr>
	</thead>
	<tfoot>
	</tfoot>
	<tbody>
		<g:each var="event" in="${eventList}">
			<tr>
				<td><g:link controller="VenStatus" action="venStatuses"
						params="[eventID:event.eventID]">
						${event.eventID}
					</g:link></td>
				<td>
					${event.priority}
				</td>
				<td>
					${event.status}
				</td>
				<td>
				  <g:formatDate format='dd/MM/yyyy HH:mm' date='${event.startDate}' />
				</td>
				<td>
          <g:formatDate format='dd/MM/yyyy HH:mm' date='${event.endDate}' />
				</td>
				<td>
					${event.program.name}
				</td>
				<td>
          ${ event.responseRequired }
				</td>
				<g:if test="${ event.endDate > new Date() }">
					<td><g:link controller="Event" action="editEvent" params="[id: event.id]" class="btn btn-mini">Edit</g:link>
					</td>
					<td><g:link controller="Event" action="cancelEvent" params="[id: event.id]"
							class="btn btn-mini btn-inverse">Cancel</g:link></td>
				</g:if>
				<g:else>
          <td><div disabled class="btn btn-mini">Edit</div>
          </td>
          <td><div disabled class="btn btn-mini btn-inverse">Cancel</div>
          </td>			
				</g:else>
				<td><g:link controller="Event" action="deleteEvent" params="[id: event.id]"
            class="btn btn-mini btn-danger">Delete</g:link></td>
			</tr>
		</g:each>
	</tbody>
</table>
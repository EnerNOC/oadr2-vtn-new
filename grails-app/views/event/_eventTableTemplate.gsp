
<table class="table table-striped">
	<thead>
		<tr>
			<th>Event ID</th>
			<th>Signals</th>
			<th>Priority</th>
			<th>Status</th>
			<th>Start</th>
			<th>End</th>
			<th>Program</th>
			<th>Actions</th>
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
					</g:link>
				</td>
				<td>
				  ${event.signals.size()}
				  <g:link mapping="eventSignal" params="[eventID: event.id]">
				    <i class='icon-edit' title='Edit Intervals'></i></g:link>
				</td>
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
				<td><g:link controller="Event" action="editEvent" params="[id: event.id]"
						class="btn">Edit</g:link></td>
				<td><g:link controller="Event" action="deleteEvent" params="[id: event.id]"
						class="btn btn-danger">Delete</g:link></td>
				<td><g:link controller="Event" action="cancelEvent" params="[id: event.id]"
						class="btn btn-warn">Cancel</g:link>
			</tr>
		</g:each>
	</tbody>
</table>
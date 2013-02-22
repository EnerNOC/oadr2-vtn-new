
        <table class="table table-striped">
            <thead>
                <tr>
                	<th>Event ID</th>
                	<th>Priority</th>
                	<th>Start Time</th>
                	<th>Duration</th>
                	<th>Market Context</th>
                </tr>
            </thead>
            <tfoot>
            </tfoot>
            <tbody>            	
				<g:each var="event" in="${eventList}">
					<tr>
						<td>${event.eventID}</td>
						<td>${event.priority}</td>
						<td>${event.startDate} @ </br>${event.startTime}</td>
						<td>${event.duration}min</td>
						<td>${event.programName}</td>							
						<td>
							<g:link action="editEvent" params="[id: event.id]" class="btn">
							Edit</g:link>
						</td>
						<td>
							<g:link action="deleteEvent" params="[id: event.id]" class="btn btn-danger">Delete</g:link>

						</td>	
						<td>
							<g:link action="cancelEvent" params="[id: event.id]" class="btn btn-inverse">Cancel</g:link>
					</tr>
				</g:each>           	
            </tbody>
        </table>
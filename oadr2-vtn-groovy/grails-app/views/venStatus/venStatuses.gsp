<html>
<header>
	<meta name="layout" content="bootstrap"/>
</header>
	<body>
	<h3>Select Event:</h3>
		<g:each in="${eiEvent}">
		<g:if test="${program == p.getEventDescriptor().getEventID()}">
		<option selected="selected" value="${program}">
		<g:link action="venStatuses" params="[program:program]">${program}</g:link>
		</option>
		</g:if>
		<g:else>
		<option value="${eiEvent.programName}">
		<:g:link action="venStatuses" params="[program:eiEvent.programName]">${eiEvent.programName}</:g:link>
		</option>
		</g:else>
		</g:each> 
	</select>
	<h3>VEN Status</h3>	
	<g:render template="venStatusTableTemplate" model="[venStatus: venStatus]"/>
	<!--<div id=venStatusTable></div>	

	<script>	
		$(document).ready(function(){	
			var loadOadrEvents = function(){
				$.post('@routes.VENStatuses.renderAjaxTable(program)',
						"",
						function(data){
							$('#venStatusTable').empty();
							$('<div class="refreshing"><img src=@routes.Assets.at("/images/loader.gif")></div>').appendTo('#xmlOutput');
							$('#venStatusTable').append(data);
							$(".refreshing").remove();
				});
			};

			$('#refresh').click(function(){
				$(this).text("Loading Status...");
				loadOadrEvents();
				$(this).text("Refresh");
			});

			loadOadrEvents();
						
			setInterval(loadOadrEvents, 10000);
		});	
	</script>-->

		</body>
</html>

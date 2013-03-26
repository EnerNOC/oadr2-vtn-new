<html>
<head>
<meta name="layout" content="bootstrap" />
</head>
<body>
	<h3>Select Event:</h3>
	<select>
		<g:each in="${eventList}" var="event">
		
			<option value="${event}">
				<g:link controller="VenStatus" action="venStatuses" params="[eventID: event]" class="btn btn-inverse">
					${event}
				</g:link>
			</option>
		</g:each>
	</select>
	<h3>VEN Status</h3>
	<g:render template="venStatusTableTemplate"
		model="[venStatusList: venStatusList]" />
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

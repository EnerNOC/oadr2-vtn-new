<!doctype html>
<html>
<head>
<meta name="layout" content="bootstrap" />
<title>Groovy OADR</title>
<style>
</style>

</head>

<body>
	<div class="indent">
		<section class="span12">

			<h1>OpenAdr 2.0 Groovy Style</h1>


			<div class="row-fluid">
				<div class="span6">
					<h3>Programs</h3>
					<%!import com.enernoc.oadr2.vtn.Program %>
					<% def programList = Program.list()%>
					<g:render template="/program/programTableTemplate"
						model="[programList:programList]" />
				</div>
				<div class="span6">
					<h3>VENs</h3>
					<%!import com.enernoc.oadr2.vtn.Ven %>
					<% def venList = Ven.list()%>
					<g:render template="/ven/venTableTemplate"
						model="[venList:venList]" />
				</div>
			</div>
			<div class="row">
				<div class="span12">
					<h3>Events</h3>
					<%!import com.enernoc.oadr2.vtn.Event %>
					<% def eventList = Event.list()%>
					<g:render template="/event/eventTableTemplate"
						model="[eventList:eventList]" />
				</div>
			</div>
		</section>
	</div>
</body>
</html>

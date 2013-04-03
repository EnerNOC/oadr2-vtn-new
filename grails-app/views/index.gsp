<!doctype html>
<html>
<head>
<meta name="layout" content="bootstrap" />
<title>Groovy OpenADR 2.0 VTN</title>
</head>
<body>
	<div class="indent">
		<section class="span12">
			<h1>
			 <img src="${resource(dir:'images', file:'enernoc_logo.png' )}" alt="EnerNOC" style="height:60px;width:60px"/>
			 OpenADR 2.0 Console
			</h1>
      <div class="row">
        <div class="span12">
          <h3><g:link controller="Event" action="events">Events</g:link></h3>
          <g:render template="/event/eventTableTemplate" model="[eventList:eventList]" />
        </div>
      </div>
			<div class="row-fluid">
				<div class="span6">
					<h3><g:link controller="Program" action="programs">Programs</g:link></h3>
					<g:render template="/program/programTableTemplate" model="[programList:programList]" />
				</div>
        <div class="span6">
          <h3><g:link controller="Ven" action="vens">VENs</g:link></h3>
          <g:render template="/ven/venTableTemplate" model="[venList:venList]" />
        </div>
      </div>
		</section>
	</div>
</body>
</html>

<!doctype html>
<html>
<head>
<meta name="layout" content="bootstrap" />
<title>EnerNOC OpenADR 2.0 Test Console</title>

<link rel='stylesheet' href='//cdnjs.cloudflare.com/ajax/libs/font-awesome/3.0.2/css/font-awesome.min.css' />
<style type='text/css'>
  textarea { 
    width: 100%; 
    height:20em; 
    font-family: monospace; 
    font-size: 0.9em;
    overflow: scroll;
  }
  a.help {
    color:#ccc
  }
  a.help:hover { text-decoration:none }
  .sidebar-nav select { max-width: 100% } 
</style>
</head>

<body>
	<div class="indent">
		<div class="row-fluid">
			<aside id="application-status" class="span3">
				<div class="well sidebar-nav">
				  <select id='serviceSelect'>
				    <option disabled='1' selected='1'>Select a service...</option>
				    <g:each var='svc' in='${services}'>
				    <option>${svc}</option>
				    </g:each>
				  </select>
					<ul class='nav nav-list' id='templates'>
            <li class='nav-header'>Request Templates</li>
            <li class='muted small'>(choose a service first)</li>
					</ul>
					<ul class='nav nav-list' id='programs'>
            <li class='nav-header'>Programs</li>
            <li class='muted small'>(choose a template first)</li>
					</ul>
					<ul class='nav nav-list' id='vens'>
						<li class='nav-header'>Select VEN</li>
						<li class='muted small'>(choose a program first)</li>
					</ul>
          <ul class='nav nav-list'>
            <li class='nav-header'>Select an Event</li>
            <li class='muted small'>(choose a program first)</li>
          </ul>
				</div>
			</aside>

			<section id="main" class="span9">

				<div class="row-fluid">
				  <div class='span12'>
	          <div class='pull-right'>
  	          <a href='#help' class='help' id='helpLink'><i class='icon-question-sign icon-large'></i></a>
	          </div>
						<h1>OpenADR Test Console</h1>
						<p>Use this console to simulate OpenADR requests from a VEN.</p>
					</div>
				</div>

				<div class="row-fluid">
					<div class="span10">
						<form id='requestForm' method="post" action=''>						  
              <div class='pull-right'>
                <input type='submit' class='btn btn-primary' value='Send!' />
              </div>
              <h2>Request</h2>
						  <textarea id='requestTxt' name='requestTxt'>(select a request template on the left)</textarea>
						  <input type='hidden' id='serviceName' name='serviceName' />						  
						</form>
            <textarea id='templateTxt' class='hidden'></textarea>
					</div>
				</div>

        <div class="row-fluid">
					<div class="span10">
						<h2>Response</h2>
						<textarea id='responseTxt' readonly='1'>(send a request to see the response here)</textarea>
					</div>

				</div>
			</section>
		</div>
	</div>
	
	<div class="modal hide fade" id='aboutDlg'>
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
      <h3>How to use the Test Console</h3>
    </div>
    <div class="modal-body">
      <p>You can send simulate OpenADR VEN interactions by submitting XML payloads as if they
        were coming from a VEN.  To configure XML templates location, start by setting the value
        <code>oadrTest.templatesFolder</code> in <code>grails-app/Config.groovy</code>. The 
        current value is: <code>${grailsApplication.config.oadrTest.templatesFolder}</code>.</p>
      <p>There are a number of tokens that can be replaced in templates, such as 
        <code>{ venID }</code>, <code>{ marketContext }</code>, etc. </p>
    </div>
  </div>
	
  <script type='text/html' id='templatesNav'>
   <li class='nav-header'>Request Templates</li>
   {{#templates}}
   <li><a href='template/{{ svc }}/{{ . }}'>{{ . }}</a></li>
   {{/templates}}   
  </script>
	<script type='text/html' id='programsNav'>
	 <li class='nav-header'>Programs</li>
	 {{#programs}}
	 <li><a href='#program/{{ name }}' data-id='{{ id }}' data-uri='{{ marketContext }}'>{{ name }}</a></li>
	 {{/programs}} 	 
	</script>
  <script type='text/html' id='vensNav'>
   <li class='nav-header'>VENs</li>
   {{#vens}}
   <li><a href='#ven/{{ id }}' data-id='{{ id }}'>{{ name }}</a></li>
   {{/vens}}   
  </script>
  <script type='text/html' id='eventsNav'>
   <li class='nav-header'>Events</li>
   {{#vens}}
   <li><a href='#event/{{ id }}'>{{ name }}</a></li>
   {{/vens}}   
  </script>
	
  <r:require modules="application" />
  <script src='//cdnjs.cloudflare.com/ajax/libs/ICanHaz.js/0.10/ICanHaz.min.js' type='application/javascript'></script>
  <script type='application/javascript' src='${createLink(uri:"/js/oadrTest.js")}'></script>
  <script type='application/javascript'>
  $(document).ready(function() {
    window.oadrTest.init()
  })
  </script>
</body>
</html>

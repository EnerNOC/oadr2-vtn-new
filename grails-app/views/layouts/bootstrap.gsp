<%@ page
	import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes"%>
<!doctype html>
<html lang="en">
<head>
<link rel="shortcut icon" type="image/png"
	href="${resource(dir:'images', file: 'enoc-node-bullet.png')}" />
<meta charset="utf-8" />
<title><g:layoutTitle default="${meta(name: 'app.name')}" /></title>
<meta name="description" content="EnerNOC Open Source OpenADR 2.0 VTN" />
<meta name="author" content="EnerNOC Open" />

<meta name="viewport" content="initial-scale = 1.0">

<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->

<r:require modules="bootstrap, fontAwesome, application" />

<link rel="apple-touch-icon"
	href="${resource(dir: 'images', file: 'apple-touch-icon.png')}" />
<link rel="apple-touch-icon" sizes="114x114"
	href="${resource(dir: 'images', file: 'apple-touch-icon-114x114.png')}" />

<r:layoutResources />
<g:layoutHead />
</head>

<body>
	<nav class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">

				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse">
					<span class="icon-bar"></span> 
					<span class="icon-bar"></span> 
					<span class="icon-bar"></span>
				</a> 
				<a class="brand" href="${createLink(uri: '/')}">EnerNOC OpenADR 2</a>

				<div class="nav-collapse">
					<ul class="nav">
						<li
							class="${request.forwardURI == createLink(uri: '/') ? 'active' : ''}">
							<a href="${createLink(uri: '/')}">Home</a>
						</li>
						<li
							class="${request.forwardURI == createLink(controller: 'Program') ? 'active' : ''}">
							<a href="${createLink(controller: 'Program')}">Programs</a>
						</li>
						<li
							class="${request.forwardURI == createLink(controller: 'Ven') ? 'active' : ''}">
							<a href="${createLink(controller: 'Ven')}">VENs</a>
						</li>
						<li
							class="${request.forwardURI == createLink(controller: 'Event') ? 'active' : ''}">
							<a href="${createLink(controller: 'Event')}">Events</a>
						</li>
            <li
              class="${request.forwardURI == createLink(controller: 'OADRTest') ? 'active' : ''}">
              <a href="${createLink(controller: 'OADRTest')}">Test</a>
            </li>
            <li
              class="${request.forwardURI == createLink(controller: 'Debug') ? 'active' : ''}">
              <a href="${createLink(controller: 'Debug')}">Debug</a>
            </li>
					</ul>
				</div>
				<div class="nav-collapse collapse pull-right">
				  <ul class="nav">
            <li><a class='busy-icon' style='display:none'>
              <i class='icon-spin icon-spinner icon-large'></i></a>
            </li>
				</div>
			</div>
		</div>
	</nav>

	<div class="container nav-body">

		<g:layoutBody />

		<hr />
		<footer class='muted small'>
			<p>&copy; <a href='http://open.enernoc.com'>open.enernoc.com</a></p>
		</footer>
	</div>

	<r:layoutResources />
</body>
</html>
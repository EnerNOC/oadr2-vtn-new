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

<r:require modules="bootstrap, application" />

<link rel="apple-touch-icon"
	href="${resource(dir: 'images', file: 'apple-touch-icon.png')}" />
<link rel="apple-touch-icon" sizes="114x114"
	href="${resource(dir: 'images', file: 'apple-touch-icon-114x114.png')}" />

<g:layoutHead />
<r:layoutResources />
</head>

<body>
	<nav class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">

				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="${createLink(uri: '/')}">EnerNOC OpenADR
					2</a>

				<div class="nav-collapse">
                    <ul class="nav">
                        <li> <a href="${createLink(uri: '/')}">Home</a></li>
                        <li><g:link controller="Program">Program</g:link></li>
                        <li><g:link controller="Ven">VEN</g:link></li>
                        <li><g:link controller="Event">Event</g:link></li>
                        <li><a href="${createLink(uri:'/debug') }">Debug</a></li>
                    </ul>
				</div>
			</div>
		</div>
	</nav>

	<div class="container nav-body">

		<g:layoutBody />

		<hr />
		<footer>
			<p>&copy; EnerNOC, Inc</p>
		</footer>
	</div>

	<r:layoutResources />
</body>
</html>
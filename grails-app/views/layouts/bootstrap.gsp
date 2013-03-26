<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes" %>
<!doctype html>
<html lang="en">
	<head>
	    <link rel="shortcut icon" type="image/png" href="${resource(dir:'images', file: 'enoc-node-bullet.png')}">
		<meta charset="utf-8">
		<title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
		<meta name="description" content="">
		<meta name="author" content="">

		<meta name="viewport" content="initial-scale = 1.0">

		<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
		<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->

		<r:require modules="bootstrap"/>

		<!-- Le fav and touch icons -->

		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-114x114.png')}">

		<g:layoutHead/>
		<r:layoutResources/>
		<style>
		.indent {
		margin-top:70px;
		}
		</style>
	</head>

	<body>

		<nav class="navbar navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					
					<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</a>
					
					<a class="brand" href="${createLink(uri: '/')}">OpenADR 2</a>

					<div class="nav-collapse">
						<ul class="nav">							
							 <li<%= request.forwardURI == "${createLink(uri: '/')}" ? ' class="active"' : '' %>><a href="${createLink(uri: '/')}">Home</a></li>
							<li<%= request.forwardURI == "${createLink(uri: '/program/programs')}" ? ' class="active"' : '' %>><g:link controller="Program">Program</g:link></li>
							<li<%= request.forwardURI == "${createLink(uri: '/ven/vens')}" ? ' class="active"' : '' %>><g:link controller="Ven">VEN</g:link></li>
							<li<%= request.forwardURI == "${createLink(uri: '/event/events')}" ? ' class="active"' : '' %>><g:link controller="Event">Event</g:link></li>

						</ul>
					</div>
				</div>
			</div>
		</nav>

		<div class="container">
			<div class="indent">
			<g:layoutBody/>
			</div>
			<hr>

			<footer>
				<p>&copy; EnerNOC, Inc</p>
			</footer>
		</div>

		<r:layoutResources/>

	</body>
</html>
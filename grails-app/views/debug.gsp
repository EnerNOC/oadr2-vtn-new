<!doctype html>
<html>
<head>
<meta name="layout" content="bootstrap" />
<title>EnerNOC OpenADR2 :: About</title>
<style>
</style>

</head>

<body>
	<div class="indent">
		<div class="row-fluid">
			<aside id="application-status" class="span3">
				<div class="well sidebar-nav">
					<h5>Application Status</h5>
					<ul>
						<li>App version: <g:meta name="app.version" /></li>
						<li>Grails version: <g:meta name="app.grails.version" /></li>
						<li>Groovy version: ${GroovySystem.getVersion()}</li>
						<li>JVM version: ${System.getProperty('java.version')}</li>
						<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
						<li>Domains: ${grailsApplication.domainClasses.size()}</li>
						<li>Services: ${grailsApplication.serviceClasses.size()}</li>
						<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
					</ul>
					<h5 class="hello">Installed Plugins</h5>
					<ul>
						<g:each var="plugin"
							in="${applicationContext.getBean('pluginManager').allPlugins}">
							<li>
								${plugin.name} - ${plugin.version}
							</li>
						</g:each>
					</ul>
				</div>
			</aside>

			<section id="main" class="span9">

				<div class="hero-unit">
					<h1>EnerNOC OpenADR 2.0 VTN</h1>
					</br>
					<p>This is an open source implementation of the 
					  <a href='http://openadr.org'>OpenADR</a> 2.0 VTN (server.)  This 
					  app is the second revision built on the Grails framework.
						The first version was built on the Play Framework located <a
							href="https://github.com/EnerNOC/oadr2-vtn.git">here.</a>
					</p>
				</div>

				<div class="row-fluid">

					<div class="span4">
						<h2>Controllers</h2>
						<ul class="nav nav-list">
							<g:each var="c"
								in="${grailsApplication.controllerClasses.sort { it.fullName } }">
								<li><g:link controller="${c.logicalPropertyName}">
										${c.naturalName}
									</g:link></li>
							</g:each>
						</ul>
					</div>

					<div class="span4">
						<h2>Install It</h2>
						<p>To install this project you'll need to:</p>
						<p>Download and install <a href='http://www.grails.org/download'>Grails v2.2.1</a>
						On Mac, we recommend using <a href='http://mxcl.github.com/homebrew/'>Homebrew</a>, 
						then <code>brew install grails</code></p>
					</div>

					<div class="span4">
						<h2>Fork the code</h2>
						<p>
							You can download, fork &amp; raise issues on this project on <a
								href="http://github.enernoc.net/tnichols/oadr2-vtn-groovy">GitHub</a>.
						</p>
						<p>To start:</p>
						<pre>git clone git://github.enernoc.net/tnichols/oadr2-vtn-groovy.git</pre>
					</div>

				</div>

			</section>
		</div>
	</div>
</body>
</html>

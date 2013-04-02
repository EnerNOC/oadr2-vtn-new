<!DOCTYPE html>
<html>
<head>
<title>EnerNOC OpenADR 2.0 VTN :: Error</title>
<meta name="layout" content="bootstrap" />
</head>
<body>
  <g:if test="${exception != null}">
    <g:renderException exception="${exception}" />
  </g:if>
  <g:else>
	<h1>
		Error: ${request.'javax.servlet.error.status_code'}
	</h1>
	<strong>
		${request.'javax.servlet.error.message'.encodeAsHTML()}
	</strong>
  </g:else>
</body>
</html>

<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=ISO-8859-1"
		 pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet">
	<title>Real Time</title>
</head>
<body class = "justify-content-center">
<h3 class = "text-center">Real Time Info</h3>
	<div class="container" >
		<p class="text-danger"> <c:out value="${session.error}"/> </p>
		<div class="card">
			<div class="card-body overflow-auto">
				<p class="card-text text-justify">bla bla bla bla</p>
				<!--p class="card-text text-justify"><c:out value="${election.descricao}"/></p-->
			</div>

		</div>
	</div>

</body>
</html>
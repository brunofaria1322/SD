<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=ISO-8859-1"
		 pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Choose Election</title>
</head>
<body class = "text-center">
	<main class="form-signin">

		<c:forEach items="${electionsList}" var="election" >
			<div class="card">
				<div class="card-body">
					<h5 class="card-title">
						<c:out value="${election.value.title}"/>
					</h5>
					<p class="card-text">
						<c:out value="${election.value.description}"/>
					</p>

					<s:form action="manageElection" method="post">
						<input type="hidden" name="electionId" value="${election.key}">
						<s:submit cssClass="btn btn-primary" value="Manage"/>
					</s:form>

				</div>
			</div>
		</c:forEach>

	</main>
</body>
</html>
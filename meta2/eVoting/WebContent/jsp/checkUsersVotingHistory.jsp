<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-wEmeIV1mKuiNpC+IOBjI7aAzPcEZeedi5yW5f2yOq55WWLwNGmvvx4Um1vskeMj0" crossorigin="anonymous">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Login</title>
</head>
<body >
	<div class="container">
		<div class = "text-center m-3">
			<s:form method="GET" action="checkUsersVotingHistory">
				<s:textfield name="username" label="Model" />
				<s:submit label="Search!" />
			</s:form>
		</div>
		<main class="card">
			<c:choose>
				<c:when test="${results == null}">
					A problem occurred during the search!
				</c:when>
				<c:when test="${results.isEmpty()}">
					This user has no past votes!
				</c:when>
				<c:otherwise>
					<c:forEach items="${results}" var="vote">
						<div class="card-body">
							<h4 class="card-title"><c:out value="${vote[0]}"/></h4>
							<h6 class="card-subtitle text-muted">Where:</h6>
							<p class="card-text"><c:out value="${vote[1]}"/></p>
							<h6 class="card-subtitle text-muted">At:</h6>
							<p class="card-text"><c:out value="${vote[2]}"/></p>
						</div>
						<br />
					</c:forEach>
				</c:otherwise>
			</c:choose>

		</main>
	</div>
</body>
</html>
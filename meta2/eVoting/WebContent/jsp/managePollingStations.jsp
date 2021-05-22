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
	<title><c:out value="Manage Polling Stations"/></title>
</head>
<body class = "justify-content-center">

	<div class="container">
		<div class="card">
			<div class="card-body">
				<h4 class="card-title">Polling Stations:</h4>
				<ul class="list-group">
					<c:forEach items="${results}" var="list" >
						<li class="list-group-item d-flex justify-content-between align-items-center">
							<c:out value="${list[1]}"/>
							<div>
								<s:form action="removePollingStation" method="post">
									<input type="hidden" name="stationId" value="${list[0]}">
									<s:submit cssClass="btn btn-danger" value="Remove"/>
								</s:form>
							</div>
						</li>
					</c:forEach>
				</ul>
			</div>

			<div class="card-body">
				<h4 class="card-title">New Polling Station:</h4>
				<s:form action="addPollingStation" method="post">
					<s:div cssClass="form-floating">
						<s:select
								list="departments"
								name="department"
								cssClass="form-select"
								required="required"/>
						<label class="floatingInput">Polling Station's department</label>
					</s:div>
					<s:submit cssClass="btn btn-primary" value="Add Polling Station"/>
				</s:form>
			</div>

		</div>
	</div>

</body>
</html>
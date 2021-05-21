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
<body class = "justify-content-center">

	<div class="container">
		<h3 class = "text-center"><c:out value="${election.title}"/></h3>
		<div class="card">
			<div class="card-body">
				<h4 class="card-title">Description:</h4>
				<p class="card-text text-justify"><c:out value="${election.description}"/></p>
			</div>
			<div class="card-body">
				<h4 class="card-title">Times:</h4>
				<h6 class="card-subtitle text-muted">Start:</h6>
				<p class="card-text"><c:out value="${election.start_date}"/></p>
				<h6 class="card-subtitle text-muted">End:</h6>
				<p class="card-text"><c:out value="${election.end_date}"/></p>
			</div>
			<div class="card-body justify-content-center list-group">
				<s:set name="estado" value="estado"/>
				<!-- Election hasn't started yet -->
				<s:if test="%{#estado==1}">
					<s:form action="manageCandidateLists" method="post">
						<input type="hidden" name="electionId" value="${electionId}">
						<s:submit cssClass="btn btn-primary list-group-item list-group-item-action" value="Manage candidate lists"/>
					</s:form>
					<s:form action="managePollingStations" method="post">
						<input type="hidden" name="electionId" value="${electionId}">
						<s:submit cssClass="btn btn-primary list-group-item list-group-item-action" value="Manage polling stations"/>
					</s:form>
					<s:form action="changeElectionPropreties" method="post">
						<input type="hidden" name="electionId" value="${electionId}">
						<s:submit cssClass="btn btn-primary list-group-item list-group-item-action" value="Change properties"/>
					</s:form>
				</s:if>
				<!-- Election is currently active -->
				<s:elseif test="%{#estado==2}">
					<s:form action="changeElectionPropreties" method="post">
						<input type="hidden" name="electionId" value="${electionId}">
						<s:submit cssClass="btn btn-primary" value="Change properties"/>
					</s:form>
				</s:elseif>
				<!-- Election has finished -->
				<s:elseif test="%{#estado==3}">
					<s:form action="checkElectionResults" method="post">
						<input type="hidden" name="electionId" value="${electionId}">
						<s:submit cssClass="btn btn-primary" value="Check Results"/>
					</s:form>
				</s:elseif>
			</div>

		</div>
	</div>

</body>
</html>
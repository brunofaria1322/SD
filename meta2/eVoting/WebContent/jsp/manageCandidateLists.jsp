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
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
	<title><c:out value="Manage Candidat Lists"/></title>
</head>
<body class = "justify-content-center">
	<p>${session.error}</p>
	<div class="container">
		<h3 class="card-title">Candidate Lists:</h3>
		<div class="card">
			<div class="card-body">
				<ul class="list-group list-group-flush">
					<c:forEach items="${results}" var="list" >

						<li class="list-group-item">
							<button class="btn d-flex justify-content-between align-items-center w-100 my-0"  data-target="#coll${list[0]}" data-toggle="collapse">
								<c:out value="${list[1]}"/>
								<div>
									<s:form action="removeCandidateList" method="post">
										<input type="hidden" name="listId" value="${list[0]}">
										<s:submit cssClass="btn btn-danger my-0" value="Remove"/>
									</s:form>
								</div>
							</button>

							<ul id="coll${list[0]}" class="card-body collapse list-group list-group-flush" >
								<c:forEach items="${candidates.get(list[0])}" var="candidate" >
									<li class="list-group-item justify-content-between align-items-center d-flex w-100 my-0">
										<c:out value="${candidate[1]}"/>
										<div>
											<s:form action="removeCandidate" method="post">
												<input type="hidden" name="candidateId" value="${candidate[0]}">
												<input type="hidden" name="candidateName" value="${candidate[1]}">
												<input type="hidden" name="listName" value="${list[1]}">
												<s:submit cssClass="btn btn-danger my-0" value="Remove"/>
											</s:form>
										</div>
									</li>
								</c:forEach>
								<div class="card-body">
									<h5 class="card-title">New Candidate:</h5>
									<s:form action="addCandidate" method="post">
										<s:div cssClass="form-floating">
											<s:textfield
													placeholder="Candidate Name"
													name="candidateName"
													cssClass="form-control"
													required="required"/>
											<label class="floatingInput">Candidate Name</label>
										</s:div>
										<s:div cssClass="form-floating">
											<s:textfield
													placeholder="Candidate's ID'"
													name="candidateId"
													cssClass="form-control"
													required="required"/>
											<label class="floatingInput">Candidate's ID</label>
										</s:div>
										<input type="hidden" name="listName" value="${list[1]}">
										<s:submit cssClass="btn btn-primary" value="Add Candidate"/>
									</s:form>
								</div>
							</ul>
						</li>


					</c:forEach>

				</ul>
			</div>

			<div class="card-body">
				<h4 class="card-title">New Candidate List:</h4>
				<s:form action="addCandidateList" method="post">
					<s:div cssClass="form-floating">
						<s:textfield
								placeholder="List Name"
								name="listName"
								cssClass="form-control"
								required="required"/>
						<label class="floatingInput">List Name</label>
					</s:div>
					<s:submit cssClass="btn btn-primary" value="Add Candidate List"/>
				</s:form>
			</div>

		</div>
	</div>

</body>
</html>
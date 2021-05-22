<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Create Election</title>
</head>
<body class = "text-center">
	<main class="form-signin">
		<s:form action="createElectionAction" method="post">
			<h1 class="h3 mb-3 fw-normal">Create Election</h1>
			<s:div cssClass="form-floating">
				<s:select
						  list="votersTypes"
						  name="votersType"
						  cssClass="form-select"
						  required="required"/>
				<label class="floatingInput">Who are the voters</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:select
						  list="departments"
						  name="department"
						  cssClass="form-select"
						  required="required"/>
				<label class="floatingInput">What's the person's department</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						placeholder="Title"
						name="title"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Title</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textarea
						placeholder="Description"
						name="description"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Description</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						type="datetime-local"
						placeholder="Starting Date and Time"
						name="starting_datetime"
						cssClass="form-control"
						required="required"/>

				<label class="floatingInput">Starting Date and Time</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						type="datetime-local"
						placeholder="Ending Date and Time"
						name="ending_datetime"
						cssClass="form-control"
						required="required"/>

				<label class="floatingInput">Ending Date and Time</label>
			</s:div>
			<s:submit cssClass="w-100 btn btn-lg btn-primary"/>
		</s:form>
	</main>
</body>
</html>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-wEmeIV1mKuiNpC+IOBjI7aAzPcEZeedi5yW5f2yOq55WWLwNGmvvx4Um1vskeMj0" crossorigin="anonymous">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Register Person</title>
</head>
<body class = "text-center">
	<main class="form-signin">
		<s:form action="registerPersonAction" method="post">
			<h1 class="h3 mb-3 fw-normal">Register Person</h1>
			<s:div cssClass="form-floating">
				<s:select
						  list="personTypes"
						  name="personType"
						  cssClass="form-select"
						  required="required"/>
				<label class="floatingInput">What's the person's statute</label>
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
						placeholder="Name"
						name="name"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Name</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						placeholder="Address"
						name="address"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Address</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						type="number"
						placeholder="Phone Number"
						name="phoneNo"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Phone Number</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						type="number"
						placeholder="CC Number"
						name="ccNo"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">CC Number</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						type="date"
						placeholder="CC Expiration Date"
						name="ccExpDate"
						cssClass="form-control"
						required="required"/>

				<label class="floatingInput">CC Expiration Date</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:textfield
						placeholder="Username"
						name="username"
						cssClass="form-control"
						required="required"/>
				<label class="floatingInput">Username</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:password
						placeholder="Password"
						name="password"
						cssClass="form-control"
						required="required"/>
				<label class="floatingPassword">Password</label>
			</s:div>

			<s:div cssClass="form-floating">
				<s:password
						placeholder="repeat Password"
						name="repeatedPassword"
						cssClass="form-control"
						required="required"/>
				<label class="floatingPassword">Repeat Password</label>
			</s:div>
			<s:submit cssClass="w-100 btn btn-lg btn-primary"/>
		</s:form>
	</main>
</body>
</html>
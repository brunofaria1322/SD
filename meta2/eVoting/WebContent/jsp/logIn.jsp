<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<body class = "text-center">


<main class="form-signin">
	<s:form action="logInAction" method="post">
		<h1 class="h3 mb-3 fw-normal">Please sign in</h1>
		<s:div cssClass="form-floating">
			<s:textfield
					placeholder="Username"
					name="username"
					cssClass="form-control"/>
			<label class="floatingInput">Username</label>
		</s:div>
		<s:div cssClass="form-floating">
			<s:password
					placeholder="Password"
					name="password"
					cssClass="form-control"/>
			<label class="floatingPassword">Password</label>
		</s:div>
		<s:submit cssClass="btn btn-lg btn-primary"/>
	</s:form>
	<a class="btn btn-primary"
			href="https://www.facebook.com/v3.2/dialog/oauth?response_type=code&client_id=2913897935402035&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FeVoting%2FlogInAction&state=secret494658">
		Login with Facebook
	</a>
</body>
</main>

</html>
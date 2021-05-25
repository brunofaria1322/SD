<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Admin</title>
</head>
<body class="text-center justify-content-center">
	<div class="list-group">
		<a href="<s:url action="registerPerson"/>" class="list-group-item list-group-item-action">Register Person</a>
		<a href="<s:url action="createElection"/>" class="list-group-item list-group-item-action">Create Election </a>
		<a href="<s:url action="chooseElection"/>" class="list-group-item list-group-item-action">Manage Election </a>
		<a href="<s:url action="checkUsersVotingHistory"/>" class="list-group-item list-group-item-action">Check User's Voting History </a>

		<a href="<s:url action="adminPage"/>" class="list-group-item list-group-item-action mt-3">Admin's Page </a>

		<a class="list-group-item list-group-item-action bg-primary text-white mt-3" href="https://www.facebook.com/v3.2/dialog/oauth?response_type=code&client_id=2913897935402035&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FeVoting%2FaddFacebookAction&state=secret694822
"> Associate Facebook </a>
	</div>
</body>
</html>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="styles/forms.css" type="text/css" rel="stylesheet">
	<title>Admin</title>
</head>
<body class="text-center">
	<a href="<s:url action="registerPerson"/>">Register Person </a>
	<a href="<s:url action="createElection"/>">Create Election </a>
	<a href="<s:url action="chooseElection"/>">Manage Election </a>
	<a href="<s:url action="checkUsersVotingHistory"/>">Check User's Voting History </a>
</body>
</html>
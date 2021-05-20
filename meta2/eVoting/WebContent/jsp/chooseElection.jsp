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

		<!-- TODO: ALTERAR!!! -->
		<c:forEach items="${electionsList}" var="auction" varStatus="status">
			<div id="auc" style="height: 50vh;">

				<h2 style="display:inline"> Auction Id: </h2>
				<c:out value="${auction.id_leilao}"/>
				<h3 style="display:inline"> <br><br>Auction Owner: </h3>
				<c:out value="${auction.username_criador}"/>
				<h3 style="display:inline"> <br><br>Auction Code: </h3>
				<c:out value="${auction.artigoId}"/><br><br>
				<s:form action="detailAuction" method="post">
					<input type="hidden" name="Id" value="${auction.id_leilao}">
					<s:submit value="Access auction details"/>
				</s:form>

			</div>
		</c:forEach>

	</main>
</body>
</html>
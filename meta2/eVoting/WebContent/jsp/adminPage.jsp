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
	<title>Real Time</title>
	<script type="text/javascript">

		var websocket = null;

		window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
			connect('ws://' + window.location.host + '/eVoting/ws');
		}

		function connect(host) { // connect to the host websocket
			if ('WebSocket' in window)
				websocket = new WebSocket(host);
			else if ('MozWebSocket' in window)
				websocket = new MozWebSocket(host);
			else {
				writeToHistory('Get a real browser which supports WebSocket.');
				return;
			}
			websocket.onmessage = onMessage;
			websocket.onerror   = onError;
		}


		function onMessage(message) { // print the received message
			writeToHistory(message.data);
		}

		function onError(event) {
			writeToHistory('WebSocket error.');
			document.getElementById('chat').onkeydown = null;
		}


		function writeToHistory(text) {
			var json = JSON.parse(text);
			var caixa = document.getElementById('ws');
			var display = "Active Polling Stations:<br />";
			for (let key in json["stations"]){
				display += key +": (" + json["stations"][key]["left"] + ", " + json["stations"][key]["right"]+")<br />";
			}
			display += "<br /><br /> Active Polls:<br />";
			for (let key in json["votes"]){
				display += "---"+key+"---<br />";
				for(let dep in json["votes"][key]){
					display += dep +": "+ json["votes"][key][dep]+" votes<br />";
				}
			}
			caixa.innerHTML=display;
		}

	</script>
</head>
<body class = "justify-content-center">
<h3 class = "text-center">Real Time Info</h3>
	<div class="container" >
		<p class="text-danger"> <c:out value="${session.error}"/> </p>
		<div class="card">
			<div class="card-body overflow-auto" id="ws">
			</div>

		</div>
	</div>

</body>
</html>
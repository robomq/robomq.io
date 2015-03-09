# Introduction

> Before reading this chapter, we assume that you already know STOMP protocol. If not, please go through at least the first two sections of STOMP chapter in Use Guide and refer to the [full documentation of STOMP](http://stomp.github.io/) when necessary.  

[robomq.io](http://www.robomq.io) supports WebSTOMP, which is a simple bridge exposing the STOMP protocol over emulated HTML5 WebSockets. The main intention of WebSTOMP is to make it possible to use [robomq.io](http://www.robomq.io) from web browsers. Therefore, [robomq.io](http://www.robomq.io) WebSTOMP adapter is rather simple. It just takes the STOMP protocol, as provided by [robomq.io](http://www.robomq.io) STOMP adapter and exposes it using a SockJS server.  
SockJS is a WebSockets poly-fill that provides a WebSocket-like JavaScript object in any browser. It will therefore work in older browsers that don't have native WebSocket support, as well as in new browsers that are behind WebSocket-unfriendly proxies.  

# WebSTOMP use cases

###

We will provide example of WebSTOMP in JavaScript that is embedded in HTML.  

In the example, WebSTOMP producer will first connect to [robomq.io](http://www.robomq.io) with inputted information from Web page, then publish inputted text to the inputted message destination. WebSTOMP consumer will also first connect then subscribe the inputted message destination and print the message topic and payload as it receives messages.  

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK [repository](https://github.com/robomq/robomq.io) on GitHub.  

Follow the *Message destinations* section in STOMP chapter and you will be able to switch among all the scenarios by changing only the message destination. This could also be a tutorial tool for STOMP novices to familiarize themselves with STOMP and WebSTOMP.  

### Prerequisite
The JavaScript library we use for this example is the same as we used for STOMP Node.js example. It can be found at <https://github.com/jmesnil/stomp-websocket>.  
It supports STOMP version 1.0 and 1.1.  

Download *stomp.js* from <https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js> and place it in your project directory.  

Finally, source *stomp.js* and <http://cdn.sockjs.org/sockjs-0.3.min.js> in your Web page.

The full documentation of this library is at <http://jmesnil.net/stomp-websocket/doc/>.

### Producer
Step 1 is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
In constructor of SockJS, protocol can be "http" or "https", they use different ports.  
In `connect()` function, third parameter is callback function on connect, fourth is callback function on error.  

	var webSock = new SockJS(protocol + "://" + host + ":" + port + "/stomp");
	client = Stomp.over(webSock);
	client.connect(username, password, onConnect, onError, vhost);

Step 2 is to send inputted messages to the inputted destination.   
The second parameter is message headers. It's not used in this example.  

	client.send(destination, {}, message);

### Consumer
Step 1 is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Step 2 is to subscribe the inputted destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will call the callback function to print the topic and payload of the message.  

	subscription = client.subscribe(destination, onMessage, {ack: "auto"});

	function onMessage(message) {
		//on page, print "Destiantion: " + message.headers["destination"] + ", Message: " + message.body
	}

When page unloads, consumer will unsubscribe the destination by its token and disconnect with the [robomq.io](http://www.robomq.io) broker.

	subscription.unsubscribe();
	client.disconnect();

### Putting it together

**producer.html**

	<!DOCTYPE html>
	<html>
		<head>
			<title>producer</title>
		</head>
	
		<body onunload="close()">
			<h3>Step 1:</h3>
			<form name="connForm" action="JavaScript:connect()">
				protocol:<br><input type="radio" name="protocol" value="http" checked>http
						&nbsp;<input type="radio" name="protocol" value="https">https<br>
				host:<br><input type="text" name="host" value="trial.robomq.io"><br>
				port:<br><input type="text" name="port" value="15674"><br>
				vhost:<br><input type="text" name="vhost" value=""><br>
				username:<br><input type="text" name="username" value=""><br>
				password:<br><input type="text" name="password" value=""><br><br>
				<input type="submit" value="connect">
			</form>
	
			<br><h3>Step 2:</h3>
			<form name="pubForm" action="JavaScript:publish()">
				destination:<br><input type="text" name="destination" value="/queue/test"><br>
				message:<br><input type="text" name="message" value="Hello World!"><br><br>
				<input type="submit" value="publish">
			</form>
		</body>
	
		<script src="http://cdn.sockjs.org/sockjs-0.3.min.js"></script>
		<script src="./stomp.js"></script>
		<!--download stomp.js from https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js-->
		<!--change src to file's actual path; don't directly source this link-->
	
		<script>
			var client = null;
	
			function connect() {
				if (client != null && client.connected) {
					client.disconnect();
				}
				var connInfo = document.forms["connForm"];
				var webSock = new SockJS(connInfo["protocol"].value + "://" + connInfo["host"].value + ":" + connInfo["port"].value + "/stomp");
		    	client = Stomp.over(webSock);
				client.connect(connInfo["username"].value, connInfo["password"].value, onConnect, onError, connInfo["vhost"].value);
				client.heartbeat.outgoing = 0;
				client.heartbeat.incoming = 0;
			}
	
			function onConnect() {	
				alert("Connected to broker!");
			}
	
			function publish() {
				if (client == null || !client.connected) {
					alert("Please connect first!");
					return;
				}
				var pubInfo = document.forms["pubForm"];
				client.send(pubInfo["destination"].value, {}, pubInfo["message"].value);
			}
	
			function onError(error) {
				alert(error);
			}
	
			function close() {
				client.disconnect();
			}
		</script>
	</html>

**consumer.html**

	<!DOCTYPE html>
	<html>
		<head>
			<title>consumer</title>
		</head>
	
		<body onunload="close()">
			<h3>Step 1:</h3>
			<form name="connForm" action="JavaScript:connect()">
				protocol:<br><input type="radio" name="protocol" value="http" checked>http
						&nbsp;<input type="radio" name="protocol" value="https">https<br>
				host:<br><input type="text" name="host" value="trial.robomq.io"><br>
				port:<br><input type="text" name="port" value="15674"><br>
				vhost:<br><input type="text" name="vhost" value=""><br>
				username:<br><input type="text" name="username" value=""><br>
				password:<br><input type="text" name="password" value=""><br><br>
				<input type="submit" value="connect">
			</form>
	
			<br><h3>Step 2:</h3>
			<form name="subForm" action="JavaScript:subscribe()">
				destination:<br><input type="text" name="destination" value="/queue/test"><br><br>
				<input type="submit" value="subscribe">
			</form>
			<br><h3 id="msgArea">Received:</h3><br>
		</body>
	
		<script src="http://cdn.sockjs.org/sockjs-0.3.min.js"></script>
		<script src="./stomp.js"></script>
		<!--download stomp.js from https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js-->
		<!--change src to file's actual path; don't directly source this link-->
	
		<script>
			var client = null;
			var subscription = null;
	
			function connect() {
				if (client != null && client.connected) {
					client.disconnect();
				}
				var connInfo = document.forms["connForm"];
				var webSock = new SockJS(connInfo["protocol"].value + "://" + connInfo["host"].value + ":" + connInfo["port"].value + "/stomp");
		    	client = Stomp.over(webSock);
				client.connect(connInfo["username"].value, connInfo["password"].value, onConnect, onError, connInfo["vhost"].value);
				client.heartbeat.outgoing = 0;
				client.heartbeat.incoming = 0;
			}
	
			function onConnect() {	
				alert("Connected to broker!");
			}
	
			function subscribe() {
				if (client == null || !client.connected) {
					alert("Please connect first!");
					return;
				}
				if (subscription != null) {
					subscription.unsubscribe();
				}
				var subInfo = document.forms["subForm"];
				subscription = client.subscribe(subInfo["destination"].value, onMessage, {ack: "auto"});
				alert("Subscription ID: " + subscription["id"]);
			}
	
			function onMessage(message) {
				var newMsg = document.createElement("div");
				newMsg.appendChild(document.createTextNode("Destiantion: " + message.headers["destination"] + ", Message: " + message.body));
				newMsg.appendChild(document.createElement("br"));
				document.body.insertBefore(newMsg, document.getElementById("msgArea").nextSibling);
			}
	
			function onError(error) {
				alert(error);
				subscription = null;
			}
	
			function close() {
				subscription.unsubscribe();
				client.disconnect();
			}
		</script>
	</html>
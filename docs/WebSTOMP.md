# Introduction

> Before reading this chapter, we assume that you already know STOMP protocol. If not, please go through at least the first two sections of STOMP chapter in User Guide and refer to the <a href="https://stomp.github.io/" target="_blank">Full documentation of STOMP</a> when necessary.  

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> supports WebSTOMP, which is a simple bridge exposing the STOMP protocol 1.0 and 1.1 over emulated HTML5 WebSockets. Its port is **15674**, SSL port is **15673**.    

The main intention of WebSTOMP is to make it possible to use <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> from web browsers. Therefore, <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> WebSTOMP adapter is rather simple. It just takes the STOMP protocol, as provided by <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> STOMP adapter and exposes it using a SockJS server.  
SockJS is a WebSockets poly-fill that provides a WebSocket-like JavaScript object in any browser. It will therefore work in older browsers that don't have native WebSocket support, as well as in new browsers that are behind WebSocket-unfriendly proxies.  

# WebSTOMP use case

We will provide example of WebSTOMP in JavaScript that is embedded in HTML.  

In the example, WebSTOMP producer will first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> with inputted information from Web page, then publish inputted text to the inputted message destination. WebSTOMP consumer will also first connect then subscribe the inputted message destination and print the message topic and payload as it receives messages.  

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK <a href="https://github.com/robomq/robomq.io/tree/master/sdk/WebSTOMP" target="_blank">repository</a> on GitHub.  

Follow the *Message destinations* section in STOMP chapter and you will be able to switch among all the scenarios by changing only the message destination. This could also be a tutorial tool for STOMP novices to familiarize themselves with STOMP and WebSTOMP.  

### Prerequisite
The JavaScript library we use for this example is the same as we use for STOMP Node.js example. It can be found at <a href="https://github.com/jmesnil/stomp-websocket" target="_blank">https://github.com/jmesnil/stomp-websocket</a>  
It supports STOMP version 1.0 and 1.1.  

Download *stomp.js* from <a href="https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js" target="_blank">https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js</a> and place it in your project directory.  

Finally, source *stomp.js* and <a href="https://cdn.sockjs.org/sockjs-0.3.min.js" target="_blank">https://cdn.sockjs.org/sockjs-0.3.min.js</a> in your Web page.

The full documentation of this library is at <a href="https://jmesnil.net/stomp-websocket/doc/" target="_blank">https://jmesnil.net/stomp-websocket/doc/</a>.

### Producer
Step 1 is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
In constructor of SockJS, protocol can be "http" or "https", they use different ports.  
In `connect()` function, third parameter is callback function on connect, fourth is callback function on error.  
Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker won't send heartbeat to client.  

```javascript
var webSock = new SockJS(protocol + "://" + host + ":" + port + "/stomp");
client = Stomp.over(webSock);
client.heartbeat.outgoing = 60000;
client.heartbeat.incoming = 0;
client.connect(username, password, onConnect, onError, vhost);
```

Step 2 is to send inputted messages to the inputted destination.   
The second parameter is message headers. It's not used in this example.  

```javascript
client.send(destination, {}, message);
```

### Consumer
Step 1 is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Step 2 is to subscribe the inputted destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will call the callback function to print the topic and payload of the message.  

```javascript
subscription = client.subscribe(destination, onMessage, {ack: "auto"});

function onMessage(message) {
	//on page, print "Destiantion: " + message.headers["destination"] + ", Message: " + message.body
}
```

When page unloads, consumer will unsubscribe the destination by its token and disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.

```javascript
subscription.unsubscribe();
client.disconnect();
```

### Putting it together

**producer.html**

```html
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
	<script src="stomp.js"></script>
	<!--download stomp.js from https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js-->
	<!--change src to file's actual path; don't directly source this GitHub link-->
	
	<script>
		var client = null;
	
		function connect() {
			if (client != null && client.connected) {
				client.disconnect();
			}
			var connInfo = document.forms["connForm"];
			var webSock = new SockJS(connInfo["protocol"].value + "://" + connInfo["host"].value + ":" + connInfo["port"].value + "/stomp");
	    	client = Stomp.over(webSock);
			client.heartbeat.outgoing = 60000;
			client.heartbeat.incoming = 0;
			client.connect(connInfo["username"].value, connInfo["password"].value, onConnect, onError, connInfo["vhost"].value);
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
```

**consumer.html**

```html
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
	<script src="stomp.js"></script>
	<!--download stomp.js from https://raw.githubusercontent.com/jmesnil/stomp-websocket/master/lib/stomp.js-->
	<!--change src to file's actual path; don't directly source this GitHub link-->
	
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
			client.heartbeat.outgoing = 60000;
			client.heartbeat.incoming = 0;
			client.connect(connInfo["username"].value, connInfo["password"].value, onConnect, onError, connInfo["vhost"].value);
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
```



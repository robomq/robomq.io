/**
 * File: procuder.js
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */
 
var Stomp = require("stompjs");

var server = "hostname";
var port = 61613; //It takes either string or int argument
var login = "username";
var passcode = "password";
var vhost = "yourvhost";
var destination = "/queue/test";	//There're more options other than /queue/...

var client = Stomp.overTCP(server, port);
client.connect(login, passcode
	, function() {
		process.stdout.write("Quantity of test messages: ");
		process.stdin.on("data", function (msgNum) {
			for(var i = 1; i <= msgNum; i++){	
				var message = "test msg " + i;
				client.send(destination, {"content-type": "text/plain"}, message);
			}
			client.disconnect(function() {
				process.exit(0);
			});
		});		
	}
	//callback function of connection failure
	, function(ex) {
		console.log(ex);
		process.exit(-1);
	}
	, vhost);
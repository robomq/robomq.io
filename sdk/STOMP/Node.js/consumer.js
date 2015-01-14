/**
 * File: consumer.js
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
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

var client = Stomp.overTCP(server, port, "v11.stomp");
client.connect(login, passcode
	, function() {
		try {
			//the callback for subscribe() function is actually the callback on message 
	  		client.subscribe(destination, function(message) {
				try {
					console.log(message.body);
					message.ack();
				} catch(ex) {
					console.log("Error: Can't handle message received, NACKing");
					message.nack();
				}
			},
			{ack: "client"}); //if ack:"auto", no need to ack in code
		} catch(ex) {
			console.log("Error: Can't subscribe queue");
			process.exit();
		}
	}
	//callback function of connection failure
	, function() {
		console.log("Error: Can't initialize connection");
		process.exit();
	}
	, vhost);

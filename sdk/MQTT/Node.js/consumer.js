/**
 * File: consumer.js
 * Description: This is the MQTT consumer handles incoming messages
 *     published by producers from a particular topic.
 *     Consumer prints the topic and payload as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

var mqtt = require("mqtt");

var server = "hostname";
var port = "1883";
var vhost = "yourvhost";
var username = "username";
var password = "password";
var topic = "test";

var client = mqtt.createClient(port, server, {username: vhost + ":" + username, password: password, keepalive: 60, clean: true, will: null});
client.on("connect", function() {	//library handles connection errors
	try {
		client.subscribe(topic, {qos: 1, dup: false})	//chainable API
		.on("message", function(topic, payload, packet) {	//event handling
			console.log("Topic: " + topic + ", Message: " + payload);
		});
	} catch(ex) {
		console.log("Error: Failed to subscribe and receive message");
		process.exit(-1);
	}
});

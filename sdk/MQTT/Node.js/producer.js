/**
 * File: procuder.js
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */
 
var mqtt = require("mqtt");

var server = "localhost";
var port = "1883";
var vhost = "/";
var username = "guest";
var password = "guest";
var topic = "test";

var client = mqtt.createClient(port, server, {username: vhost + ":" + username, password: password, keepalive: 10, clean: true, will: null});
client.on("connect", function() {	//library handles connection errors
	//ask user to input the number of test messages
	process.stdout.write("Quantity of test messages: ");
	process.stdin.on("data", function (msgNum) {
		//send certain number of messages
		try {
			for(var i = 1; i <= msgNum; i++){	
				var message = "test msg " + i;
				client.publish(topic, message, {qos: 1, retain: false});
			}
		} catch(ex) {
			console.log("Error: Failed to send message");
			process.exit(-1);
		}
		//shut down producer after messages sent
		setTimeout(function() {
			client.end();	//includes disconnect()
			process.exit(0);
		}, msgNum * 3);
	});
});

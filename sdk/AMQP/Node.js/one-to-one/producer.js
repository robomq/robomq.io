/**
* File: producer.js
* Description: This is the AMQP producer publishes outgoing AMQP
*     communication to  clients consuming messages from a broker server.
*     Messages can be sent over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified routing key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

var amqp = require("amqplib");

var server = "hostname";
var port = 5672;
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var routingKey = "testQ";

producer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost);
	producer.then(function(conn) {
	return conn.createConfirmChannel().then(function(ch) {
		//assigning blank string to exchange is to use the default exchange, where queue name is the routing key
		ch.publish("", routingKey, content = new Buffer("Hello World!"), options = {contentType: "text/plain", deliveryMode: 1}, function(err, ok) {
			if (err != null) {
				console.error("Error: failed to send message\n" + err);
			}
			conn.close();
		});
	});
}).then(null, function(err) {
	console.error(err);
});
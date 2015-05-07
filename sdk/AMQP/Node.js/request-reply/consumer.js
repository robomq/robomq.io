/**
* File: consumer.js
* Description: This is the AMQP consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     Messages can be received over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified binding key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

var amqp = require("amqplib");
var domain = require("domain");

var server = "hostname";
var port = "5672";
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var exchangeName = "testEx";
var requestQueue = "requestQ";
var requestKey = "request";

//use domain module to handle reconnecting
var consumer = null;
var dom = domain.create();
dom.on("error", relisten);
dom.run(listen);

function listen() {
	consumer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost + "?heartbeat=60");
	consumer.then(function(conn) {
		return conn.createChannel().then(function(ch) {
			ch.assertExchange(exchangeName, "direct", {durable: false, autoDelete: true});
			ch.assertQueue(requestQueue, {durable: false, autoDelete: true, exclusive: true});
			ch.bindQueue(requestQueue, exchangeName, requestKey);
			ch.consume(requestQueue, function(message) {
				//callback funtion on receiving messages, reply to the reply_to header
				console.log(message.content.toString());
				ch.publish(exchangeName, message.properties.replyTo, new Buffer("Reply to " + message.content.toString()), options = {contentType: "text/plain", deliveryMode: 1}, function(err, ok) {
					if (err != null) {
						ch.nack(message);
					}
					else {
						ch.ack(message);
					}
				});
			}, {noAck: false});
		});
	}).then(null, function(err) {
		console.error("Exception handled, reconnecting...\nDetail:\n" + err);
		setTimeout(listen, 5000);
	});
}

function relisten() {
	consumer.then(function(conn) {
		conn.close();
	});	
	setTimeout(listen, 5000);
}
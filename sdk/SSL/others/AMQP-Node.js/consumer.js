/**
* File: consumer.js
* Description: This is the AMQP SSL consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     This example applies routing-key pattern out of 5 patterns.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

var amqp = require("amqplib");
var domain = require("domain");

var server = "hostname";
var port = "5671";
var vhost = "yourvhost";
var username = "username";
var password = "password";
var exchangeName = "testEx";
var queueName = "testQ1";
var routingKey = "test";

//use domain module to handle reconnecting
var consumer = null;
var dom = domain.create();
dom.on("error", relisten);
dom.run(listen);

function listen() {
	consumer = amqp.connect("amqps://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost + "?heartbeat=60");
	consumer.then(function(conn) {
		return conn.createChannel().then(function(ch) {
			ch.assertExchange(exchangeName, "direct", {durable: false, autoDelete: true});
			ch.assertQueue(queueName, {durable: false, autoDelete: true, exclusive: true});
			ch.bindQueue(queueName, exchangeName, routingKey);
			ch.consume(queueName, function(message) {
				//callback funtion on receiving messages
				console.log(message.content.toString());
			}, {noAck: true});
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

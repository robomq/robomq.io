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
var port = 5672;
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var queueName = "testQ";

//use domain module to handle reconnecting
var consumer = null;
var dom = domain.create();
dom.on("error", relisten);
dom.run(listen);

function listen() {
	consumer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost);
	consumer.then(function(conn) {
		return conn.createChannel().then(function(ch) {
			//one-to-one messaging uses the default exchange, where queue name is the routing key
			ch.assertQueue(queueName, {durable: false, autoDelete: true, exclusive: false});
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

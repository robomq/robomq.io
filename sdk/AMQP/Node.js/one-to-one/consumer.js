/**
* File: consumer.js
* Description: This is the AMQP consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     Messages can be received over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified binding key.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*/

var amqp = require("amqp");

var server = "localhost";
var port = 5672;
var vhost = "/";
var username = "guest";
var password = "guest";
var queueName = "testQ";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
connection.on("ready", function(){
	//one-to-one messaging uses the default exchange, where queue name is the routing key
	var queue = connection.queue(queueName, options = {autoDelete: true}, function(queue){
		queue.subscribe(options = {ack: false}, function(message, headers, deliveryInfo, messageObject){
			//callback funtion on receiving messages
			console.log(message.data.toString());
		});
	});
});

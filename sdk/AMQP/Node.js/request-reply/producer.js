/**
* File: producer.js
* Description: This is the AMQP producer publishes outgoing AMQP
*     communication to  clients consuming messages from a broker server.
*     Messages can be sent over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified routing key.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*/

var amqp = require("amqp");
var uuid = require('node-uuid').v4;

var server = "hostname";
var port = 5672;
var vhost = "yourvhost";
var username = "username";
var password = "password";
var exchangeName = "testEx";
var repQueueName = "replyQ";
var reqRoutingKey = "request";
var repRoutingKey = "reply";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
//node amqp library will automatically reconnect on exception
connection.on("ready", function(){
	connection.exchange(exchangeName, options = {type: "direct", autoDelete: true, confirm: true}, function(exchange){
		//listen for reply messages
		var queue = connection.queue(repQueueName, options = {exclusive: true, autoDelete: true}, function(queue){
			queue.bind(exchangeName, repRoutingKey, function(){
				queue.subscribe(options = {ack: false}, function(message, headers, deliveryInfo, messageObject){
					//callback funtion on receiving reply messages
					console.log(message.data.toString());
					connection.disconnect();
					process.exit(0);
				});
				//publish the request
				exchange.publish(reqRoutingKey, message = "Hello World!", options = {contentType: "text/plain", deliveryMode: 1, replyTo: repRoutingKey, correlationId: uuid()});
			});
		});
	}); 
});

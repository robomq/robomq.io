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

var server = "hostname";
var port = 5672;
var vhost = "yourvhost";
var username = "username";
var password = "password";
var exchangeName = "testEx";
var reqQueueName = "requestQ";
var reqRoutingKey = "request";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
//node amqp library will automatically reconnect on exception
connection.on("ready", function(){
	connection.exchange(exchangeName, options = {type: "direct", autoDelete: true, confirm: true}, function(exchange){
		var queue = connection.queue(reqQueueName, options = {exclusive: true, autoDelete: true}, function(queue){
			queue.bind(exchangeName, reqRoutingKey, function(){
				queue.subscribe(options = {ack: true}, function(message, headers, deliveryInfo, messageObject){
					//callback funtion on receiving request messages, reply to the reply_to header
					console.log(message.data.toString());
					exchange.publish(deliveryInfo.replyTo, "Reply to " + message.data.toString(), options = {contentType: "text/plain", deliveryMode: 1, correlationId: deliveryInfo.correlationId}, function(){
						messageObject.acknowledge(false);
					});
				});
			});
		});
	}); 
});

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

var server = "localhost";
var port = 5672;
var vhost = "/";
var username = "guest";
var password = "guest";
var exchangeName = "testEx";
var repQueueName = "replyQ";
var reqRoutingKey = "request";
var repRoutingKey = "reply";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
connection.on("ready", function(){
	connection.exchange(exchangeName, options = {type: "direct", autoDelete: true, confirm: true}, function(exchange){
		var queue = connection.queue(repQueueName, options = {exclusive: true, autoDelete: true}, function(queue){
			queue.bind(exchangeName, repRoutingKey, function(){
				queue.subscribe(options = {ack: false}, function(message, headers, deliveryInfo, messageObject){
					//callback funtion on receiving messages
					console.log(message.data.toString());
					connection.disconnect();
					process.exit(0);
				});
				exchange.publish(reqRoutingKey, message = "Hello World!", options = {contentType: "text/plain", replyTo: repRoutingKey, correlationId: uuid()});
			});
		});
	}); 
});

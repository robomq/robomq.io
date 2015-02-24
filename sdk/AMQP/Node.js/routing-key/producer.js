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

var server = "localhost";
var port = 5672;
var vhost = "/";
var username = "guest";
var password = "guest";
var exchangeName = "testEx";
var routingKey = "test";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
connection.on("ready", function(){
	connection.exchange(exchangeName, options = {type: "direct", autoDelete: true, confirm: true}, function(exchange){
		exchange.publish(routingKey, message = "Hello World!", options = {contentType: "text/plain"}, function(){
			connection.disconnect();
			process.exit(0);
		});
	}); 
});

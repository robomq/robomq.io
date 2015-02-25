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

var server = "hostname";
var port = 5672;
var vhost = "yourvhost";
var username = "username";
var password = "password";
var exchangeName = "testEx";

var connection = amqp.createConnection({host: server, port: port, vhost: vhost, login: username, password: password});
//node amqp library will automatically reconnect on exception
connection.on("ready", function(){
	connection.exchange(exchangeName, options = {type: "fanout", autoDelete: true, confirm: true}, function(exchange){
		//for fanout type exchange, routing key is useless
		exchange.publish("", message = "Hello World!", options = {contentType: "text/plain", deliveryMode: 1}, function(){
			connection.disconnect();
			process.exit(0);
		});
	}); 
});

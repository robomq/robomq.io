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
var port = "5672";
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var exchangeName = "testEx";
var replyQueue = "replyQ";
var requestKey = "request";
var replyKey = "reply";

producer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost);
producer.then(function(conn) {
	return conn.createChannel().then(function(ch) {
		//listen for reply messages
		ch.assertQueue(replyQueue, {durable: false, autoDelete: true, exclusive: true});
		ch.bindQueue(replyQueue, exchangeName, replyKey);
		ch.consume(replyQueue, function(message) {
			//callback funtion on receiving reply messages
			console.log(message.content.toString());
			//close connection once receives the reply
			conn.close();
		}, {noAck: true});
		//send the request message after 1 second
		setTimeout(function() {
			ch.publish(exchangeName, requestKey, content = new Buffer("Hello World!"), options = {contentType: "text/plain", deliveryMode: 1, replyTo: replyKey}, function(err, ok) {
				if (err != null) {
					console.error("Error: failed to send message\n" + err);
				}
			});
		}, 1000);
	});
}).then(null, function(err) {
	console.error(err);
}); 

/**
* File: consumer.js
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Request-reply method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
var amqp = require('amqp');
var connection = amqp.createConnection({ host: 'your host', port: 'port' });

connection.on('ready', function(){
	connection.exchange('exchangeName', options={type:'direct',
		autoDelete:false}, function(exchange){
		var queue = connection.queue('queueName', function(queue){
			console.log('Declare one queue, name is ' + queue.name);
			queue.bind('exchangeName', 'routingKey');
			queue.subscribe(function (message, headers, deliveryInfo, messageObject) {
				console.log('Server: Received message detail as follow:');
				var replyOption = {correlationId:deliveryInfo.correlationId};				
				exchange.publish('Reply','reply form consumer',replyOption);
				console.log('Server has replied');
			});
		});
	});
});


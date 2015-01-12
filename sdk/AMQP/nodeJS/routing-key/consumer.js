/**
* File: consumer.js
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Routing-key method.
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
		var queue = connection.queue('queueName', options={},function(queue){
			console.log('Declare one queue, name is ' + queue.name);
			queue.bind('exchangeName', 'routingKey');
			queue.subscribe(function (msg){
				console.log('consumer received the message'+msg.data);
			});	
		});
	});
});

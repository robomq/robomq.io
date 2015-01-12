/**
* File: producer.js
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Request-reply method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
var amqp = require('amqp');
var uuid = require('node-uuid').v4;
var connection = amqp.createConnection({ host: 'your host', port: 'port' });

connection.on('ready',function(){
	connection.exchange('exchangeName',{type:'direct',
		autoDelete:false, confirm:true}, function(exchange){
			console.log('start send message');
			replyQueue = connection.queue('Reply',{autoDelete:false});
			replyQueue.bind('exchangeName','Reply');
			var messageid = uuid();
			console.log('message id is :'+ messageid);
			exchange.publish('routingKey','hello world',{mandatory:true,contentType:'text/plain',replyTo:'Reply',correlationId:messageid},function(message) {
				if (message == false){
					console.log('Client: message has delivered');
					return;
				}
			});	
		replyQueue.subscribe(function (message, headers, deliveryInfo, messageObject) {
			console.log('Client: get reply message is \' %s \'', message.data);
		});
	});
});


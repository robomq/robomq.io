/**
* File: producer.js
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. One-to-one method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
var amqp = require('amqp');
var connection = amqp.createConnection({ host: 'your host', port: 'port' });

connection.on('ready',function(){
	connection.exchange('', options={type:'direct',	autoDelete:false}, function(exchange){
		exchange.publish('routingKey', 'hello world');
        print('message sent');
	});
});

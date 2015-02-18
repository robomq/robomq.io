# File: consumer.py
# Description: This is the AMQP consumer handles incoming
#     communication from clients publishing messages to a broker server.
#     Messages can be received over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified binding key.
#
# Author: Stanley
# robomq.io (http://www.robomq.io)

import pika

server = "localhost"
port = 5672
vhost = "/" 
username = "guest"
password = "guest"
exchangeName = "testEx"
reqQueueName = "requestQ"
reqRoutingKey = "request"

#callback funtion on receiving request messages, reply by correlation_id
def onMessage(channel, method, properties, body):
	print body
	channel.basic_publish(exchange = exchangeName, routing_key = properties.reply_to, properties=pika.BasicProperties(correlation_id = properties.correlation_id), body = "Reply to %s" % (body))
	channel.basic_ack(delivery_tag = method.delivery_tag)

#connect
credentials = pika.PlainCredentials(username, password)
connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials))
channel = connection.channel()

#declare exchange and queue, bind them and consume messages
channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
channel.queue_declare(queue = reqQueueName, exclusive = True, auto_delete = True)
channel.queue_bind(exchange = exchangeName, queue = reqQueueName, routing_key = reqRoutingKey)
channel.basic_consume(onMessage, queue = reqQueueName, no_ack = False)
channel.start_consuming()

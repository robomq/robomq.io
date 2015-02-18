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
queueName = "testQ1"

#callback funtion on receiving messages
def onMessage(channel, method, properties, body):
	print body

#connect
credentials = pika.PlainCredentials(username, password)
connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials))
channel = connection.channel()

#declare exchange and queue, bind them and consume messages
#for fanout type exchange, routing key is useless
channel.exchange_declare(exchange = exchangeName, exchange_type = "fanout", auto_delete = True)
channel.queue_declare(queue = queueName, exclusive = True, auto_delete = True)
channel.queue_bind(exchange = exchangeName, queue = queueName, routing_key=None)
channel.basic_consume(onMessage, queue = queueName, no_ack = True)
channel.start_consuming()

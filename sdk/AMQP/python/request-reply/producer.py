# File: producer.py
# Description: This is the AMQP producer publishes outgoing AMQP
#     communication to  clients consuming messages from a broker server.
#     Messages can be sent over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified routing key.
#
# Author: Stanley
# robomq.io (http://www.robomq.io)

import pika
import thread
import uuid
import time

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
replyQueue = "replyQ"
requestKey = "request"
replyKey = "reply"

#callback funtion on receiving reply messages
def onMessage(channel, method, properties, body):
	print body
	channel.stop_consuming()

#listen for reply messages
def listen():
	channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
	channel.queue_declare(queue = replyQueue, exclusive = True, auto_delete = True)
	channel.queue_bind(exchange = exchangeName, queue = replyQueue, routing_key = replyKey)
	channel.basic_consume(consumer_callback = onMessage, queue = replyQueue, no_ack = True)
	channel.start_consuming()

try:
	#connect
	credentials = pika.PlainCredentials(username, password)
	connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials))
	channel = connection.channel()

	thread.start_new_thread(listen, ())
	time.sleep(1) #give time for it to start consuming

	#send message
	properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1, correlation_id = str(uuid.uuid4()), reply_to = replyKey)
	channel.basic_publish(exchange = exchangeName, routing_key = requestKey, body = "Hello World!", properties = properties)
	time.sleep(1) #give time for it to receive the reply

	#disconnect
	connection.close()
except Exception, e:
	print e

# File: consumer.py
# Description: This is the AMQP consumer handles incoming
#     communication from clients publishing messages to a broker server.
#     Messages can be received over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified binding key.
#
# Author: Stanley
# robomq.io (http://www.robomq.io)

import pika
import time

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
requestQueue = "requestQ"
requestKey = "request"

#callback funtion on receiving request messages, reply to the reply_to header
def onMessage(channel, method, properties, body):
	print body
	try:
		replyProp = pika.BasicProperties(content_type = "text/plain", delivery_mode = 1)
		channel.basic_publish(exchange = exchangeName, routing_key = properties.reply_to, properties = replyProp, body = "Reply to %s" % (body))
		channel.basic_ack(delivery_tag = method.delivery_tag)
	except:
		channel.basic_nack(delivery_tag = method.delivery_tag)
		
while True:
	try:
		#connect
		credentials = pika.PlainCredentials(username, password)
		connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials))
		channel = connection.channel()

		#declare exchange and queue, bind them and consume messages
		channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
		channel.queue_declare(queue = requestQueue, exclusive = True, auto_delete = True)
		channel.queue_bind(exchange = exchangeName, queue = requestQueue, routing_key = requestKey)
		channel.basic_consume(consumer_callback = onMessage, queue = requestQueue, no_ack = False)
		channel.start_consuming()
	except Exception, e:
		#reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e
		try:
			connection.close()
		except:
			pass
		time.sleep(5)
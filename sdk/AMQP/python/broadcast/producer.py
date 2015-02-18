# File: producer.py
# Description: This is the AMQP producer publishes outgoing AMQP
#     communication to  clients consuming messages from a broker server.
#     Messages can be sent over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified routing key.
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

#connect
credentials = pika.PlainCredentials(username, password)
connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials))
channel = connection.channel()

#send message
#for fanout type exchange, routing key is useless
properties = pika.spec.BasicProperties(content_type = "text/plain")
channel.basic_publish(exchange = exchangeName, routing_key = "", body = "Hello World!", properties = properties)

#disconnect
connection.close()

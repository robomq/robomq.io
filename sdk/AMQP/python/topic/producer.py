# File: producer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Topic method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika
import sys

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
channel = connection.channel()

channel.exchange_declare(exchange='exchangeName',
                         type='topic')

routing_key ='routingKey'
message = 'hello world'
channel.basic_publish(exchange='exchangeName',
                      routing_key=routing_key,
                      body=message)
print 'Sent %r:%r' % (routing_key, message)
connection.close()
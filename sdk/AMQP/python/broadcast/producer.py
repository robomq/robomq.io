# File: producer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Broadcast method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika
import sys

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your port'))
channel = connection.channel()

channel.exchange_declare(exchange='exchangeName',
                         type='fanout')

message = 'hello world'
channel.basic_publish(exchange='exchangeName',
                      routing_key='',
                      body=message)
print 'Sent %r' % (message)
connection.close()
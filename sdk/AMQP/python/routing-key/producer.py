# File: producer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Routing-key method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika
import sys

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
channel = connection.channel()

channel.exchange_declare(exchange='exchangeName',
                         type='direct')

severity = 'routingKey'
message = 'Hello World!'
channel.basic_publish(exchange='exchangeName',
                      routing_key=severity,
                      body=message)
print 'Sent %r:%r' % (severity, message)
connection.close()
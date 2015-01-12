# File: producer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. One-to-one method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
channel = connection.channel()

channel.queue_declare(queue='queueName')

channel.basic_publish(exchange='',
                      routing_key='queueName',
                      body='Hello World!')
print 'Sent 'Hello World!''
connection.close()
#File: consumer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. One-to-one method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
channel = connection.channel()

channel.queue_declare(queue='queueName')

print 'Waiting for messages. To exit press CTRL+C'

def callback(ch, method, properties, body):
    print 'Received %r' % (body,)

channel.basic_consume(callback,
                      queue='queueName',
                      no_ack=True)

channel.start_consuming()
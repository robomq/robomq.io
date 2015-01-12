#File: consumer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Broadcast method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
channel = connection.channel()

channel.exchange_declare(exchange='exchangeName',
                         type='fanout')
queue_name = 'queueName'
channel.queue_declare(queue=queue_name,exclusive=True)
channel.queue_bind(exchange='exchangeName',
                   queue=queue_name)

print 'Waiting for logs. To exit press CTRL+C'

def callback(ch, method, properties, body):
    print '%r' % (body)

channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)

channel.start_consuming()
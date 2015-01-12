#File: consumer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Request-reply method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))

channel = connection.channel()

channel.queue_declare(queue='queueName')
channel.exchange_declare(exchange='exchangeName',type='direct')

channel.queue_bind(exchange='exchangeName',queue='queueName',routing_key='routingKey')

def callback(ch, method, props, body):
    ch.basic_publish(exchange='exchangeName',
                     routing_key='reply',
                     properties=pika.BasicProperties(correlation_id = props.correlation_id),
                     body='reply')
    ch.basic_ack(delivery_tag = method.delivery_tag)

channel.basic_consume(callback, queue='queueName')

print 'Awaiting requests'
channel.start_consuming()
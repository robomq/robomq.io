# File: producer.py
# Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Request-reply method.
# Author: Stanley
# robomq.io (http://www.robomq.io)
import pika
import uuid

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))

channel = connection.channel()
channel.exchange_declare(exchange='exchangeName', type='direct')
channel.queue_declare(queue='replyQueue')
channel.queue_bind(exchange='exchangeName', queue='replyQueue',routing_key='reply')
routingKey = 'routingKey'
message = 'hello world'
corr_id = str(uuid.uuid4())
self.channel.basic_publish(exchange='',
	routing_key='routingKey',
	properties=pika.BasicProperties(
		reply_to = 'replyQueue',
		correlation_id = corr_id,
		),
	body=str(n))
print'message sent'
def callback(ch, method, properties, body):
    print body

channel.basic_consume(callback,
                      queue='reply',
                      no_ack=True)

channel.start_consuming()

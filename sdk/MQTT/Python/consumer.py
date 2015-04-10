"""
 * File: consumer.py
 * Description: This is the MQTT consumer handles incoming messages
 *     published by producers from a particular topic.
 *     Consumer prints the topic and payload as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
"""

import time
import paho.mqtt.client as mqtt

server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/#"

"""
 * This method is the callback on connecting to broker.
 * @ It subscribes the target topic.
"""
def onConnect(client, userdata, rc):	#event on connecting
	client.subscribe([(topic, 1)])	#subscribe

"""
 * This method is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
"""
def onMessage(client, userdata, message):	#event on receiving message
	print("Topic: " + message.topic + ", Message: " + message.payload)

while True:
	try:
		client = mqtt.Client(client_id = "", clean_session = True, userdata = None, protocol = "MQTTv31")
		client.username_pw_set(vhost + ":" + username, password)
		client.on_connect = onConnect
		client.on_message = onMessage
		client.connect(server, port, keepalive = 60, bind_address = "")	#connect
		client.loop_forever()	#automatically reconnect once loop forever
	except Exception, e:
		#when initialize connection, reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e 
		time.sleep(5)
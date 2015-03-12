"""
 * File: procuder.py
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
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
topic = "test/any"

try:
	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(vhost + ":" + username, password)
	client.connect(server, port, keepalive=60, bind_address="")	#connect
	client.loop_start()	#start loop
	msgNum = int(input("Quantity of test messages: "))
	for i in range(msgNum):
		message = "test msg " + str(i + 1)
		client.publish(topic, payload=message, qos=1, retain=False)	#publish
		time.sleep(1)
	client.loop_stop()	#stop loop
	client.disconnect()
except Exception, e:
	print e
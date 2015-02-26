"""
 * File: procuder.py
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
"""

import sys
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
except:
	print("Error: Failed to connect and start loop")
	sys.exit(-1)

msgNum = int(input("Quantity of test messages: "))
for i in range(msgNum):
	message = "test msg " + str(i + 1)
	try:
		client.publish(topic, payload=message, qos=1, retain=False)	#publish
	except:
		print("Error: Failed to publish message")
		sys.exit(-1)		

try:
	client.loop_stop()	#stop loop
	client.disconnect()
except:
	print("Error: Failed to stop loop and disconnect")
	sys.exit(-1)

"""
 * File: consumer.py
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
"""

import time
from stompest.config import StompConfig
from stompest.sync import Stomp

server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"	#There're more options other than /queue/...

while True:
	try:
		client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
		client.connect(versions = ["1.2"], host = vhost, heartBeats = (0, 60000))	#CONNECT
		subscription = client.subscribe(destination, {"ack": "client", "id": "0"})	#SUBSCRIBE
		while True:
			frame = client.receiveFrame()
			try:
				print frame.body
				client.ack(frame)	#ACK
			except:
				print "Error: Can't handle message received, NACKing"
				client.nack(frame)	#NACK
	except Exception, e:
		#reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e
		try:
			client.disconnect()
		except:
			pass
		time.sleep(5)
"""
 * File: consumer.py
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
"""

import sys
from stompest.config import StompConfig
from stompest.protocol import StompSpec
from stompest.sync import Stomp

server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"	#There're more options other than /queue/...

try:
	client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
	client.connect(versions = ["1.2"], host = vhost)	#CONNECT
except:
	print "Error: Can't initialize connection"
	sys.exit()

try:	
	subscription = client.subscribe(destination, {StompSpec.ACK_HEADER: StompSpec.ACK_CLIENT_INDIVIDUAL, StompSpec.ID_HEADER: '0'})	#SUBSCRIBE
except:
	print "Error: Can't subscribe queue"
	sys.exit()
		
while True:
	frame = client.receiveFrame()
	try:
		print "%s" % frame.body
		client.ack(frame)	#ACK
	except:
		print "Error: Can't handle message received, NACKing"
		client.nack(frame)	#NACK

#client.unsubscribe(subscription)	#UNSUBSCRIBE
#client.disconnect()	#DISCONNECT

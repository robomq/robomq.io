"""
 * File: procuder.py
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
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

try:
	client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
	client.connect(versions = ["1.2"], host = vhost)	#CONNECT
	msgNum = int(input("Quantity of test messages: "))
	for i in range(msgNum):	
		message = "test msg " + str(i + 1)
		client.send(destination, body = message, headers = None, receipt = None)	#SEND
		time.sleep(1)	
	client.disconnect()	#DISCONNECT
except Exception, e:
	print e
#IoT and M2M Integration

Device, sensors and systems connecting together are driving the next generation of applications creating the Internet of Things (IoT). [robomq.io](http://www.robomq.io) provides an open standard based platform to connect your devices and sensors to the back end applications, systems, processes and people to build the nest big thing. You can work with familiar protocols like MQTT ( MQ for Telemetry), AMQP (Advanced Message Queue Protocol) and STOMP ( Simple Text Oriented Messaging Protocol) while using robomq.


The protocols supported by [robomq.io](http://www.robomq.io) can run on very small footprint devices using a choice among languages that are supported by device OS and profile.

A common choice of devices include Raspberry Pi, Audrino, Beaglebone and mBed based platforms. The most basic requirement is the support for running one languages and network connectivity. You should however be cognizant of the device footprint when choosing the messaging protocol.

For the example case, we have chosen Raspberry Pi device running a small Linux kernel (a variant of Debian Linux) and Python programming language.

##Raspberry Pi as an example device

The Raspberry Pi is a credit-card sized micro-computer that plugs into your TV and a keyboard. It is a capable little computer which can be used in electronics projects. In this guide you will be introduced how to use raspberry pi to acquire information from real-world via sensors and other electrical components, and then integrate raspberry pi into [robomq.io](http://www.robomq.io).

![RaspberryPi](./images/raspberryPi.jpg)

###Getting raspberry pi ready

There may be some steps to do when you have all the devices and materials. For example you need to install a system into the micro-SD card which your raspberry pi will be boot from.

For detailed instructions on how to setup a raspberry pi, please go to the official raspberry pi document page.

[http://www.raspberrypi.org/documentation/](http://www.raspberrypi.org/documentation/ "Raspberry Pi Documentation")

###Prerequisite


###Raspberry Pi

Although any model of raspberry pi will work, we recommend the newest version. In this documentation we will use the model raspberry pi B+ for the example. You can buy a raspberry pi from their official site:

[http://www.raspberrypi.org/products/](http://www.raspberrypi.org/products/ "raspberry product")

###Input device

For raspberry B+ there are 4 USB ports on the board. You can plug in a USB keyboard and mouse to control your raspberry pi.

###Power source

A typical Android cellphone charger is good for raspberry pi.

###Monitor(optional)

Raspberry Pi has an HDMI port for display. It may be easier and more comfortable for you to work with your raspberry pi directly on it's own system GUI. But if you do not, there is no problem because you can also use you laptop to ssh the raspberry and control it in the terminal. For tutorial how to ssh a raspberry you can refer to Adafruit Learning System:

[https://learn.adafruit.com/adafruits-raspberry-pi-lesson-6-using-ssh](https://learn.adafruit.com/adafruits-raspberry-pi-lesson-6-using-ssh "raspberry pi ssh")

###Sensors:

Raspberry pi is compatible with a lot of sensors. In our documentation we are going to show 3 sensors for demonstration: DHT11 humidity & temperature sensor, PIR motion sensor and light sensor. You can get these sensors from online shopping site(Amazon, ebay, etc).

###Breadboard and jumper wires

These are used to connect sensors to the raspberry pi. You can also get these from online shopping sites.


![components](./images/components.jpg)


###Python

This is the language we recommend for raspberry pi-sensor programming. As it's handy, simple, and is supported by raspberry pi. Python 2.7.3 or above is preferred.




###Raspberry Pi and sensors

![Connected Sensors](./images/connectedSensors.jpg)
With raspberry pi we can easily develop some sensor projects. In this part we are going to show 3 examples: PIR motion sensor, DHT11 humidity & temperature sensor, and light sensor. Before integrate these sensors into [robomq.io](http://www.robomq.io), we would try them locally on your raspberry. You can see how them work in your raspberry pi LXTerminal.

For details about how to set up and run the sensors with raspberry pi, please go to the **Adafruit Learning System**.

###DHT11 humidity & temperature sensor

![DHT sensor](./images/DHTSensor.jpg)

[https://learn.adafruit.com/downloads/pdf/dht-humidity-sensing-on-raspberry-pi-with-gdocs-logging.pdf](https://learn.adafruit.com/downloads/pdf/dht-humidity-sensing-on-raspberry-pi-with-gdocs-logging.pdf "DHT11 humidity & temperature sensor")

###Light sensor

![ Light sensor](./images/LightSensor.jpg)

[https://learn.adafruit.com/basic-resistor-sensor-reading-on-raspberry-pi/basic-photocell-reading](https://learn.adafruit.com/basic-resistor-sensor-reading-on-raspberry-pi/basic-photocell-reading "Light sensor")

###PIR motion sensor

![Motion sensor](./images/motionSensor.jpg)

[https://learn.adafruit.com/adafruits-raspberry-pi-lesson-12-sensing-movement/overview](https://learn.adafruit.com/adafruits-raspberry-pi-lesson-12-sensing-movement/overview "PIR motion sensor")



##Integrate Raspberry Pi with robomq.io

After you finished setting it up and testing it locally, you may start to think: Why would I do this? What can I do with this in real life? 

Okay, let's talk about a scenario for this. You are a Device Integration Engineer working at [robomq.io](http://www.robomq.io). One day a customer contact you. He said he is an employee in a water & sewer utility company. They need a system to monitor their water wells. They need temperature, humidity, brightness, and intruder detection information to be detected as it happens and, for some, at a defined interval. And for the convenience of management and ability to take immediate actions, they need all the information sent to their control center.

As an smart engineer, you have already developed the information collecting functions, as discussed in sections above, by working with raspberry pi and sensors locally. What you need now is to write some code for sending the information acquired from the sensors via [robomq.io](http://www.robomq.io) to the water & sewer utility company's control center for monitoring, alerts, analytics and other business specific actions.

For sending messages, the main job is to create a producer method, then call this method in your raspberry pi & sensor programs. Follow the sections below to build this application. You will find it easy, handy and fun !!!

##Messaging with AMQP

####Create Producer

First, launch your raspberry pi and create a file named "producer.py", then paste the following code into the file:


	import sys, os, pika

	stationID = "well1"
	coordinate = [-89, 64]
	server = "hostname"
	port = "5672"
	vhost = "waterSupply"
	username = "username"
	password = "password"
	exchange = "amq.topic"
	topic = "sensors"
	parameters = pika.URLParameters("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost)

	def connect():
		try:
			connection = pika.BlockingConnection(parameters)
			channel = connection.channel()
			return channel
		except:
			print "Error: Failed to connect broker"

	channel = connect() #connect to broker

	#publish message
	def send(msgJson):
		global channel
		msgJson["stationID"] = stationID
		msgJson["coordinate"] = coordinate
		properties = pika.BasicProperties(content_type = "application/json", delivery_mode = 1)
		try:
			channel.basic_publish(exchange, topic, json.dumps(msgJson, ensure_ascii=False), properties)
		except:
			print "Error: Failed to send message"
			channel = connect() #reconnect to broker


Above is the "producer.py" file that you are going to call in your raspberry pi-sensor codes to send those messages to [robomq.io](http://www.robomq.io).Now we are all set for the producer, after this we are going to do a little modification in the sensor codes.

####Modify Sensor Programs

In this part we are going to combine the DHT11 sensor and the Light sensor into one program, just for clean purpose.

Revise your DHT11 sensor and Light sensor program as follow:


    import RPi.GPIO as gpio
    import Adafruit_DHT
    import time
    import os
    import sys
    import producer      

    DEBUG = 1
    gpio.setmode(gpio.BCM)

    htsensor = Adafruit_DHT.DHT11
    htpin = 4
    lpin=21

    while True:
        humidity, temperature = Adafruit_DHT.read_retry(htsensor, htpin)
        if humidity is not None and temperature is not None:
            temperature = temperature * 9 / 5 + 32
            print 'Temp={0:0.1f}*F  Humid={1:0.1f}%'.format(temperature, humidity)
        else:
            print 'Failed to get reading. Try again!'

    reading = 0
    gpio.setup(lpin, gpio.OUT)
    gpio.output(lpin, gpio.LOW)
    time.sleep(1)
    gpio.setup(lpin, gpio.IN)
    while (gpio.input(lpin) == gpio.LOW):
        reading += 1
    luminance = 100 - reading
    print ('Luminance='), luminance

    message = {"time": time.time() * 1000}
    message["temperature"] = temperature
    message["humidity"] = humidity
    message["luminance"] = luminance
    try:
        producer.send(message)
    except:
        print "Error: failed to send data through producer"

    time.sleep(30) #interval


And then revise your PIR motion sensor as follow:


    import RPi.GPIO as gpio
    import time
    import producer

    gpio.setmode(gpio.BOARD)
    mpin = 31
    gpio.setup(mpin,gpio.IN)

    while True:
        gpio.wait_for_edge(mpin, gpio.RISING)
        print('Intruder detected')
        message = {"time": time.time() * 1000}
        message["intruder"] = 1
        try:
            producer.send(message)
        except:
            print "Error: failed to send data through producer"
        gpio.remove_event_detect(mpin)
        
        gpio.wait_for_edge(mpin, gpio.FALLING)
        print('Intruder left or froze')
        message = {"time": time.time() * 1000}
        message["intruder"] = 0
        try:
            producer.send(message)
        except:
            print "Error: failed to send data through producer"
        gpio.remove_event_detect(mpin)


Now you can see all the messages been sent via [robomq.io](http://www.robomq.io) to the dashboard.

##Messaging with MQTT

In the above example we fulfilled message sending by creating a producer method and call it in the sensor program. However there are more than one way we can build a producer as [robomq.io](http://www.robomq.io) supports multiple protocols. Now we are going to show you one more example how to send messages using the MQTT protocol followed by an example using STOMP.

For MQTT, the only difference is the producer code. Modify the "producer.py" as follow:

	
	import sys, os
	import paho.mqtt.client as mqtt

	stationID = "well1"
	coordinate = [-89, 64]
	server = "hostname"
	port = "5672"
	vhost = "waterSupply"
	username = "username"
	password = "password"
	topic = "sensors"

	def connect():
		try:
			client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
			client.username_pw_set(vhost + ":" + username, password)
			client.connect(server, port, keepalive=60, bind_address="")
			client.loop_start()
			return client
		except:
			print "Error: Failed to connect broker"

	client = connect() #connect to broker

	#publish message
	def send(msgJson):
		global client
		msgJson["stationID"] = stationID
		msgJson["coordinate"] = coordinate
		try:
			client.publish(topic, payload=json.dumps(msgJson, ensure_ascii=False), qos=1, retain=False)
		except:
			print "Error: Failed to send message"
			channel = connect() #reconnect to broker

Since the difference is limited in producer program, there is no need to modify the sensor programs.


##Messaging with STOMP

For STOMP, no change is required to sensor programs, only modify your "producer.py" as follow:


	import sys, os
	from stompest.config import StompConfig
	from stompest.sync import Stomp

	stationID = "well1"
	coordinate = [-89, 64]
	server = "hostname"
	port = "5672"
	vhost = "waterSupply"
	username = "username"
	password = "password"
	topic = "sensors"

	def connect():
		try:
			client = Stomp(StompConfig("tcp://" + server + ":" + port, login = username, passcode = password, version = "1.2"))
			client.connect(versions = ["1.2"], host = vhost)
			return client
		except:
			print "Error: Failed to connect broker"

	client = connect() #connect to broker

	#publish message
	def send(msgJson):
		global client
		msgJson["stationID"] = stationID
		msgJson["coordinate"] = coordinate
		try:
			client.send("/topic/" + topic, json.dumps(msgJson, ensure_ascii=False))
		except:
			print "Error: Failed to send message"
			channel = connect() #reconnect to broker


As you can see, the "producer.py" code contains 2 methods. First is the "connect" method that is in charge of establishing connection to the [robomq.io](http://www.robomq.io) broker.  The other is the "send" method that publishes messages through [robomq.io](http://www.robomq.io) to any listening consumers.

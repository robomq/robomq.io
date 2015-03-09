[robomq.io](http://www.robomq.io) has rich library of examples code in multiple programming languages supporting AMQP, MQTT and STOMP protocols. The [SDK](https://github.com/robomq/robomq.io/tree/master/sdk) includes example code for multiple Messaging Integration Patterns (MEPs). All examples have implemented automatic reconnecting, which is crucial in real production.  

#Supported protocols
You can send and receive messages using [robomq.io](http://www.robomq.io) platform from a choice of [AMQP](http://www.amqp.org/), [MQTT](http://mqtt.org/) and [STOMP](https://stomp.github.io/) protocols.
	 
Each of the protocols can be used for variety of messaging integration patterns. MQTT and STOMP, being relatively light weight protocol, are ideal for small footprint devices. AMQP could be the protocol of choice for more capable applications and enterprise systems. 
	
##[AMQP use cases](https://github.com/robomq/robomq.io/tree/master/sdk/AMQP)

Following use cases using AMQP protocols are documented with code on the [robomq.io GitHub](https://github.com/robomq/robomq.io/tree/master/sdk/AMQP).  

1. One-to-one or direct messaging : point to point message transportation

2. Broadcast : sending messages to all subscribed consumers

3. Key based routing : routing messages to consumer based on key based subscription 

4. Topic : routing messages based on complex filter rules applied to routing keys 

5. Request and reply : two way request reply communication


##[MQTT use cases](https://github.com/robomq/robomq.io/tree/master/sdk/MQTT)

MQTT (Message Queues for Telemetry Transport) is lighter weight protocol for device specific use cases supporting pub-sub messaging pattern. MQTT code examples are on [robomq.io GitHub](https://github.com/robomq/robomq.io/tree/master/sdk/MQTT)

##[STOMP use cases](https://github.com/robomq/robomq.io/tree/master/sdk/STOMP)

STOMP (Simple Text Oriented Messaging Protocol) is a HTTP like simple protocol and can be used for variety of use cases with very little programming. STOMP code examples are on [robomq.io GitHub](https://github.com/robomq/robomq.io/tree/master/sdk/STOMP)

##Supported programming languages

[robomq.io](http://www.robomq.io)  supports majority of the programming languages. For most programming languages, the client side libraries exists for AMPQ, MQTT and STOMP protocols. The [SDK on GitHub](https://github.com/robomq/robomq.io/tree/master/sdk) contains examples in Python, Java, C, C++, node.js and PHP. We continue to add examples in additional languages.  



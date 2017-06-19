<a href="https://www.robomq.io" target="_blank">RoboMQ</a> has rich library of examples code in multiple programming languages supporting AMQP, MQTT and STOMP protocols. The <a href="https://github.com/robomq/robomq.io/tree/master/sdk" target="_blank">SDK</a> includes example code for multiple Messaging Integration Patterns (MEPs). All examples have implemented automatic reconnecting, which is crucial in real production.  

#Supported protocols
You can send and receive messages using <a href="https://www.robomq.io" target="_blank">RoboMQ</a> platform from a choice of [AMQP](http://www.amqp.org/) <a href="https://www.amqp.org/" target="_blank">AMQP</a>, <a href="https://www.mqtt.org" target="_blank">MQTT</a> and <a href="https://stomp.github.io/" target="_blank">STOMP</a> protocols.
	 
Each of the protocols can be used for variety of messaging integration patterns. MQTT and STOMP, being relatively light weight protocol, are ideal for small footprint devices. AMQP could be the protocol of choice for more capable applications and enterprise systems. 
	
## <a href="https://github.com/robomq/robomq.io/tree/master/sdk/AMQP" target="_blank">AMQP use cases</a>

Following use cases using AMQP protocols are documented with code on the <a href="https://github.com/robomq/robomq.io/tree/master/sdk/AMQP" target="_blank">RoboMQ GitHub</a>.  

1. One-to-one or direct messaging : point to point message transportation

2. Broadcast : sending messages to all subscribed consumers

3. Key based routing : routing messages to consumer based on key based subscription 

4. Filter based routing (Topic) : routing messages based on complex filter rules applied to routing keys 

5. Request and reply : two way request reply communication

## <a href="https://github.com/robomq/robomq.io/tree/master/sdk/MQTT" target="_blank">MQTT use cases</a>

MQTT (Message Queues for Telemetry Transport) is lighter weight protocol for device specific use cases supporting pub-sub messaging pattern. MQTT code examples are on <a href="https://github.com/robomq/robomq.io/tree/master/sdk/MQTT" target="_blank">RoboMQ GitHub</a>

## <a href="https://github.com/robomq/robomq.io/tree/master/sdk/STOMP" target="_blank">STOMP use cases</a>

STOMP (Simple Text Oriented Messaging Protocol) is a HTTP like simple protocol and can be used for variety of use cases with very little programming. STOMP code examples are on <a href="https://github.com/robomq/robomq.io/tree/master/sdk/STOMP" target="_blank">RoboMQ GitHub</a>

## <a href="https://github.com/robomq/robomq.io/tree/master/sdk/" target="_blank">Other use cases</a>

### <a href="https://github.com/robomq/robomq.io/tree/master/sdk/WebSTOMP" target="_blank">WebSTOMP</a>

WebSTOMP is a simple bridge exposing the STOMP protocol over emulated HTML5 WebSockets, which makes it possible to use <a href="https://www.robomq.io" target="_blank">RoboMQ</a> from web browsers. WebSTOMP code examples are on <a href="https://github.com/robomq/robomq.io/tree/master/sdk/WebSTOMP" target="_blank">RoboMQ GitHub</a>

###[SSL](https://github.com/robomq/robomq.io/tree/master/sdk/SSL) <a href="https://github.com/robomq/robomq.io/tree/master/sdk/SSL" target="_blank">SSL</a>

<a href="https://www.robomq.io" target="_blank">RoboMQ</a> has obtained certificate from certificate authority and supports SSL connection for all available protocols. SSL code examples  are on <a href="https://github.com/robomq/robomq.io/tree/master/sdk/SSL" target="_blank">RoboMQ GitHub</a>

#Supported programming languages

<a href="https://www.robomq.io" target="_blank">RoboMQ</a> supports majority of the programming languages. For most programming languages, the client side libraries exists for AMPQ, MQTT and STOMP protocols. The <a href="https://github.com/robomq/robomq.io/tree/master/sdk" target="_blank">SDK on GitHub</a> contains examples in Python, Node.js, PHP, Java, C and C++. We continue to add examples in additional languages.  

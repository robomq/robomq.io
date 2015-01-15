#SDK & Example code

#Support portocols
Robomq.io provided 3 protocol for user to choose.  

1. [AMQP](http://www.amqp.org/)
2. [MQTT](http://mqtt.org/)
3. [STOMP](https://stomp.github.io/)
	 
All those protocol can be used in multiple scenario. 
MQTT and STOMP are relatively light weight protocol for message transportation. Recommend for small devices. 
AMQP can reach ideal target with strong network support and powerful processor.
 
	
###[AMQP support scenarios ](https://github.com/robomq/robomq.io/tree/master/sdk/AMQP)

1. one to one : point to point message transportation. 
2. broadcast : sending message to all subscribed consumers,
3. routing key based : sending message to the consumers which holding the matching routing key. 
4. topic : sending messages to all consumer whose routing key can pass filter rules.
5. request and reply : each messages sent will can get a reply from consumer. 

All [SDK](https://github.com/robomq/robomq.io) are created by SRB technologies team and hold on [github](https://github.com/robomq/robomq.io)<br>
For all detail, refer [robomq.io](https://github.com/robomq/robomq.io)

### [MQTT support scenarios](https://github.com/robomq/robomq.io/tree/master/sdk/MQTT)

MQTT is using the default amp.topic exchange as the only one for handle all the messages transportation. Make sure you using that exchange. This is light weight message protocol especially have good performance for M2M. Refer M2M section. 

###[STOMP support scenarios](https://github.com/robomq/robomq.io/tree/master/sdk/STOMP)

STOMP is an easy message protocol especial for light weight messages transport. Works Good on small devices, like Raspberry.

##Support programming languages

1. Java
2. C++
3. Python
4. Node.js
5. PHP



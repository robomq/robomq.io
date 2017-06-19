#Advantages of Application Integration using Message Queues

Message Oriented Middleware (MOM) have long been used for application integration and creating Enterprise Service Bus for developing scalable and decoupled applications.

<a href="https://www.robomq.io" target="_blank">RoboMQ</a>< platform is built with the AMQP broker at the core. The particular implementation of the AMQP broker used is <a href="https://www.robomq.io" target="_blank">RoboMQ</a>. RoboMQ thus provides all the capabilities and feature sets of the RabbitMQ platform with integrated management, dashboards, and analytics in a Software as a Service (SaaS) package.

There are certain distinct advantages of using Message Queues for application integration.

##Decoupled applications 

The applications developed using Message Queues are decouple by design. What it means is that your order submission application is not hard wired to your order processor and the order processor application is not hard wired to the shipping application, in atypical order management application example. All of these applications are working together by sending discreet messages to each other. By virtue of decoupling, you can:

 - Add more functionality and applications in the system without changing the existing code

 - Modify any part of the application without affecting other

 - Add more instances of a part of application to handle load. If there is a order rush but order processing takes longer time, you can add more order processor to handle increased load

 - Try different message delivery topologies to achieve complex process orchestration and grow the system organically

    ***Note*** : *Refer to this excellent book on Integration Design patterns to learn and build complex messaging applications <a href="https://www.eaipatterns.com/eaipatterns.html" target="_blank">Enterprise Integration Patterns</a>*  

##Scalable systems
Applications and systems built using Message Queue based hub are scalable by design. Additional worker processes can be added to the different part of the system to handle increased load. This elastic scaling allows quick and dynamic handling of the bottleneck applications

##Sophisticated integration patterns
Message queues allow building applications using variety of Message delivery patterns and their combination. Basic message delivery patterns are :

1. **Direct** - one to one message delivery from producers to consumers

2. **Topic or Fanout** - deliver one message to all interested consumers

3. **Request-Reply** - two way request reply communication

4. **Content based Routing** - delivering message based on routing 

5. **Work Queues** - round robin delivery of messages to multiple worker processes

SDK containing example code for these use cases can be found at <a href="https://github.com/robomq/robomq.io/tree/master/sdk" target="_blank">RoboMQ SDK</a> 

##Guaranteed Delivery
Message Queue based integration hub ensures guaranteed delivery of the message and critical information. Unlike synchronous point to point communication where recipient needs to available and be on-line, Message Queues deliver message when the recipient is available.

In addition, messages can be durable or persistent which will survive any catastrophic failure of the messaging system itself. 

##Deliver any type of content - text, JSON, binary, image or any arbitrary type
Message Queue based integration system do not enforce any data format or type of content. Any type of media or content can be delivered using Message Queues. The producer and consumer of the message can decide and process any kind of the messages.


*To learn more details about the benefits of message queues built on AMQP protocol, refer to <a href="https://www.amqp.org" target="_blank">AMQP website</a>*

*Learn about RabbitMQ and the details of its implementation of AMQP, read more at <a href="https://www.rabbitmq.com" target="_blank">RabbitMQ website</a>* 

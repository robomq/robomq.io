# Introduction

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> supports AMQP 0-9-1 as the main module of our broker. Its port is **5672**, SSL port is **5671**.    

AMQP (Advanced Message Queuing Protocol) is a networking protocol that enables conforming client applications to communicate with conforming messaging middleware brokers. Messaging brokers receive messages from producers (applications that publish them, also known as publishers) and route them to consumers (applications that process them). Since AMQP is a network protocol, the producers, consumers and the broker can all reside on different machines.
  
The AMQP 0-9-1 Model has the following view of the world: messages are published to exchanges, which are often compared to post offices or mailboxes. Exchanges then distribute message copies to queues using rules called bindings. Then AMQP brokers either deliver messages to consumers subscribed to queues, or consumers fetch / pull messages from queues on demand. Queues, exchanges and bindings are collectively referred to as AMQP entities.  

When publishing a message, producers may specify various message headers (message properties). Some of these headers may be used by the broker, however, the rest of it is completely opaque to the broker and is only used by applications that receive the message.  

Networks are unreliable and applications may fail to process messages therefore the AMQP model has a notion of message acknowledgement: when a message is delivered to a consumer the consumer notifies the broker, either automatically or as soon as the application developer chooses to do so. When message acknowledgement is in use, a broker will only completely remove a message from a queue when it receives a notification for that message (or group of messages). In certain situations, for example, when a message cannot be routed, messages may be returned to producers, dropped, or, if the broker implements an extension, placed into a so-called "dead letter queue". Producers choose how to handle situations like this by publishing messages using certain parameters.  

AMQP 0-9-1 is a programmable protocol in the sense that AMQP entities and routing schemes are defined by applications themselves, not a broker administrator. Accordingly, provision is made for protocol operations that declare queues and exchanges, define bindings between them, subscribe to queues and so on. This gives application developers a lot of freedom but also requires them to be aware of potential definition conflicts. In practice, definition conflicts are rare and often indicate a misconfiguration. Applications declare the AMQP entities that they need, define necessary routing schemes and may choose to delete AMQP entities when they are no longer used.  

You can read the <a href="https://www.amqp.org" target="_blank">full documentation of AMQP</a> or go through a <a href="https://www.rabbitmq.com/tutorials/amqp-concepts.html" target="_blank">simple tutorial of AMQP</a> for details.  

> Now, before proceeding to the following chapters, we assume that you already know AMQP protocol more or less, at least the basic concepts e.g broker, exchange, queue, producer, consumer, etc.  

# AMQP use cases

We will provide examples of five messaging scenarios, each in five languages, including Python, Node.js, PHP, Java and C or C++.  
The five messaging scenarios includes one-to-one, broadcast, routing key, routing filter (topic) and request-reply. They are respectively elaborated in the beginning of each's chapter.      

In the examples, details vary among five scenarios, but the typical procedure is as following. AMQP producer will publish a "Hello World!" message through a exchange with a routing key through <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker. AMQP consumer will create exchange and queue, then bind them with the routing key and start consuming messages from the queue. It will print the message as it receives messages.  

All examples have implemented automatic reconnecting, which is crucial in real production.

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK <a href="https://github.com/robomq/robomq.io/tree/master/sdk/AMQP" target="_blank">repository</a> on GitHub.  

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  
> Always run consumer first to create the exchange and queue for producer to send messages to.   
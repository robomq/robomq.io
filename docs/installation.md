#Installation
For using AMQP protocol as your major protocol of sending message and consuming message, first, you need to download some libraries for your programming language. 


##Python:

for install pika 
<pre>$ sudo pip install pika</pre>

Ubuntu
<pre>$ sudo apt-get install python-pip git-core</pre>

On Debian
<pre>
$ sudo apt-get install python-setuptools git-core
$ sudo easy_install pip
</pre>

on Windows
<pre>
	easy_install pip
	pip install pika
</pre>

##Java

Using amqp for java programming, need to the <a href ="http://www.rabbitmq.com/java-client.html">client library package</a>, and check its signature as described. Unzip it into your working directory and grab the JAR files from the unzipped directory:
<pre>
$ unzip rabbitmq-java-client-bin-\*.zip
$ cp rabbitmq-java-client-bin-\*/\*.jar ./
</pre>


##PHP
Using amqp for php programming, need to install <a href="https://github.com/videlalvaro/php-amqplib">php-amqplib</a>. 

##node.js 
Using amqp for node.js programming, need to install <a href ="https://github.com/postwait/node-amqp">node-amqp</a> library. 

##C/C++


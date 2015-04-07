# Introduction

> Browse the chapter of AMQP Introduction first if you're new to AMQP.  

[robomq.io](http://www.robomq.io) innovatively provides REST interface over the AMQP broker. It's only accessible over HTTPS.  

Our REST interface facilitates using [robomq.io](http://www.robomq.io) from any REST clients. Therefore, it allows you send and receive messages without installing any message queue client library. Two most useful cases of our REST interface are  

1. Send and receive messages through simple REST client, such as cURL. Get rid of programming.  
2. Send and receive messages from Web browser with a JavaScript REST client. No library installation is required.  

# Usage

HTTP GET and POST methods are supported. Each transaction consists of one AMQP message per HTTP request-response.  

1. GET method gets a message from particular queue, which is bound to an exchange with a routing key.  
2. POST method published a message to particular exchange with a routing key. 

### Request

URL format of REST request requires AMQP parameters to locate the message source or destination.  

**GET:**

	https://{hostname}/rest/{vhost}/{exchangeName}/{queueName}/{routingkey}

**POST:**

	https://{hostname}/rest/{vhost}/{exchangeName}/{routingkey}

POST request also requires a `Content-Type` header as `application/json` and HTTP body as a JSON object. The body object contains the AMQP message content and properties, e.g. `'{"content": "Hello", "properties": {"contentType": "text/plain", "deliveryMode": 1}}'`. Note that `content` field is mandatory while `properties` is optional.  

Moreover, each request needs to provide username and password in HTTP basic auth. The credentials must be authenticated to access your vhost.  

In case the REST client you use requires the CA certificate to verify [robomq.io](http://www.robomq.io)'s certificate, download it from <http://www.tbs-x509.com/AddTrustExternalCARoot.crt>

### Response

**GET:**  

If the GET request succeeds, the response will be either status code 200 and message in HTTP body, or status code 204 which indicates the target queue is empty. The HTTP body in 200 response is formatted in JSON, for example  

	{
		"fields":{
			"deliveryTag":1,
			"redelivered":false,
			"exchange":"testEx",
			"routingKey":"testKey",
			"messageCount":0
		},
		"properties":
		{
			"contentType":"text/plain",
			"headers":{},
			"deliveryMode":1
		},
		"content":"Hello"
	}

If any of the exchange, queue or binding doesn't exist, server will create it with the default arguments: 

* type (exchange): topic
* durable (exchange & queue): true
* auto-delete (exchange & queue): false
* internal (exchange): false

All errors will be responded with status code and error details in HTTP body.  

**POST:**  

The response for POST request is either 200 OK or error status code and details in HTTP body.  

If the exchange doesn't exist, server will create it with the default arguments: 

* type: topic
* durable: true
* auto-delete: false
* internal: false

### Properties

The optional `properties` field in HTTP body of GET response and POST request should be a JSON object. All available attributes are listed bellow,  

* `mandatory` (boolean): if true, the message will be returned if it is not routed to a queue (i.e., if there are no bindings that match its routing key).

* `immediate` (boolean): in the specification, this instructs the server to return the message if it is not able to be sent immediately to a consumer. No longer implemented in RabbitMQ, and if true, will provoke a channel error, so it's best to leave it out.

* `deliveryMode` (boolean or numeric): Either 1 or falsey, meaning non-persistent; or, 2 or truthy, meaning persistent. That's just obscure though. Use the option persistent instead.

* `persistent` (boolean): If truthy, the message will survive broker restarts provided it's in a queue that also survives restarts. Corresponds to, and overrides, the property deliveryMode.

* `contentType` (string): a MIME type for the message content

* `contentEncoding` (string): a MIME encoding for the message content

* `correlationId` (string): usually used to match replies to requests, or similar

* `replyTo` (string): often used to name a queue to which the receiving application must send replies, in an RPC scenario (many libraries assume this pattern)

* `messageId` (string): arbitrary application-specific identifier for the message

* `expiration` (string): if supplied, the message will be discarded from a queue once it's been there longer than the given number of milliseconds. In the specification this is a string; numbers supplied here will be coerced to strings for transit.

* `timestamp` (positive number): a timestamp for the message

* `CC` (string or array of string): an array of routing keys as strings; messages will be routed to these routing keys in addition to that given as the routingKey parameter. A string will be implicitly treated as an array containing just that string. This will override any value given for CC in the headers parameter. NB The property names CC and BCC are case-sensitive.

* `BCC` (string or array of string): like CC, except that the value will not be sent in the message headers to consumers.

* `userId` (string): If supplied, RabbitMQ will compare it to the username supplied when opening the connection, and reject messages for which it does not match.

* `appId` (string): an arbitrary identifier for the originating application

* `type` (string): an arbitrary application-specific type for the message

* `headers` (object): application specific headers to be carried along with the message content. The value as sent may be augmented by extension-specific fields if they are given in the parameters, for example, 'CC', since these are encoded as message headers; the supplied value won't be mutated

# REST use case

We will provide example of REST client using cURL, but you may use any tool or language to make REST calls.  

The only prerequisite is that you have cURL client installed. cURL comes with most Linux systems. For windows, you may need to download [curl.exe](http://curl.haxx.se/download.html) and place it into your system directory, e.g. `C:\Windows\System32\curl.exe`.  

### GET

	curl -X GET -i https://{username}:{password}@{hostname}/rest/{yourvhost}/testEx/testQ/testKey

### POST

	curl -X POST -i https://{username}:{password}@{hostname}/rest/{yourvhost}/testEx/testKey \
		-H "Content-Type: application/json" \
		-d '{"content": "Hello", "properties": {"contentType": "text/plain", "deliveryMode": 1}}'
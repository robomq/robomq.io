# Introduction

> Browse the chapter of AMQP Introduction first if you're new to AMQP.  

<a href="https://www.robomq.io" target="_blank">RoboMQ</a> innovatively provides <a href="https://www.robomq.io/#thingsconnect" target="_blank">REST interface</a> over the AMQP broker. It's only accessible over HTTPS.  

Our REST interface facilitates using <a href="https://www.robomq.io" target="_blank">RoboMQ</a> from any HTTP client. Therefore, it allows you send and receive messages without installing a message queue client library, writing and running a client program. Some common scenarios of interacting with our REST interface are  

1. Integrate any devices or applications with <a href="https://www.robomq.io" target="_blank">RoboMQ</a> message queue system by making HTTP calls from them.  
2. Send and receive messages through simple HTTP client for easy testing, such as cURL. Get rid of programming.  
3. Send and receive messages from Web browser with a JavaScript HTTP client. No library installation is required.  

# Usage

HTTP GET and POST methods are supported. Each transaction consists of one AMQP message per HTTP request-response.  

1. GET method gets a message from a particular queue, which is bound to an exchange with a routing key.  
2. POST method publishes a message to a particular exchange with a routing key, and finally delivers it to a queue. 

### Request

**URL:**  

URL format of HTTP request requires AMQP parameters to locate the message source or destination.  

```
https://{hostname}/rest/{vhost}/{exchangeName}/{queueName}/{routingkey}
```

**Authentication:**  

There are 2 authentication mechanisms that the REST interface will accept. They are secret token header and HTTP basic auth. You need to apply one of them. If you apply both, the basic auth will be ignored.  
  
1. Secret Token Header: set a HTTP header in request as the credential. You will need to provide <a href="https://www.robomq.io" target="_blank">RoboMQ</a> the header name and value for us to add it into server records.  
2. HTTP Basic Auth: submit your <a href="https://www.robomq.io" target="_blank">RoboMQ</a> username:password via HTTP basic auth.  

**Certificate:**

In case the HTTP client you use requires the CA certificate to verify <a href="https://www.robomq.io" target="_blank">RoboMQ</a>'s certificate, download it from <a href="https://www.tbs-x509.com/AddTrustExternalCARoot.crt" target="_blank">https://www.tbs-x509.com/AddTrustExternalCARoot.crt</a>

**GET:**

GET method requires no additional HTTP header or body in the request.  

**POST:**

You can optionally set a `X-AMQP-Properties` HTTP header in POST request. It will be an object containing key-value pairs of any available AMQP message properties. For example,  

	X-AMQP-Properties: {"contentType": "text/plain", "deliveryMode": 2}

Find more details on AMQP message properties in the _Properties_ section.  

The HTTP request body will be sent as the AMQP message body. Make sure the `Content-Type` header matches the actual MIME type of the body.    

### Response

**GET:**  

If the GET request succeeds, the response will be either status code 200 and the message that is fetched, or status code 204 which indicates the target queue is currently empty.  
The HTTP body in 200 response is the AMQP message body and there are 2 HTTP headers `X-AMQP-Envelop` and `X-AMQP-Properties`, respectively containing the envelop and properties of the AMQP message, for example  

HTTP Headers:  

	X-AMQP-Envelop: {"deliveryTag":1,"redelivered":false,"exchange":"testEx","routingKey":"testKey","messageCount":0}
	X-AMQP-Properties: {"contentType":"text/plain","headers":{},"deliveryMode":2,"correlationId":"0053b20e-a462-435d-8697-cd43fc22c4c7","messageId":"0053b20e-a462-435d-8697-cd43fc22c4c7"}

HTTP Body: 

	Hello World

All errors will be responded with status code and error description in HTTP body.  

**POST:**  

The response for POST request is either 200 OK or error status code and description in HTTP body.  

**Missing resources:**

For both GET and POST methods, if any of the exchange, queue or binding doesn't exist, server will create it with the following default arguments:  

* type (exchange): topic
* durable (exchange & queue): true
* auto-delete (exchange & queue): false
* internal (exchange): false

### Properties

The `X-AMQP-Properties` HTTP header in the response of GET and POST request should be a JSON object. All available properties are listed bellow.  

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

We will provide complete examples of HTTP call to our REST interface using cURL, but you may use any other tool or language to make the calls.  

The only prerequisite is that you have cURL client installed. cURL comes with most Linux systems. For windows, you may need to download [curl.exe](http://curl.haxx.se/download.html) and place it into your system directory, e.g. `C:\Windows\System32\curl.exe`.  

### GET

**Secret Token:**  

```bash
curl -X GET -i https://{hostname}/rest/{yourvhost}/testEx/testQ/testKey \
	-H 'X-Secret-Token: {token}'
```

**Basic Auth:**  

```bash
curl -X GET -i https://{username}:{password}@{hostname}/rest/{yourvhost}/testEx/testQ/testKey
```

### POST

**Secret Token:**  

```bash
curl -X POST -i https://{hostname}/rest/{yourvhost}/testEx/testQ/testKey \
	-H 'X-Secret-Token: {token}' \
	-H 'X-AMQP-Properties: {"contentType": "text/plain", "deliveryMode": 2}' \
	-d 'Hello World'
```

**Basic Auth:**  

```bash
curl -X POST -i https://{hostname}/rest/{yourvhost}/testEx/testQ/testKey \
	-u {username}:{password} \
	-H 'X-AMQP-Properties: {"contentType": "text/plain", "deliveryMode": 2}' \
	-d 'Hello World'
```



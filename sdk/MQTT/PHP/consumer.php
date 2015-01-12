<?php
/**
 * File: consumer.php
 * Description: This is the MQTT consumer handles incoming messages
 *     published by producers from a particular topic.
 *     Consumer prints the topic and payload as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

require(__DIR__."/spMQTT.class.php");
//spMQTTDebug::Enable();

$server = "localhost";
$port = "1883";
$vhost = "/";
$username = "guest";
$password = "guest";
$topic = "test";

$client = new spMQTT("tcp://".$server.":".$port, $clientid=null);	//clientid auto-assigned
$client->setAuth($vhost.":".$username, $password);
$client->setConnectClean(true);
$client->setKeepalive(60);

/**
 * This method is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
 */
function message_callback($client, $topic, $payload) {
    printf("Topic: %s, Message: %s\n", $topic, $payload);
}

if ($client->connect()) {
	try {
		$topics[$topic] = 1;	//$topics['sepcific topic'] = qos of the subscription to that topic
		$client->subscribe($topics);
		//$client->unsubscribe(array_keys($topics));
		$client->loop("message_callback");	//start looping with message_callback
	} catch(Exception $ex) {
		echo "Error: Failed to subscribe".PHP_EOL;	
		exit(-1);
	}
}
else {
	echo "Error: Failed to connect".PHP_EOL;
	exit(-1);
}
?>
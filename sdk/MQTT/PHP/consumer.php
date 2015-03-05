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

$GLOBALS["client"] = $client;
$GLOBALS["topic"] = $topic;

$server = "hostname";
$port = "1883";
$vhost = "yourvhost";
$username = "username";
$password = "password";
$topic = "test/#";

function subscribe() {
	$GLOBALS["client"]->subscribe($GLOBALS["topic"], 1); //qos=1
}

/**
 * This method is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
 */
function onMessage($message) {
    printf("Topic: %s, Message: %s\n", $message->topic, $message->payload);
//	var_dump($message);
}

while (true) {
	try {
		$client = new Mosquitto\Client("1", true); //clientid auto-assigned, clean_session=true
		$client->setCredentials($vhost.":".$username, $password);
		$client->onConnect("subscribe");
		$client->onMessage("onMessage");
		$client->connect($server, $port, 60); //keepalive=60
		$client->loopForever(); //automatically reconnect when loopForever
	} catch (Exception $e) {
		//when initialize connection, reconnect on exception
		echo "Exception handled, reconnecting...\nDetail:\n".$e."\n";
		sleep(5);
	}
}
?>

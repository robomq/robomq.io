<?php
/**
 * File: procuder.php
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */
 
require(__DIR__ . "/spMQTT.class.php");
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

if ($client->connect()) {
	echo "Quantity of test messages: ";
	$msgNum = rtrim(fgets(STDIN), PHP_EOL);
	for ($i = 1; $i <= $msgNum; $i++) {
		//publish test messages to the topic
		$message = "test msg ".$i;
		try {
			$client->publish($topic, $message, $dup=0, $qos=1, $retain=0, $msgid=null);	//msgid auto-assigned
		} catch(Exception $ex) {
			echo "Error: Failed to send message".PHP_EOL;
			exit(-1);
		}
	}
	sleep($msgNum / 1000);	//to allow all the messages get through
	$client->disconnect();
}
else {
	echo "Error: Failed to connect".PHP_EOL;
	exit(-1);
}
?>
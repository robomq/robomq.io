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

$server = "hostname";
$port = "1883";
$vhost = "yourvhost";
$username = "username";
$password = "password";
$topic = "test/any";

try {
	$client = new Mosquitto\Client("2", true); //clientid auto-assigned, clean_session=true
	$client->setCredentials($vhost.":".$username, $password);
	$client->connect($server, $port, 60); //keepalive=60

	echo "Quantity of test messages: ";
	$msgNum = rtrim(fgets(STDIN), PHP_EOL);
	for ($i = 1; $i <= $msgNum; $i++) {
		$message = "test msg ".$i;
		$client->publish($topic, $message, 1, false); //publish test messages to the topic
		$client->loop(); //frequently loop to to keep communications with broker
		sleep(1);
	}
	$client->disconnect();
} catch (Exception $e) {
	echo $e;
}
?>

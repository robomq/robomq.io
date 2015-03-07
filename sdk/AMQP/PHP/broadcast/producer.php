<?php
/**
* File: producer.php
* Description: This is the AMQP producer publishes outgoing AMQP
*     communication to  clients consuming messages from a broker server.
*     Messages can be sent over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified routing key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

require_once __DIR__ . '/../vendor/autoload.php';
use PhpAmqpLib\Connection\AMQPConnection;
use PhpAmqpLib\Message\AMQPMessage;

$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";

try {
	//connect
	$connection = new AMQPConnection($server, $port, $username, $password, $vhost);
	$channel =  $connection->channel();	

	//send message
	//for fanout type exchange, routing key is useless
	$message = new AMQPMessage("Hello World!", array("content_type" => "text/plain", "delivery_mode" => 1));
	$channel->basic_publish($message, $exchangeName, $routing_key = "");

	//disconnect
	$connection->close();
} catch(Exception $e) {
	echo $e.PHP_EOL;
}
?>

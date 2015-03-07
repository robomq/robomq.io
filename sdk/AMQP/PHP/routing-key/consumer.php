<?php
/**
* File: consumer.php
* Description: This is the AMQP consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     Messages can be received over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified binding key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

require_once __DIR__."/../vendor/autoload.php";
use PhpAmqpLib\Connection\AMQPConnection;

$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";
$queueName = "testQ1";
$routingKey = "test";

//callback funtion on receiving messages
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
};

while (true) {
	try {
		//connect
		$connection = new AMQPConnection($server, $port, $username, $password, $vhost);
		$channel = $connection->channel();

		//declare exchange and queue, bind them and consume messages
		$channel->exchange_declare($exchangeName, $type = "direct", false, false, $auto_delete = true);
		$channel->queue_declare($queueName, false, false, $exclusive = true, $auto_delete = true);
		$channel->queue_bind($queueName, $exchangeName, $routingKey);
		$channel->basic_consume($queueName, "", false, $no_ack = true, false, false, $callback = $onMessage);

		//start consuming
		while(count($channel->callbacks)) {
			$channel->wait();
		}
	} catch(Exception $e) {
		//reconnect on exception
		echo "Exception handled, reconnecting...\nDetail:\n".$e.PHP_EOL;
		if ($connection != null) {
			try {
				$connection->close();
			} catch (Exception $e1) {}
		}
		sleep(5);
	}
}
?>

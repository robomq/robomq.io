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

require_once __DIR__."/../vendor/autoload.php"; //directory of library folder
use PhpAmqpLib\Connection\AMQPConnection;

$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$queueName = "testQ";

//callback funtion on receiving messages
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
};

while (true) {
	try {
		//connect
		$connection = new AMQPConnection($server, $port, $username, $password, $vhost, $heartbeat = 60);
		$channel = $connection->channel();

		//declare queue and consume messages
		//one-to-one messaging uses the default exchange, where queue name is the routing key
		$channel->queue_declare($queueName, false, false, false, $auto_delete = true);
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
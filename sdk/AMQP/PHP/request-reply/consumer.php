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
use PhpAmqpLib\Message\AMQPMessage;

$GLOBALS["channel"] = $channel;
$GLOBALS["exchangeName"] = $exchangeName;

$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";
$requestQueue = "requestQ";
$requestKey = "request";

//callback funtion on receiving request messages, reply to the reply_to header
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	try {
		$replyMessage = new AMQPMessage("Reply to ".$message->body, array("content_type" => "text/plain", "delivery_mode" => 1));
		$GLOBALS["channel"]->basic_publish($replyMessage, $GLOBALS["exchangeName"], $message->get("reply_to"));
		$GLOBALS["channel"]->basic_ack($message->delivery_info["delivery_tag"]);
	} catch (Exception $e) {
		$GLOBALS["channel"]->basic_nack($message->delivery_info["delivery_tag"]);
	}
};

while (true) {
	try {
		//connect
		$connection = new AMQPConnection($server, $port, $username, $password, $vhost);
		$channel = $connection->channel();

		//declare exchange and queue, bind them and consume messages
		$channel->exchange_declare($exchangeName, $type = "direct", false, false, $auto_delete = true);
		$channel->queue_declare($requestQueue, false, false, $exclusive = true, $auto_delete = true);
		$channel->queue_bind($requestQueue, $exchangeName, $requestKey);
		$channel->basic_consume($requestQueue, "", false, $no_ack = false, false, false, $callback = $onMessage);

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

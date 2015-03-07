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

$GLOBALS["channel"] = $channel;
$GLOBALS["consumerTag"] = $consumerTag;

$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";
$replyQueue = "replyQ";
$requestKey = "request";
$replyKey = "reply";

//callback funtion on receiving reply messages
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	//stop consuming once receives the reply
	$GLOBALS["channel"]->basic_cancel($GLOBALS["consumerTag"]);
};

try {
	//connect
	$connection = new AMQPConnection($server, $port, $username, $password, $vhost);
	$channel =  $connection->channel();	

	//listen for reply messages
	$channel->queue_declare($replyQueue, false, false, $exclusive = true, $auto_delete = true);
	$channel->queue_bind($replyQueue, $exchangeName, $replyKey);
	$consumerTag = $channel->basic_consume($replyQueue, "", false, $no_ack = true, false, false, $callback = $onMessage);

	//send request message
	$message = new AMQPMessage("Hello World!", array("content_type" => "text/plain", "delivery_mode" => 1, "reply_to" => $replyKey));
	$channel->basic_publish($message, $exchangeName, $requestKey);

	//start consuming
	while(count($channel->callbacks)) {
		$channel->wait();
	}

	//disconnect
	$connection->close();
} catch(Exception $e) {
	echo $e.PHP_EOL;
}
?>

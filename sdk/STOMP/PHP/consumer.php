<?php
/**
 * File: consumer.php
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

$server = "localhost";
$port = "61613";
$vhost = "/";
$login = "guest";
$passcode = "guest";
$destination = "/queue/test";	//There're more options other than /queue/...

try {
	$client = new Stomp("tcp://".$server.":".$port, $login, $passcode, array("host" => $vhost));
} catch(StompException $e) {
	die("Error: Connection failed: ".$e->getMessage());
}

$client->subscribe($destination, array("ack" => "client")); //if "ack"=>"auto", no need to ack in code

while(true) {
	if ($frame = $client->readFrame()) {
		echo $frame->body.PHP_EOL;
		$client->ack($frame);
	}
}

//$client->unsubscribe($destination);
//unset($client);
?>

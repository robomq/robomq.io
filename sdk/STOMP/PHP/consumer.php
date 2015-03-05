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

$server = "hostname";
$port = "61613";
$vhost = "yourvhost";
$login = "username";
$passcode = "password";
$destination = "/queue/test";	//There're more options other than /queue/...

while (true) {
	try {
		$client = new Stomp("tcp://".$server.":".$port, $login, $passcode, array("host" => $vhost));	
		$client->subscribe($destination, array("ack" => "client")); //if "ack"=>"auto", no need to ack in code
		while(true) {
			if ($frame = $client->readFrame()) {
				echo $frame->body.PHP_EOL;
				$client->ack($frame);
			}
		}
	} catch(StompException $e) {
		echo "Exception handled, reconnecting...\nDetail:\n".$e->getMessage().PHP_EOL;
		unset($client);
		sleep(5);
	}
}
?>
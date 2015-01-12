<?php
/**
 * File: procuder.php
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
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

echo "Quantity of test messages: ";
$msgNum = rtrim(fgets(STDIN), PHP_EOL);
for ($i = 1; $i <= $msgNum; $i++) {
	$message = "test msg ".$i;
	$client->send($destination, $message);
}

unset($client);
?>

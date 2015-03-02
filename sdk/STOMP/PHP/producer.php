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

$server = "hostname";
$port = "61613";
$vhost = "yourvhost";
$login = "username";
$passcode = "password";
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
	sleep(1);
}

unset($client);
?>

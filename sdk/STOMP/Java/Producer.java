/**
 * File: Procuder.java
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import net.ser1.stomp.*;
import java.util.Scanner;

class Producer {
	private Client client;
	private String server = "hostname";
	private int port = 61613;
	private String vhost = "yourvhost";
	private String destination = "/queue/test"; //There're more options other than /queue/...
	private String login = "username";
	private String passcode = "password";

	private void produce() {
		try {
			client = new Client(server, port, login, passcode, vhost);
			System.out.print("Quantity of test messages: ");
			Scanner scanner = new Scanner(System.in);
			int msgNum = scanner.nextInt();
			for (int i = 0; i < msgNum; i ++) {
				String message = "test msg " + Integer.toString(i + 1);
				client.send(destination, message, null);
				Thread.sleep(1000);
			}
			client.disconnect();
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);			
		}
	}

	public static void main(String[] args) {
		Producer p = new Producer();
		p.produce();
	}
}

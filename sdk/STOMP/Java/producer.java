/**
 * File: procuder.java
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import net.ser1.stomp.*;
import java.util.Scanner;

class producer {
	private Client client;
	private String server = "hostname";
	private int port = 61613;
	private String vhost = "yourvhost";
	private String destination = "/queue/test"; //There're more options other than /queue/...
	private String login = "username";
	private String passcode = "password";


	/**
	 * This method connects client to the broker.
	 * @ exception on connection error.
	 */
	private void connect() {
		try {
			client = new Client(server, port, login, passcode, vhost);
		} catch(Exception e) {
			System.out.println("Error: Can't initialize connection");
			System.exit(-1);
		}		
	}

	/**
	 * This method publishes a certain number of messages to the specified destination.
	 * @ param n is the number of messages to publish.
	 * @ exception on publish error.
	 */
	private void send(int n) {
		for (int i = 0; i < n; i ++) {
			try {
				String message = "test msg " + Integer.toString(i + 1);
				client.send(destination, message);
			} catch(Exception e) {
				System.out.println("Error: Can't send message");
				System.exit(-1);			
			}	
		}
	}

	/**
	 * This method disconnect client from the broker.
	 * @ exception on disconnection error.
	 */
	private void disconnect() {
		try {
			client.disconnect();
		} catch(Exception e) {
			System.out.println("Error: Can't disconnect");
			System.exit(-1);			
		}
	}

	/**
	 * This is the main method which creates and runs producer instance.
	*/
	public static void main(String[] args) {
		producer p = new producer();
		p.connect();
		System.out.print("Quantity of test messages: ");
		Scanner scanner = new Scanner(System.in);
		int msgNum = scanner.nextInt();
		p.send(msgNum);
		p.disconnect();
	}
}

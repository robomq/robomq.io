/**
 * File: consumer.java
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import net.ser1.stomp.*;
import java.util.Map;

class consumer {
	private Client client;
	private String server = "localhost";
	private int port = 61613;
	private String vhost = "/";
	private String destination = "/queue/test";	//There're more options other than /queue/...
	private String login = "guest";
	private String passcode = "guest";

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
	 * This method sucscribes the topic.
	 * @ exception on subscription error.
	 */
	private void subscribe() {
		try {
			client.subscribe(destination, new Listener() {
				/**
				 * This method is the overrided callback on receiving messages.
				 * @ It is event-driven. You don't call it in your code.
				 * @ It prints the message body on console.
				 * @ There're other callback functions provided by this library.
				 */
				public void message( Map headers, String body ) {
					System.out.println(body);
				}
  			}); 
		} catch(Exception e) {
			System.out.println("Error: Can't subscribe queue");
			System.exit(-1);		
		}	
	}

	/**
	 * This method unsucscribes the topic.
	 * @ exception on unsubscription error.
	 */
	private void unsubscribe() {
		try {
			client.unsubscribe(destination);
		} catch(Exception e) {
			System.out.println("Error: Can't unsubscribe");
			System.exit(-1);			
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
	 * This is the main method which creates and runs consumer instance.
	*/
	public static void main(String[] args) {
		consumer c = new consumer();
		c.connect();
		c.subscribe();
		//c.unsubscribe();
		//c.disconnect();
	}
}

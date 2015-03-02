/**
 * File: Consumer.java
 * Description: This is the STOMP consumer handles incoming messages
 *     published by producers from a particular queue.
 *     Consumer prints the message body as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import net.ser1.stomp.*;
import java.util.Map;

class Consumer {
	private Client client;
	private String server = "hostname";
	private int port = 61613;
	private String vhost = "yourvhost";
	private String destination = "/queue/test";	//There're more options other than /queue/...
	private String login = "username";
	private String passcode = "password";

	private void consume() {
		while (true) {
			try {
				client = new Client(server, port, login, passcode, vhost);
				client.subscribe(destination, new Listener() {
					/**
					 * This method is the overrided callback on receiving messages.
					 * @ It is event-driven. You don't call it in your code.
					 * @ It prints the message body on console.
					 * @ There're other callback functions provided by this library.
					 */
					public void message(Map headers, String body) {
						System.out.println(body);
					}
	  			});
				client.addErrorListener(new Listener() {
					public void message(Map header, String body) {
						System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", body);
						try {
							client.disconnect();
						} catch(Exception e) {}
						consume(); //reconnect on exception
					}
				});
				break;
			} catch(Exception e) {
				//reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", e); 
				try {
					Thread.sleep(5000); 
				} catch(Exception es) {}
			}
		}	
	}

	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}

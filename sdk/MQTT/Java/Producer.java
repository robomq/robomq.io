/**
 * File: Procuder.java
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Scanner;

public class Producer {

	private MqttClient client;
	private String server = "hostname";
	private String port = "1883";
	private String broker = "tcp://" + server + ":" + port;
	private String vhost = "yourvhost";
	private String username = "username";
	private String password = "password";
	private String topic = "test/any";
	private String clientId = MqttClient.generateClientId();
	private MemoryPersistence persistence = new MemoryPersistence();

	/**
	 * This method connects client to the broker.
	 * @ exception on connection error.
	 */
	private void connect() {
		try {
			client = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName(vhost + ":" + username);
			connOpts.setPassword(password.toCharArray());
			connOpts.setKeepAliveInterval(60);
			connOpts.setCleanSession(true);
			client.connect(connOpts);
		} catch(MqttException me) {
			System.out.println("Error: "+me);
			System.exit(-1);
		}
	}

	/**
	 * This method publishes a certain number of messages to the specified topic.
	 * @ param n is the number of messages to publish.
	 * @ exception on publish error.
	 */
	private void publish(int n) {
		for (int i = 0; i < n; i ++) {
			MqttMessage message = new MqttMessage(("test msg " + Integer.toString(i + 1)).getBytes());
			message.setQos(1);
			message.setRetained(false);
			try {
				client.publish(topic, message);
			} catch(MqttException me) {
				System.out.println("Error: "+me);
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
			System.exit(0);
		} catch(MqttException me) {
			System.out.println("Error: "+me);
			System.exit(-1);			
		}
	}

	/**
	 * This is the main method which creates and runs producer instance.
	*/
	public static void main(String[] args) {
		Producer p = new Producer();
		p.connect();
		//ask user to input number of test messages
		System.out.print("Quantity of test messages: ");
		Scanner scanner = new Scanner(System.in);
		int msgNum = scanner.nextInt();
		p.publish(msgNum);
		p.disconnect();
	}
}

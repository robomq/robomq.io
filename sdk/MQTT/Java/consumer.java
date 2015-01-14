/**
 * File: consumer.java
 * Description: This is the MQTT consumer handles incoming messages
 *     published by producers from a particular topic.
 *     Consumer prints the topic and payload as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

public class consumer {

	private MqttClient client;
	private String server = "hostname";
	private String port = "1883";
	private String broker = "tcp://" + server + ":" + port;
	private String vhost = "yourvhost";
	private String username = "username";
	private String password = "password";
	private String topic = "test";
	private String clientId = MqttClient.generateClientId();
	private MemoryPersistence persistence = new MemoryPersistence();

	/**
	 * This method is the overrided callback on receiving messages.
	 * @ It is event-driven. You don't call it in your code.
	 * @ It prints the message topic and payload on console.
	 * @ There're other callback functions provided by this library.
	 */
	private class onMessage implements MqttCallback {
	
		public void messageArrived(String topic, MqttMessage message) {		
			System.out.println("Topic: " + topic + ", Message: " + (new String(message.getPayload())));
		}

		public void connectionLost(Throwable cause) {

		}

		public void deliveryComplete(IMqttDeliveryToken token) {

		}
	}

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
	 * This method sucscribes the topic.
	 * @ exception on subscription error.
	 */
	private void subscribe() {
		try {
			onMessage callback = new onMessage();
			client.setCallback(callback);
			client.subscribe(topic, 1);	//qos=1
		} catch(MqttException me) {
			System.out.println("Error: "+me);
			System.exit(-1);		
		}
	}

	/**
	 * This method unsucscribes the topic.
	 * @ exception on unsubscription error.
	 */
	private void unsubscribe() {
		try {
			client.unsubscribe(topic);
		} catch(MqttException me) {
			System.out.println("Error: "+me);
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
			System.exit(0);
		} catch(MqttException me) {
			System.out.println("Error: "+me);
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

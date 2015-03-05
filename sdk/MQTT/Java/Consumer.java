/**
 * File: Consumer.java
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

public class Consumer {

	private MqttClient client;
	private String server = "hostname";
	private String port = "1883";
	private String broker = "tcp://" + server + ":" + port;
	private String vhost = "yourvhost";
	private String username = "username";
	private String password = "password";
	private String topic = "test/#";
	private String clientId = MqttClient.generateClientId();
	private MemoryPersistence persistence = new MemoryPersistence();
	private boolean connected = false;

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
			System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", cause.getMessage());
			connected = false; //reconnect on exception
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
		}
	}

	private void consume() {
		while (true) {
			try {
				client = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setUserName(vhost + ":" + username);
				connOpts.setPassword(password.toCharArray());
				connOpts.setKeepAliveInterval(60);
				connOpts.setCleanSession(true);
				client.connect(connOpts);
				onMessage callback = new onMessage();
				client.setCallback(callback);
				client.subscribe(topic, 1);	//qos=1
				connected = true;
				while (connected) { //check connection status
					try {
						Thread.sleep(5000);
					} catch (Exception e) {}
				} 
			} catch(MqttException me) {
				//reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", me); 
				try {
					Thread.sleep(5000); 
				} catch(Exception e) {}
			}
		}
	}

	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}

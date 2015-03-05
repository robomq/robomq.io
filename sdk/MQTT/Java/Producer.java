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

	private void produce() {
		try {
			client = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName(vhost + ":" + username);
			connOpts.setPassword(password.toCharArray());
			connOpts.setKeepAliveInterval(60);
			connOpts.setCleanSession(true);
			client.connect(connOpts);
			System.out.print("Quantity of test messages: ");
			Scanner scanner = new Scanner(System.in);
			int msgNum = scanner.nextInt();
			for (int i = 0; i < msgNum; i ++) {
				MqttMessage message = new MqttMessage(("test msg " + Integer.toString(i + 1)).getBytes());
				message.setQos(1);
				message.setRetained(false);
				client.publish(topic, message);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
			}
			client.disconnect();
		} catch(MqttException me) {
			System.out.println(me);
			System.exit(-1);			
		}
	}

	public static void main(String[] args) {
		Producer p = new Producer();
		p.produce();
	}
}

/**
 * File: procuder.cpp
 * Description: This is the MQTT producer publishes a certain number
 *     of test messages to a particular topic through MQTT broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

#include <stdio.h>
#include <iostream>
#include <unistd.h>
#include <mosquitto.h>
#include <exception>
#include <stdlib.h>

using namespace std;

/**
 * This method is the callback on writing log. 
 * @It is event-driven. You don't call it in your code.
 * @It prints the log information on console.
 * @There're other callback functions provided by this library.
 */
void my_log_callback(struct mosquitto *mosq, void *userdata, int level, const char *str)
{
	cout << str << endl;
}

/**
 * This is the main method which creates and runs producer instance.
 * @Looping is essential for this MQTT library to work.
 * @Exceptions on connection and publish error.
 */
int main(int argc, char *argv[])
{
	//configuration
	string hst = "hostname";
	const char *host = hst.c_str();
	int port = 1883;
	string vhost = "yourvhost";
	string usn = "username";
	string vhusn = vhost + ":" + usn;
	const char *username = vhusn.c_str();
	string pwd = "password";
	const char *password = pwd.c_str();
	string tpc = "test/any";
	const char *topic = tpc.c_str();
	int keepalive = 60;
	bool clean_session = true;
	struct mosquitto *mosq = NULL;

	//create producer and connect to broker
	mosquitto_lib_init();
	mosq = mosquitto_new(NULL, clean_session, NULL);
	mosquitto_username_pw_set(mosq,	username, password);	 
	//mosquitto_log_callback_set(mosq, my_log_callback);
	if(mosquitto_connect(mosq, host, port, keepalive)){
		cout << "Error: Failed to connect" << endl;
		return 1;
	}
	//usually start loop right after connecting
	mosquitto_loop_start(mosq); 

	//send certain number of test messages
	int msgNum;
	cout << "Quantity of test messages: ";
	cin >> msgNum;
	char payload[20];
	for (int i = 1; i <= msgNum; i++) {
		sprintf(payload, "test msg %d", i);
		try {
			mosquitto_publish(mosq, NULL, topic, 20, payload, 1, false);
		} catch(exception& e) {
			cout << "Error: Failed to publish message\n" << e.what() << endl;
			return 1;
		}
		sleep(1);
	}

	//stop producer
	mosquitto_loop_stop(mosq, true); 
	mosquitto_disconnect(mosq);
	mosquitto_destroy(mosq);
	mosquitto_lib_cleanup();
	return 0;
}

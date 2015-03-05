/**
 * File: consumer.cpp
 * Description: This is the MQTT consumer handles incoming messages
 *     published by producers from a particular topic.
 *     Consumer prints the topic and payload as it receives messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

#include <stdio.h>
#include <iostream>
#include <mosquitto.h>
#include <exception>
#include <stdlib.h>
#include <unistd.h>

using namespace std;

//The library automatically reconnects to broker

string hst = "hostname";
const char *host = hst.c_str();
int port = 1883;
string vhost = "yourvhost";
string usn = "username";
string vhusn = vhost + ":" + usn;
const char *username = vhusn.c_str();
string pwd = "passwrod";
const char *password = pwd.c_str();
string tpc = "test/#";
const char *topic = tpc.c_str();

/**
 * This method is the callback on connecting broker. 
 * @It is event-driven. You don't call it in your code.
 * @It subscribes the specific topic.
 * @There're other callback functions provided by this library.
 */
void onConnect(struct mosquitto *mosq, void *userdata, int result) {
	if (!result) {
		try {
			mosquitto_subscribe(mosq, NULL, topic, 1); 
		} catch (exception& e) {
			printf("Error: Failed to subscribe\n%s\n", e.what());
		}
	} else {
		printf("Error: Failed to connect\n");
	}
}

/**
 * This method is the callback on receiving messages. 
 * @It is event-driven. You don't call it in your code.
 * @It prints the message topic and payload on console.
 * @There're other callback functions provided by this library.
 */
void onMessage(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *message) {
	if(message->payloadlen) {
		printf("Topic: %s, Message: %s\n", (char*)message->topic, (char*)message->payload);
	} else {
		printf("Topic: %s, Message: (null)\n", message->topic);
	}
	fflush(stdout);
}

/**
 * This is the main method which creates and sets consumer instance.
 * @Looping is essential for this MQTT library to work.
 * @Exceptions on connection and subscription error.
 */
int main(int argc, char *argv[]) {
	int keepalive = 60;
	bool clean_session = true;
	struct mosquitto *mosq = NULL;
	mosquitto_lib_init();
	mosq = mosquitto_new(NULL, clean_session, NULL);
	mosquitto_username_pw_set(mosq,	username, password);	 
	mosquitto_connect_callback_set(mosq, onConnect);
	mosquitto_message_callback_set(mosq, onMessage);
	mosquitto_connect(mosq, host, port, keepalive);
	//looping is essential for consumer to work
	while(!mosquitto_loop_forever(mosq, 0, 1)){
	}
	return 0;
}
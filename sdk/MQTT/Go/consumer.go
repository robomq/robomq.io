package main

import (
	"fmt"
	MQTT "github.com/eclipse/paho.mqtt.golang"
	"time"
)

var server = "hostname"
var port = 1883
var vhost = "yourvhost"
var username = "username"
var password = "password"
var topic = "test/#"

/**
 * This function is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
 */
var OnMessage MQTT.MessageHandler = func(client MQTT.Client, msg MQTT.Message) {
	fmt.Printf("Topic: %s, Message: %s\n", msg.Topic(), msg.Payload())
}

func main() {
	connOpts := MQTT.NewClientOptions().AddBroker(fmt.Sprintf("tcp://%s:%d", server, port))
	connOpts.SetUsername(fmt.Sprintf("%s:%s", vhost, username))
	connOpts.SetPassword(password)
	connOpts.SetClientID("0")
	connOpts.SetCleanSession(true)
	connOpts.SetKeepAlive(60 * time.Second)
	connOpts.SetAutoReconnect(false)

	// Infinite loop to auto-reconnect on failure
	for {
		// Create and start a client using the above ClientOptions
		client := MQTT.NewClient(connOpts)
		if token := client.Connect(); token.Wait() && token.Error() != nil {
			fmt.Printf("Failed to connect, err: %v\n", token.Error())
		}

		// QoS = 1
		if token := client.Subscribe(topic, 1, OnMessage); token.Wait() && token.Error() != nil {
			fmt.Printf("Failed to subscribe, err: %v\n", token.Error())
		}

		// Constantly checking connectivity
		for client.IsConnected() {
			time.Sleep(time.Second)
		}

		fmt.Println("Restarting in 5 seconds...")
		time.Sleep(5 * time.Second)
	}
}
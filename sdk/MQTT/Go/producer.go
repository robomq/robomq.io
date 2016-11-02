package main

import (
	"fmt"
	MQTT "github.com/eclipse/paho.mqtt.golang"
	"os"
	"time"
)

var server = "hostname"
var port = 1883
var vhost = "yourvhost"
var username = "username"
var password = "password"
var topic = "test/any"

func main() {
	connOpts := MQTT.NewClientOptions().AddBroker(fmt.Sprintf("tcp://%s:%d", server, port))
	connOpts.SetUsername(fmt.Sprintf("%s:%s", vhost, username))
	connOpts.SetPassword(password)
	connOpts.SetClientID("1")
	connOpts.SetCleanSession(true)
	connOpts.SetKeepAlive(60 * time.Second)
	connOpts.SetAutoReconnect(false)

	// Create and start a client using the above ClientOptions
	client := MQTT.NewClient(connOpts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		fmt.Printf("Failed to connect, err: %v\n", token.Error())
		os.Exit(1)
	}

	var msgNum int
	fmt.Print("Quantity of test messages: ")
	fmt.Scanf("%d", &msgNum)
	for i := 0; i < msgNum; i++ {
		message := fmt.Sprintf("test msg %d", i+1)
		// QoS = 1, retained = false
		token := client.Publish(topic, 1, false, message)
		// Use PublishToken to confirmed receipt from the broker
		token.Wait()
		if token.Error() != nil {
			fmt.Printf("Failed to publish, err: %v\n", token.Error())
			os.Exit(1)
		}
		time.Sleep(time.Second)
	}

	client.Disconnect(250)
}
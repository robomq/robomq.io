/**
* File: producer.go
* Description: This is the AMQP producer publishes outgoing AMQP
*     communication to  clients consuming messages from a broker server.
*     Messages can be sent over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified routing key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

package main

import (
	"fmt"
	"github.com/streadway/amqp"
	"os"
	"time"
)

var server = "hostname"
var port = 5672
var vhost = "yourvhost"
var username = "username"
var password = "password"
var routingKey = "testQ"

func main() {
	connection, err := amqp.DialConfig(fmt.Sprintf("amqp://%s:%s@%s:%d/%s", username, password, server, port, vhost),
		amqp.Config{Heartbeat: 60 * time.Second})
	if err != nil {
		fmt.Printf("Failed to connect, err: %v\n", err)
		os.Exit(1)
	}
	defer connection.Close()

	channel, err := connection.Channel()
	if err != nil {
		fmt.Printf("Failed to create channel, err: %v\n", err)
		os.Exit(1)
	}
	defer channel.Close()

	err = channel.Publish(
		// assigning blank string to exchange is to use the default exchange, where queue name is the routing key
		"",         // exchange
		routingKey, // routing key
		false,      // mandatory
		false,      // immediate
		amqp.Publishing{
			ContentType:  "text/plain",
			DeliveryMode: 1,
			Body:         []byte("Hello World!"),
		})
	if err != nil {
		fmt.Printf("Failed to publish message, err: %v\n", err)
		os.Exit(1)
	}
}
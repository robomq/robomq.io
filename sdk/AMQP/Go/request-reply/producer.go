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
var exchangeName = "testEx"
var replyQueue = "replyQ"
var requestKey = "request"
var replyKey = "reply"

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

	queue, err := channel.QueueDeclare(
		replyQueue, // name
		false,      // durable
		true,       // auto-delete
		true,       // exclusive
		false,      // no-wait
		nil,        // args
	)
	if err != nil {
		fmt.Printf("Failed to declare reply queue, err: %v\n", err)
		os.Exit(1)
	}

	err = channel.QueueBind(
		replyQueue,   // queue
		replyKey,     // key
		exchangeName, // exchange
		false,        // no-wait
		nil,          // args
	)
	if err != nil {
		fmt.Printf("Failed to bind reply queue with exchange, err: %v\n", err)
		os.Exit(1)
	}

	messageChan, err := channel.Consume(
		queue.Name,      // queue
		"replyConsumer", // consumer tag
		true,            // auto-ack
		true,            // exclusive
		false,           // no-local
		false,           // no-wait
		nil,             // args
	)
	if err != nil {
		fmt.Printf("Failed to consume reply messages, err: %v\n", err)
		os.Exit(1)
	}

	// use a channel to communicate between goroutines
	gotReply := make(chan bool)

	// listen for reply message
	go func(messageChan <-chan amqp.Delivery, gotReply chan bool) {
		message := <-messageChan
		fmt.Println(string(message.Body))

		// notify main goroutine it has got the reply
		gotReply <- true
	}(messageChan, gotReply)

	err = channel.Publish(
		exchangeName, // exchange
		requestKey,   // routing key
		false,        // mandatory
		false,        // immediate
		amqp.Publishing{
			ContentType:  "text/plain",
			DeliveryMode: 1,
			ReplyTo:      replyKey,
			Body:         []byte("Hello World!"),
		})
	if err != nil {
		fmt.Printf("Failed to publish request message, err: %v\n", err)
		os.Exit(1)
	}

	// block until it has got the reply
	_ = <-gotReply
}
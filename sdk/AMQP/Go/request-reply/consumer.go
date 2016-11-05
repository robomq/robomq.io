/**
* File: consumer.go
* Description: This is the AMQP consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     Messages can be received over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified binding key.
*
* Author: Eamin Zhang
* robomq.io (http://www.robomq.io)
*/

package main

import (
	"fmt"
	"github.com/streadway/amqp"
	"time"
)

var server = "hostname"
var port = 5672
var vhost = "yourvhost"
var username = "username"
var password = "password"
var exchangeName = "testEx"
var requestQueue = "requestQ"
var requestKey = "request"

func main() {
	// Infinite loop to auto-reconnect on failure
Loop:
	for {
		fmt.Println("Starting in 5 seconds...")
		time.Sleep(5 * time.Second)

		connection, err := amqp.DialConfig(fmt.Sprintf("amqp://%s:%s@%s:%d/%s", username, password, server, port, vhost),
			amqp.Config{Heartbeat: 60 * time.Second})
		if err != nil {
			fmt.Printf("Failed to connect, err: %v\n", err)
			continue Loop
		}
		defer connection.Close()

		channel, err := connection.Channel()
		if err != nil {
			fmt.Printf("Failed to create channel, err: %v\n", err)
			continue Loop
		}
		defer channel.Close()

		err = channel.ExchangeDeclare(
			exchangeName, // name
			"direct",     // type
			false,        // durable
			true,         // audo-delete
			false,        // internal
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to declare exchange, err: %v\n", err)
			continue Loop
		}

		queue, err := channel.QueueDeclare(
			requestQueue, // name
			false,        // durable
			true,         // auto-delete
			true,         // exclusive
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to declare request queue, err: %v\n", err)
			continue Loop
		}

		err = channel.QueueBind(
			requestQueue, // queue
			requestKey,   // key
			exchangeName, // exchange
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to bind request queue with exchange, err: %v\n", err)
			continue Loop
		}

		messageChan, err := channel.Consume(
			queue.Name,        // queue
			"requestConsumer", // consumer tag
			false,             // auto-ack
			true,              // exclusive
			false,             // no-local
			false,             // no-wait
			nil,               // args
		)
		if err != nil {
			fmt.Printf("Failed to consume request messages, err: %v\n", err)
			continue Loop
		}

		fmt.Println("Started consuming messages.")
		for message := range messageChan {
			fmt.Println(string(message.Body))

			// on receiving request messages, reply to the reply_to header
			err = channel.Publish(
				exchangeName,    // exchange
				message.ReplyTo, // routing key
				false,           // mandatory
				false,           // immediate
				amqp.Publishing{
					ContentType:  "text/plain",
					DeliveryMode: 1,
					Body:         append([]byte("Reply to "), message.Body...),
				})
			if err != nil {
				fmt.Printf("Failed to publish reply message, err: %v\n", err)
				err = message.Nack(
					false, // multiple
					true,  // requeued
				)
				if err != nil {
					fmt.Printf("Failed to NACK request message, err: %v\n", err)
					break
				}
			} else {
				err = message.Ack(
					false, // multiple
				)
				if err != nil {
					fmt.Printf("Failed to ACK request message, err: %v\n", err)
					break
				}
			}
		}
	}
}
package main

import (
	"fmt"
	"github.com/go-stomp/stomp"
	"net"
	"time"
)

var server = "hostname"
var port = "61613"
var vhost = "yourvhost"
var login = "username"
var passcode = "password"
var destination = "/queue/test" // There're more options other than /queue/...

func main() {
	// Infinite loop to auto-reconnect on failure
Loop:
	for {
		fmt.Println("Starting in 5 seconds...")
		time.Sleep(5 * time.Second)

		// Connect to broker
		client, err := stomp.Dial("tcp", net.JoinHostPort(server, port),
			stomp.ConnOpt.Login(login, passcode),
			stomp.ConnOpt.Host(vhost),
			stomp.ConnOpt.AcceptVersion(stomp.V12),
			stomp.ConnOpt.HeartBeat(60*time.Second, 0*time.Second))
		if err != nil {
			fmt.Printf("Failed to connect, err: %v\n", err)
			continue Loop
		}
		// Subscribe to queue with client acknowledgement
		sub, err := client.Subscribe(destination, stomp.AckClient)
		if err != nil {
			fmt.Printf("Failed to subscribe, err: %v\n", err)
			continue Loop
		}
		for {
			msg := <-sub.C
			if msg.Err != nil {
				fmt.Printf("Can't handle message received, NACKing... Error: %v\n", msg.Err)
				// Unacknowledge the message
				err = client.Nack(msg)
				if err != nil {
					fmt.Printf("Failed to NACK, err: %v\n", err)
					break
				}
			}

			fmt.Println(string(msg.Body))
			// Acknowledge the message
			err = client.Ack(msg)
			if err != nil {
				fmt.Printf("Failed to ACK, err: %v\n", err)
				break
			}
		}
	}
}
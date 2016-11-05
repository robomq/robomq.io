/**
 * File: procuder.go
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

package main

import (
	"fmt"
	"github.com/go-stomp/stomp"
	"net"
	"os"
	"time"
)

var server = "hostname"
var port = "61613"
var vhost = "yourvhost"
var login = "username"
var passcode = "password"
var destination = "/queue/test" // There're more options other than /queue/...

func main() {
	// Connect to broker
	client, err := stomp.Dial("tcp", net.JoinHostPort(server, port),
		stomp.ConnOpt.Login(login, passcode),
		stomp.ConnOpt.Host(vhost),
		stomp.ConnOpt.AcceptVersion(stomp.V12),
		stomp.ConnOpt.HeartBeat(60*time.Second, 0*time.Second))
	if err != nil {
		fmt.Printf("Failed to connect, err: %v\n", err)
		os.Exit(1)
	}

	var msgNum int
	fmt.Print("Quantity of test messages: ")
	fmt.Scanf("%d", &msgNum)
	for i := 0; i < msgNum; i++ {
		message := fmt.Sprintf("test msg %d", i+1)
		err = client.Send(destination, "text/plain", []byte(message), stomp.SendOpt.Header("key", "value"))
		if err != nil {
			fmt.Printf("Failed to publish, err: %v\n", err)
			os.Exit(1)
		}
		time.Sleep(time.Second)
	}

	client.Disconnect()
}
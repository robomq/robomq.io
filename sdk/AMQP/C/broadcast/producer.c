/**
 * File: producer.c
 * Description: This is the AMQP sender class publishes outgoing AMQP
 *     communication to  clients consuming messages from a broker server.
 *     Messages can be sent over AMQP exchange types including one-to-one,
 *     from broadcast pattern, or selectively using specified routing key.
 *
 * Author: fyatzeck
 * Robomq.io (http://www.robomq.io)
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <amqp_tcp_socket.h>
#include <amqp.h>
#include <amqp_framing.h>

amqp_connection_state_t mqconnect() {

    amqp_connection_state_t conn = amqp_new_connection();
    amqp_socket_t *socket = NULL;
	char hostname[] = "localhost"; // robomq.io hostname
	int port = 5672; //default
	char user[] = "guest"; // robomq.io username
	char password[] = "guest"; // robomq.io password
	char vhost[] = "/"; // robomq.io account vhost
    amqp_channel_t channel = 1;
    int channel_max = 0;
    int frame_max = 131072;
    int heartbeat = 60;
    int status = 0;

    // Opening socket
    socket = amqp_tcp_socket_new(conn);

    status = amqp_socket_open(socket, hostname, port);
    if (status) {
        printf("Error opening TCP socket, status = %d, exiting.", status);
    }

    amqp_login(conn, vhost, channel_max, frame_max, heartbeat, AMQP_SASL_METHOD_PLAIN, user, password);
    amqp_channel_open(conn, channel);

    return conn;
}

int main(int argc, char const *const *argv)
{
    amqp_connection_state_t conn;
    amqp_channel_t channel = 1;
    amqp_basic_properties_t props;
    props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
    props.content_type = amqp_cstring_bytes("text/plain");
    props.delivery_mode = 1; /* non-persistent delivery mode */
    amqp_boolean_t mandatory = 0;
    amqp_boolean_t immediate = 0;
    char exchange_name[] = "fanout-exchange";
    char routing_key[] = "";
    char *msg_body = "Hello\n";
    int result;

    conn = mqconnect();

    // Sending message
    result = amqp_basic_publish(conn,
            channel,
            amqp_cstring_bytes(exchange_name),
            amqp_cstring_bytes(routing_key),
            mandatory,
            immediate,
            &props,
            amqp_cstring_bytes(msg_body));

    // Closing connection
    amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
    amqp_destroy_connection(conn);

    return 0;
}
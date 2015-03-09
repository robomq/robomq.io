/**
 * File: consumer.c
 * Description: This is the AMQP listener class handles incoming
 *     communication from clients publishing messages to a broker server.
 *     Messages can be received over AMQP exchange types including one-to-one,
 *     from broadcast pattern, or selectively using specified binding key.
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
	amqp_rpc_reply_t reply;
	int channel_max = 0;
	int frame_max = 131072;
	int heartbeat = 0;
	int status = 0;

	// Opening socket
	socket = amqp_tcp_socket_new(conn);

	status = amqp_socket_open(socket, hostname, port);
	if (status) {
		printf("Error opening TCP socket, status = %d\n", status);
	}

	reply = amqp_login(conn, vhost, channel_max, frame_max, heartbeat, AMQP_SASL_METHOD_PLAIN, user, password);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		fprintf(stderr, "%s: server connection reply code: %d\n",
				"Error logging in", reply.reply_type);
	}

	amqp_channel_open(conn, channel);

	return conn;
}

amqp_bytes_t mqdeclare(amqp_connection_state_t conn, const char *exchange_name, const char *queue_name) {
	amqp_bytes_t queue;
	amqp_channel_t channel = 1;
	amqp_boolean_t passive = 0;
	amqp_boolean_t durable = 0;
	amqp_boolean_t exclusive = 0;
	amqp_boolean_t auto_delete = 1;
	amqp_boolean_t internal = 0;
	char exchange_type[] = "direct";
	char binding_key[] = "hola";
	amqp_rpc_reply_t reply;

	// Declaring exchange
	amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
			passive, durable, auto_delete, internal, amqp_empty_table);

	reply = amqp_get_rpc_reply(conn);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
		if(NULL != m) {
			fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
					"Error declaring exchange",
					m->reply_code,
					(int) m->reply_text.len, (char *) m->reply_text.bytes);
		}
	}

	// Declaring queue
	amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
			passive, durable, exclusive, auto_delete, amqp_empty_table);

	reply = amqp_get_rpc_reply(conn);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		fprintf(stderr, "%s: server connection reply code: %d\n",
				"Error declaring queue", reply.reply_type);
	}
	else {
		queue = amqp_bytes_malloc_dup(r->queue);

		// Binding to queue
		amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
				amqp_empty_table);
	}

	return queue;
}

int main(int argc, char const *const *argv)
{
	amqp_connection_state_t conn;
	amqp_bytes_t queue;
	amqp_channel_t channel = 1;
	amqp_boolean_t no_local = 0;
	amqp_boolean_t no_ack = 1;
	amqp_boolean_t exclusive = 0;
	char exchange_name[] = "hello-exchange";
	char queue_name[] = "hello-queue";
	int retry_time = 5; // retry time in seconds

	conn = mqconnect();
	queue = mqdeclare(conn, &exchange_name[0], &queue_name[0]);

	// Consuming the message
	amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);

	while (1) {
		amqp_rpc_reply_t result;
		amqp_envelope_t envelope;

		amqp_maybe_release_buffers(conn);
		result = amqp_consume_message(conn, &envelope, NULL, 0);

		if (AMQP_RESPONSE_NORMAL != result.reply_type) {
			printf("Consumer AMQP failure occurred, response code = %d, retrying in %d seconds...\n",
					result.reply_type, retry_time);

			// Closing current connection before reconnecting
			amqp_connection_close(conn, AMQP_CONNECTION_FORCED);
			amqp_destroy_connection(conn);

			// Reconnecting on exception
			conn = mqconnect();
			queue = mqdeclare(conn, &exchange_name[0], &queue_name[0]);
			amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
			sleep(retry_time);
		}
		else {
			printf("Received message size: %d\nbody: %s\n", (int)envelope.message.body.len, (char *)envelope.message.body.bytes);

			amqp_destroy_envelope(&envelope);
		}
	}

	return 0;
}
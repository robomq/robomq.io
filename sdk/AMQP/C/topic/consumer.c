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
    char hostname[] = "hostname"; // robomq.io hostname
    int port = 5672; //default
    char user[] = "username"; // robomq.io username
    char password[] = "password"; // robomq.io password
    char vhost[] = "vhost"; // robomq.io account vhost
    amqp_channel_t channel = 1;
    int channel_max = 0;
    int frame_max = 131072;
    int heartbeat = 0;
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

amqp_bytes_t mqdeclare(amqp_connection_state_t conn) {
    amqp_bytes_t queue;
    amqp_channel_t channel = 1;
    amqp_boolean_t passive = 0;
    amqp_boolean_t durable = 0;
    amqp_boolean_t exclusive = 0;
    amqp_boolean_t auto_delete = 1;
    char exchange_name[] = "hello-exchange";
    char exchange_type[] = "topic";
    char queue_name[] = "hello-queue";
    char binding_key[] = "mytopic.new";
    amqp_rpc_reply_t reply;

    // Declaring exchange
    amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
            passive, durable, amqp_empty_table);

    reply = amqp_get_rpc_reply(conn);
    if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
        amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
        fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
                "Error declaring exchange",
                m->reply_code,
                (int) m->reply_text.len, (char *) m->reply_text.bytes);
        exit(1);
    }

    // Declaring queue
    amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
            passive, durable, exclusive, auto_delete, amqp_empty_table);

    reply = amqp_get_rpc_reply(conn);
    if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
        amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
                fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
                        "Error declaring queue",
                        m->reply_code,
                        (int) m->reply_text.len, (char *) m->reply_text.bytes);
        exit(1);
    }
    queue = amqp_bytes_malloc_dup(r->queue);

    // Binding to queue
    amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
            amqp_empty_table);

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
    amqp_frame_t frame;

    conn = mqconnect();
    queue = mqdeclare(conn);

    // Consuming the message
    amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);

    while (1) {
        amqp_rpc_reply_t result;
        amqp_envelope_t envelope;

        amqp_maybe_release_buffers(conn);
        result = amqp_consume_message(conn, &envelope, NULL, 0);

        if (AMQP_RESPONSE_NORMAL == result.reply_type) {

            printf("Received message size: %d\nbody: %s\n", envelope.message.body.len, envelope.message.body.bytes);

            amqp_destroy_envelope(&envelope);
        }
    }

    // Closing connection
    amqp_channel_close(conn, channel, AMQP_REPLY_SUCCESS);
    amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
    amqp_destroy_connection(conn);

    return 0;
}

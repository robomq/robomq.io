/**
 * File: procuder.c
 * Description: This is the STOMP producer publishes a certain number
 *     of test messages to a particular queue through STOMP broker.
 *     It will first ask user for the quantity of messages.
 *
 * Author: Eamin Zhang
 * robomq.io (http://www.robomq.io)
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "./stomp/stomp.h" //depends on where you place the library

struct ctx {
	const char *destination;
};

/**
 * This is the method to print headers.
 */
static void dump_hdrs(int hdrc, const struct stomp_hdr *hdrs)
{
	int i;
	for (i=0; i < hdrc; i++) {
		fprintf(stdout, "%s:%s\n", hdrs[i].key, hdrs[i].val);
	}
}

/**
 * This is the callback method on error.
 * @It prints the error information.
 */
static void _error(stomp_session_t *session, void *ctx, void *session_ctx)
{
	struct stomp_ctx_error *e = ctx;
	dump_hdrs(e->hdrc, e->hdrs);
	fprintf(stderr, "err: %s\n", (const char *)e->body);
}

/**
 * This is the main method which creates and runs producer instance.
 * @Exceptions on connection and publish error.
 */
int main(int argc, char *argv[]) 
{
	char* server = "hostname";
	char* port = "61613";
	char* login = "username";
	char* passcode = "password";
	char* vhost = "yourvhost";
	char* destination = "/queue/test"; //There're more options other than /queue/...
	int err;
	struct ctx client;
	stomp_session_t *session;
	struct stomp_hdr conn_hdrs[] = {
		{"login", login},
		{"passcode", passcode},
        {"vhost", vhost},
		{"accept-version", "1.2"},
		{"heart-beat", "1000,1000"},
	};

	session = stomp_session_new(&client);
	if (!session) {
		perror("stomp");
		exit(EXIT_FAILURE);
	}

	stomp_callback_set(session, SCB_ERROR, _error);

	err = stomp_connect(session, server, port, sizeof(conn_hdrs)/sizeof(struct stomp_hdr), conn_hdrs);
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}

	struct stomp_hdr send_hdrs[] = {
		{"destination", destination},
		{"content-type", "text/plain"},
		{"content-length", "20"},
	};
	int msgNum, i;
	char body[20];
	printf("Quantity of test messages: ");
	scanf("%d", &msgNum);
	for(i = 1; i <= msgNum; i++) {
		sprintf(body, "test msg %d", i);
		do {	//in case sending failed, keep retrying
			err = stomp_send(session, sizeof(send_hdrs)/sizeof(struct stomp_hdr), send_hdrs, body, 20);
			usleep(1);
		} while(err);
	}

	struct stomp_hdr disconn_hdrs[] = {
	};	//could use receipt to gracefully disconnect
	err = stomp_disconnect(session, sizeof(disconn_hdrs)/sizeof(struct stomp_hdr), disconn_hdrs);
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}

	err = stomp_run(session);	//necessary to really run the process, stop when stomp_disconnect() called
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}

	stomp_session_free(session);
	exit(EXIT_SUCCESS);

	return 0;
}

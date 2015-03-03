# You can put your build options here
-include config.mk

CFLAGS=-I. \
	-Ilibrabbitmq

DEFS = -DHAVE_CONFIG_H
LDFLAGS = -L. -lrabbitmq -lrt

ifeq (,${type})
	ERROR = $(error Please specify messaging type i.e. 'type=broadcast')
endif

DIR=${type}

all: err mqlisten mqsend

librabbitmq.a: \
	librabbitmq/amqp_api.o \
	librabbitmq/amqp_connection.o \
	librabbitmq/amqp_consumer.o \
 	librabbitmq/amqp_framing.o \
	librabbitmq/amqp_hostcheck.o \
	librabbitmq/amqp_mem.o \
	librabbitmq/amqp_socket.o \
	librabbitmq/amqp_table.o \
	librabbitmq/amqp_tcp_socket.o \
	librabbitmq/amqp_timer.o \
	librabbitmq/amqp_url.o
	$(AR) rc $@ $^

librabbitmq/amqp_api.o: librabbitmq/amqp_api.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_connection.o: librabbitmq/amqp_connection.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_consumer.o: librabbitmq/amqp_consumer.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_framing.o: librabbitmq/amqp_framing.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_hostcheck.o: librabbitmq/amqp_hostcheck.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_mem.o: librabbitmq/amqp_mem.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_socket.o: librabbitmq/amqp_socket.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_table.o: librabbitmq/amqp_table.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_tcp_socket.o: librabbitmq/amqp_tcp_socket.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_timer.o: librabbitmq/amqp_timer.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@
librabbitmq/amqp_url.o: librabbitmq/amqp_url.c
	$(CC) $(DEFS) -c $(CFLAGS) $< -o $@


%.o: $(DIR)/%.c
	$(CC) -c $(CFLAGS) $< -o $@

mqlisten: consumer.o
	$(CC) $(DEFS) $< -o $@ $(LDFLAGS)

mqsend: producer.o
	$(CC) $(DEFS) $< -o $@ $(LDFLAGS)

consumer.o: $(DIR)/consumer.c librabbitmq.a

producer.o: $(DIR)/producer.c librabbitmq.a

clean:
	rm -f producer.o consumer.o librabbitmq/*.o
	rm -f librabbitmq.a
	rm -f mqlisten mqsend

.PHONY: all clean mqlisten mqsend one-to-one
err: ; $(ERROR)

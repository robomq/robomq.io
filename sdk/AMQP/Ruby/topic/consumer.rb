# File: consumer.rb
# Description: This is the AMQP consumer handles incoming
#     communication from clients publishing messages to a broker server.
#     Messages can be received over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified binding key.
#
# Author: Eamin Zhang
# robomq.io (http://www.robomq.io)

require "bunny"

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
queueName = "testQ1"
routingKey = "test.#" #topic with wildcard

while true
  begin
    #connect, disable auto-reconnect so as to manually reconnect
    connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
    connection.start
    channel = connection.create_channel

    #declare exchange and queue, bind them and consume messages
    exchange = channel.topic(exchangeName, :auto_delete => true)
    queue = channel.queue(queueName, :exclusive => true, :auto_delete => true)
    queue.bind(exchange, :routing_key => routingKey)
    queue.subscribe(:block => false, :manual_ack => false) do |delivery_info, metadata, payload|
      puts payload
    end
    #keep checking the existence of the subscribed queue
    while true
      raise "Lost the subscribed queue %s" % queueName unless connection.queue_exists?(queueName)
      sleep 1
    end
  rescue Exception => e
    #reconnect on exception
    puts "Exception handled, reconnecting...\nDetail:\n%s" % e
    #blindly clean old connection
    begin
      connection.close
    end
    sleep 5
  end
end

# File: producer.rb
# Description: This is the AMQP producer publishes outgoing AMQP
# communication to  clients consuming messages from a broker server.
# Messages can be sent over AMQP exchange types including one-to-one,
# from broadcast pattern, or selectively using specified routing key.
#
# Author: Eamin Zhang
# robomq.io (http://www.robomq.io)

require "bunny"

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
routingKey = "testQ"

begin
  #connect
  connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
  connection.start
  channel = connection.create_channel

  #send message
  #assigning blank string to exchange is to use the default exchange, where queue name is the routing key
  exchange = channel.default_exchange
  exchange.publish("Hello World!", :routing_key => routingKey, :content_type => "text/plain", :delivery_mode => 1)

  #disconnect
  connection.close
rescue Exception => e
  puts e
end

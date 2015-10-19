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
exchangeName = "testEx"
routingKey = "test"

begin
  #connect
  connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
  connection.start
  channel = connection.create_channel

  #send message
  exchange = channel.direct(exchangeName, :auto_delete => true)
  exchange.publish("Hello World!", :routing_key => routingKey, :content_type => "text/plain", :delivery_mode => 1)

  #disconnect
  connection.close
rescue Exception => e
  puts e
end

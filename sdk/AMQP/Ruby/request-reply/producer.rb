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
replyQueue = "replyQ"
requestKey = "request"
replyKey = "reply"

begin
  #connect
  connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
  connection.start
  channel = connection.create_channel

  #listen for reply message
  exchange = channel.direct(exchangeName, :auto_delete => true)
  queue = channel.queue(replyQueue, :exclusive => true, :auto_delete => true)
  queue.bind(exchange, :routing_key => replyKey)
  isReplied = false
  consumer = queue.subscribe(:block => false, :manual_ack => false) do |delivery_info, metadata, payload|
      puts payload
    isReplied = true
  end 

  #send request message
  exchange.publish("Hello World!", :routing_key => requestKey, :content_type => "text/plain", :delivery_mode => 1, :reply_to => replyKey)

  #wait until receives the reply
  while !isReplied
  end

  #close connection once receives the reply
  cancel_ok = consumer.cancel
  connection.close
rescue Exception => e
  puts e
end

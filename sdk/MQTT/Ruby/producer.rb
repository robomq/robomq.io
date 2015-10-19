# File: procuder.rb
# Description: This is the MQTT producer publishes a certain number
#     of test messages to a particular topic through MQTT broker.
#     It will first ask user for the quantity of messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)

require "mqtt"

server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

print "Quantity of test messages: "
qty = gets.to_i

# create connection
begin
  client = MQTT::Client.connect(
    remote_host: server,
    remote_port: port,
    username: "#{vhost}:#{username}",
    password: password)
rescue MQTT::ProtocolException => e
  puts "Failed to connect to broker, detail:\n\t #{e.message}"
  exit!
end

# publish messages
(1..qty).each do |counter|
  begin
    msg = "test message #{counter}"
    client.publish(topic, msg)
    puts "=> Published:\n\t #{msg}"
  rescue => e
    puts "failed to publish message, detail: " + e.message
    sleep 1
  end
end

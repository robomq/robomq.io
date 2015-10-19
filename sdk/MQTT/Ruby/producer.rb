# File: procuder.rb
# Description: This is the MQTT producer publishes a certain number
#     of test messages to a particular topic through MQTT broker.
#     It will first ask user for the quantity of messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)

require "mqtt"

server = "10.211.55.3"
port = 1883
vhost = "customer1"
username = "customer1"
password = "customer1"
topic = "test/any"

print "Quantity of test messages: "
msgNum = gets.to_i

# create connection
begin
  client = MQTT::Client.connect(
      :host => server,
      :port => port,
      :username => "#{vhost}:#{username}",
      :password => password,
      :version => "3.1.0",
      :keep_alive => 60,
      :clean_session => true,
      :client_id => "",
      :will_qos => 1,
      :will_retain => false
  )

  # publish messages
  (1..msgNum).each do |counter|
    msg = "test msg  #{counter}"
    client.publish(topic, msg)
    sleep 1
  end

  client.disconnect(true)
end

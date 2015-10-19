# File: consumer.rb
# Description: This is the MQTT consumer handles incoming messages
#     published by producers from a particular topic.
#     Consumer prints the topic and payload as it receives messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)

require "mqtt"

# connection options
server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

# event on receiving message
def onMessage(topic, message)
  puts "Topic: #{topic}, Message: #{message}"
end

# create connection and keep getting messages
loop do
  begin
    MQTT::Client.connect(
      :host => server,
      :port => port,
      :username => "#{vhost}:#{username}",
      :password => password,
      :version => "3.1.0",
      :keep_alive => 60,
      :clean_session => true,
      :client_id => "",
      ) do |client|
          client.get(topic) do |topic, message|
            onMessage(topic, message)
          end
        end
  rescue MQTT::ProtocolException => pe
    puts "Exception handled, reconnecting...\nDetail:\n#{pe.message}"
    sleep 5
  end
end

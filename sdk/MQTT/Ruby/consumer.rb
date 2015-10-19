# File: consumer.rb
# Description: This is the MQTT consumer handles incoming messages
#     published by producers from a particular topic.
#     Consumer prints the topic and payload as it receives messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)

require "mqtt"

server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

# connection options
conn_opts = {
  remote_host: server,
  remote_port: port,
  username: "#{vhost}:#{username}",
  password: password
}

# event on receiving message
def onMessage(topic, message)
  puts "get one message from topic \"#{topic}\", message body:\n\t#{message}"
end

# create connection and keep getting messages
MQTT::Client.connect(conn_opts) do |client|
  client.get(topic) do |topic, message|
    onMessage(topic, message)
  end
end

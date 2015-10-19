require "mqtt"

server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

conn_opts = {
  remote_host: server,
  remote_port: port,
  username: "#{vhost}:#{username}",
  password: password
}

def onMessage(topic, message)
  puts "get one message from topic \"#{topic}\", message body:\n\t#{message}"
end

MQTT::Client.connect(conn_opts) do |client|
  client.get(topic) do |topic, message|
    onMessage(topic, message)
  end
end

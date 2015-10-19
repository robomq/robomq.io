require "mqtt"

server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

print "Quantity of test messages: "
qty = gets.to_i

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

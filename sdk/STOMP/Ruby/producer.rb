# File: procuder.rb
# Description: This is the STOMP producer publishes a certain number
#     of test messages to a particular queue through STOMP broker.
#     It will first ask user for the quantity of messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)


require 'stomp'

# connection options
server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"

print "Quantity of test messages: "
msgNum = gets.to_i

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0"}
}

begin
  # connect
  connection = Stomp::Connection.new(hash)

  # send messages
  (1..msgNum).each do |counter|
    message = "test msg  #{counter}"
    connection.publish(destination, message, headers = {"content-type": "text/plain"})
    # sleep 1
  end

  # disconnect
  connection.disconnect
end

# File: procuder.rb
# Description: This is the STOMP producer publishes a certain number
#     of test messages to a particular queue through STOMP broker.
#     It will first ask user for the quantity of messages.

# Author: Wesley Zhang
# robomq.io (http://www.robomq.io)


require 'stomp'

# connection options
server = "10.211.55.3"
port = "61613"
vhost = "customer1"
login = "customer1"
passcode = "customer1"
destination = "/queue/test1"

print "Quantity of test messages: "
msgNum = gets.to_i

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0", "content-type": "text/plain"}
}

begin
  # connect
  client = Stomp::Client.new(hash)

  # send messages
  (1..msgNum).each do |counter|
    message = "test msg  #{counter}"
    client.publish(destination, message)
    # sleep 1
  end

  # disconnect
  client.close
end

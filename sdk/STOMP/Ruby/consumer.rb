# File: consumer.rb
# Description: This is the STOMP consumer handles incoming messages
#     published by producers from a particular queue.
#     Consumer prints the message body as it receives messages.

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

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0", "content-type": "text/plain"}
}

loop do
  begin
    # connect
    connection = Stomp::Connection.new(hash)

    # subscribe
    connection.subscribe(destination, {"ack": "client-individual", "id": "0"})
    while msg = connection.receive
      puts msg.body
      # ack current message
      connection.ack(msg.headers['message-id'])
    end
  rescue => e
    puts "Exception handled, reconnecting...\nDetail:\n#{e.message}"
    sleep 5
  end
end

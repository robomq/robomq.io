# File: consumer.rb
# Description: This is the STOMP consumer handles incoming messages
#     published by producers from a particular queue.
#     Consumer prints the message body as it receives messages.

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

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0", "content-type": "text/plain"}
}

loop do
  begin
    # connect
    conn = Stomp::Connection.new(hash)

    # subscribe
    conn.subscribe(destination, {"ack": "client-individual", "id": "0"})
    while msg = conn.receive
      puts msg.body
      # ack current message
      conn.ack(msg.headers['message-id'])
    end
  rescue => e
    puts "Exception handled, reconnecting...\nDetail:\n#{e.message}"
    sleep 5
  end
end

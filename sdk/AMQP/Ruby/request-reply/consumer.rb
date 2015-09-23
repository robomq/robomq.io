# File: consumer.rb
# Description: This is the AMQP consumer handles incoming
#     communication from clients publishing messages to a broker server.
#     Messages can be received over AMQP exchange types including one-to-one,
#     from broadcast pattern, or selectively using specified binding key.
#
# Author: Eamin Zhang
# robomq.io (http://www.robomq.io)

require "bunny"

server = "localhost"
port = 5672
vhost = "/" 
username = "guest"
password = "guest"
exchangeName = "testEx"
requestQueue = "requestQ"
requestKey = "request"

while true
	begin
		#connect, disable auto-reconnect so as to manually reconnect
		connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
		connection.start
		channel = connection.create_channel

		#declare exchange and queue, bind them and consume messages
		exchange = channel.direct(exchangeName, :auto_delete => true)
		queue = channel.queue(requestQueue, :exclusive => true, :auto_delete => true)
		queue.bind(exchange, :routing_key => requestKey)
		queue.subscribe(:block => false, :manual_ack => true) do |delivery_info, metadata, payload|
			puts payload
			#reply according to the reply_to header
			begin
				exchange.publish("Reply to %s" % payload, :routing_key => metadata.reply_to, :content_type => "text/plain", :delivery_mode => 1)
				channel.basic_ack(delivery_info.delivery_tag, false)
			rescue
				channel.basic_nack(delivery_info.delivery_tag, false, false)
			end
		end
		#keep checking the existence of the subscribed queue
		while true
			raise "Lost the subscribed queue %s" % requestQueue unless connection.queue_exists?(requestQueue)
			sleep 1
		end
	rescue Exception => e
		#reconnect on exception
		puts "Exception handled, reconnecting...\nDetail:\n%s" % e
		#blindly clean old connection
		begin
			connection.close
		end
		sleep 5
	end
end

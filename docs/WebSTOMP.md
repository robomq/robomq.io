The Web-Stomp plugin is a simple bridge exposing the STOMP protocol over emulated HTML5 WebSockets.  
The main intention of Web-Stomp is to make it possible to use RabbitMQ from web browsers.  
RabbitMQ Web-Stomp plugin is rather simple. It takes the STOMP protocol, as provided by RabbitMQ-STOMP plugin and exposes it using a SockJS server.  
SockJS is a WebSockets poly-fill that provides a WebSocket-like JavaScript object in any browser. It will therefore work in older browsers that don't have native WebSocket support, as well as in new browsers that are behind WebSocket-unfriendly proxies.

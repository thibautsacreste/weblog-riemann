var socket = new WebSocket("ws://127.0.0.1:5556/index?subscribe=true&query=true");

socket.onmessage = function (event) {
  console.log(event.data);
}
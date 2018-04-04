var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

io.on('connection', function(socket){
    console.log('a user connected');
    socket.on('message', function (message) {
        console.log('Un client me parle ! Il me dit : ' + message);
        socket.emit("message",message)
    });	
});
http.listen(4000, function(){
  console.log('listening on *:4000');
});
    
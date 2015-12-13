var dgram = require("dgram");

var server = dgram.createSocket("udp4");

server.on("error", function (err) {
  console.log("server error:\n" + err.stack);
  server.close();
});

server.on("message", function (msg, rinfo) {
  var x = new Buffer(msg, 'hex');
  var output = "server got: [";
  for (var i=0; i < x.length; i++) {
    if (i > 0) {
      output += ", ";
    }
    output += x[i];
  }
  output += "] from " + rinfo.address + ":" + rinfo.port;
  console.log(output);
});

server.on("listening", function () {
  var address = server.address();
  console.log("server listening " +
      address.address + ":" + address.port);
});

server.bind(7890);
// A test that sets pixels repeating in a red, green, blue pattern

var dgram = require('dgram');
//var ip = "localhost"
var ip = "10.0.42.117" // esp8266 address
var port = 7890;
var rgb = [255, 0, 0, 0, 255, 0, 0, 0, 255];

var current_color = 0;

var header_size = 4;
var data_size = 150 * 3;
var header = [0, 0, data_size & 0xff, (data_size >> 8) & 0xff];

var message = new Buffer(data_size  + header_size);

message[0] = header[0];
message[1] = header[1];
message[2] = header[2];
message[3] = header[3];

for (var i=0; i < 150 * 3; i++) {
	message[header_size + i] = rgb[current_color];

	current_color += 1;
	// reset the color counter every 9th entry
	if (current_color > 0 && current_color % 9 == 0) {
		current_color = 0;
	}
}
var client = dgram.createSocket("udp4");
client.send(message, 0, message.length, port, ip, function(err) {
  client.close();
});
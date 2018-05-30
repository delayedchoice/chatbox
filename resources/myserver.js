// Load required modules
var http    = require("http");              // http server core module
var express = require("express");           // web framework external module
var serveStatic = require('serve-static');  // serve static files
var socketIo = require("socket.io");        // web socket external module
var easyrtc = require("../");               // EasyRTC external module
//var jwt = require('express-jwt');
var jwks = require('jwks-rsa');
var jwt = require('jsonwebtoken');
// Set process name
process.title = "node-easyrtc";
var publickey = 'NnGf-j03R384timhNmYS7vrB1DjlNnzVCfgf5B_wdJ02aN4HxDszqJVuo8yCliUe';
var publickey = "-----BEGIN CERTIFICATE-----\n" + 
"MIIC/TCCAeWgAwIBAgIJUJQ1j12UsO4wMA0GCSqGSIb3DQEBCwUAMBwxGjAYBgNV\n " +
"BAMTEWZlbmFyaW8uYXV0aDAuY29tMB4XDTE4MDUyODE0NDkzNVoXDTMyMDIwNDE0\n " +
"NDkzNVowHDEaMBgGA1UEAxMRZmVuYXJpby5hdXRoMC5jb20wggEiMA0GCSqGSIb3\n " +
"DQEBAQUAA4IBDwAwggEKAoIBAQC+t2mP1MaNY69BivXvAkP0X4sur6ALV0O06K9t\n " +
"zt6MaiY7nsmLWgDySGI7q0OHXiCPWjdMXlNjGkTBTnzxGRpUAF/AK8rMt8HLSyBj\n " +
"fQha5JLWM5z5HDTOxeTeJ14qgRyMOdIb64p5UMYFTqXeOr+fk3RDSOnLiGPX3ads\n " +
"m75Cevb/+Dd5dJn5ooUCIFwEsZx8aSbdWem9LxQrRsbeAr8L++kXeNslKW2Yi2Hx\n " +
"TONXqwqyVzS1HcXP4Ng54TApEiTjJgQM3+EEfK6bU7NjPwqkrae76FwR2zFimDoy\n " +
"xwXvbPq9QTz0OCw6iXrjZNqo0/g0BOe4eHjvlRA1AyBjbaG3AgMBAAGjQjBAMA8G\n " +
"A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFGit+Jpiykb8kGNgm2H0+lMhm7AgMA4G\n " +
"A1UdDwEB/wQEAwIChDANBgkqhkiG9w0BAQsFAAOCAQEAOVl+R8TRnXtook64Ls8l\n " +
"Fz8/tQq52D03C5iW1I+YGQ/rB93O51EHCnPwc/OnsHrFIbhdiR7PDe3iYp12s9jO\n " +
"DRkcBfTwEUPqWr+A078UgjcPgrEkN8by2CPtukERI4r6o7KxKZ9A37jC1nfbSXgz\n " +
"4mV5E8oP3llq6vhHq+h9+kRNLV7JVrHw08WGJwhjscNLSpeyhzX7Jl3AqjnLitKL\n " +
"BUQLeTHzKg1NfrJBokvtUqLx3LZYpVbU7y8NRHvAsJuAH9syIc+tVhQKDgGLoIbm\n " +
"FWDp9m6TfboSzCbLNcapv00xzQBZ5eRG218bHV9QfoYogOr9uKSP5RLE28hmoA5t\n " +
"zw==\n" + 
"-----END CERTIFICATE-----";
//var publickey = "MIIC/TCCAeWgAwIBAgIJUJQ1j12UsO4wMA0GCSqGSIb3DQEBCwUAMBwxGjAYBgNVBAMTEWZlbmFyaW8uYXV0aDAuY29tMB4XDTE4MDUyODE0NDkzNVoXDTMyMDIwNDE0NDkzNVowHDEaMBgGA1UEAxMRZmVuYXJpby5hdXRoMC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC+t2mP1MaNY69BivXvAkP0X4sur6ALV0O06K9tzt6MaiY7nsmLWgDySGI7q0OHXiCPWjdMXlNjGkTBTnzxGRpUAF/AK8rMt8HLSyBjfQha5JLWM5z5HDTOxeTeJ14qgRyMOdIb64p5UMYFTqXeOr+fk3RDSOnLiGPX3adsm75Cevb/+Dd5dJn5ooUCIFwEsZx8aSbdWem9LxQrRsbeAr8L++kXeNslKW2Yi2HxTONXqwqyVzS1HcXP4Ng54TApEiTjJgQM3+EEfK6bU7NjPwqkrae76FwR2zFimDoyxwXvbPq9QTz0OCw6iXrjZNqo0/g0BOe4eHjvlRA1AyBjbaG3AgMBAAGjQjBAMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFGit+Jpiykb8kGNgm2H0+lMhm7AgMA4GA1UdDwEB/wQEAwIChDANBgkqhkiG9w0BAQsFAAOCAQEAOVl+R8TRnXtook64Ls8lFz8/tQq52D03C5iW1I+YGQ/rB93O51EHCnPwc/OnsHrFIbhdiR7PDe3iYp12s9jODRkcBfTwEUPqWr+A078UgjcPgrEkN8by2CPtukERI4r6o7KxKZ9A37jC1nfbSXgz4mV5E8oP3llq6vhHq+h9+kRNLV7JVrHw08WGJwhjscNLSpeyhzX7Jl3AqjnLitKLBUQLeTHzKg1NfrJBokvtUqLx3LZYpVbU7y8NRHvAsJuAH9syIc+tVhQKDgGLoIbmFWDp9m6TfboSzCbLNcapv00xzQBZ5eRG218bHV9QfoYogOr9uKSP5RLE28hmoA5tzw==";
// Setup and configure Express http server. Expect a subfolder called "static" to be the web root.
var app = express();
app.use(serveStatic('static', {'index': ['index.html']}));

//var jwtCheck = jwt({
//    secret: jwks.expressJwtSecret({
//        cache: true,
//        rateLimit: true,
//        jwksRequestsPerMinute: 5,
//        jwksUri: "https://fenario.auth0.com/.well-known/jwks.json"
//    }),
//    audience: 'http://100.115.92.206:8081/socket.io',
//    issuer: "https://fenario.auth0.com/",
//    algorithms: ['RS256']
//});
//
//app.use(jwtCheck);
//
//app.get('/authorized', function (req, res) {
//  res.send('Secured Resource');
//});

// Start Express http server on port 8080
var webServer = http.createServer(app);

// Start Socket.io so it attaches itself to Express server
var socketServer = socketIo.listen(webServer, {"log level":1});

easyrtc.setOption("logLevel", "debug");
easyrtc.setOption("appIceServers", [{ "url":"stun:fenario.hopto.org:5349" },
                   { "url":"turn:fenario.hopto.org:5349", "username":"bobi", "credential":"9Bergen4" },
                   { "url":"turn:fenario.hopto.org:5349?transport=tcp", "username":"bobi", "credential":"Bergen4" } ]);
// Overriding the default easyrtcAuth listener, only so we can directly access its callback
easyrtc.events.on("easyrtcAuth", function(socket, easyrtcid, msg, socketCallback, callback) {
    easyrtc.events.defaultListeners.easyrtcAuth(socket, easyrtcid, msg, socketCallback, function(err, connectionObj){
        if (err || !msg.msgData || !msg.msgData.credential || !connectionObj) {
            callback(err, connectionObj);
            return;
        }

        connectionObj.setField("credential", msg.msgData.credential, {"isShared":false});

        console.log("["+easyrtcid+"] Credential saved!", connectionObj.getFieldValueSync("credential"));

        callback(err, connectionObj);
    });
});

// To test, lets print the credential to the console for every room join!
easyrtc.events.on("roomJoin", function(connectionObj, roomName, roomParameter, callback) {
    console.log("["+connectionObj.getEasyrtcid()+"] Credential retrieved!", connectionObj.getFieldValueSync("credential"));
    easyrtc.events.defaultListeners.roomJoin(connectionObj, roomName, roomParameter, callback);
});

var onAuthenticate = function(socket, easyrtcid, appName, username, credential, easyrtcAuthMessage, next){
      //if (username === "bobby.harris@gmail.com" || username === "c" || username === "f"){
      var verifresult = jwt.verify(credential['token'], publickey, {algorithms: ['RS256']});
      console.log('verify: ' + JSON.stringify(verifresult));
      if (jwt.verify(credential['token'], publickey, {algorithms: ['RS256']})){
        next(null);
      }
      else {
        next(new easyrtc.util.ConnectionError("Failed auth." + username));
      }
    };

    easyrtc.events.on("authenticate", onAuthenticate);
// Start EasyRTC server
var rtc = easyrtc.listen(app, socketServer, null, function(err, rtcRef) {
    console.log("Initiated");

    rtcRef.events.on("roomCreate", function(appObj, creatorConnectionObj, roomName, roomOptions, callback) {
        console.log("roomCreate fired! Trying to create: " + roomName);

        appObj.events.defaultListeners.roomCreate(appObj, creatorConnectionObj, roomName, roomOptions, callback);
    });
});

//listen on port 8080
webServer.listen(8080, function () {
    console.log('listening on http://localhost:8080');
});

var cordova = require('cordova');
var exec = require('cordova/exec');
var channel = require('cordova/channel');

function Estimote(){
	var me = this;
}

Estimote.prototype.startListening = function(arg){
    exec(estimote._notification, estimote._error, "Estimote", "startListening", [encodeURIComponent(arg)]);
}

Estimote.prototype.stopListening = function(arg, success, error){
    exec(success, error, "Estimote", "stopListening");
}


Estimote.prototype._notification = function(info){
    cordova.fireDocumentEvent("beaconsReceived", info);
}

Estimote.prototype._error = function(e) {
    console.log("Error receiving message: " + e);
};

var estimote = new Estimote();

module.exports = estimote;

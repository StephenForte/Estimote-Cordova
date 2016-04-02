var cordova = require('cordova');
var exec = require('cordova/exec');

function Estimote() {};

Estimote.prototype.scanOnce = function scanOnce(region) {

    var options = {};

    if (typeof region === 'object') {
        options = region;
    } else if (typeof region === 'string') {
        options.region = region;
    }

    return new Promise(function(resolve, reject) {
        /**
         * all the usability of _notification + more.
         **/
        var resolve_function = function(info) {
            if (info.beacons.length > 0) {
                cordova.fireDocumentEvent('beaconsReceived', info);
                estimote.stopRanging();
                resolve(info);
            }
        };
        /**
         *  Actually make the error useful!
         **/
        var reject_function = function(error) {
            console.error(error);
            estimote.stopRanging();
            reject(error);
        }

        exec(resolve_function, reject_function, 'Estimote', 'startRanging', [options]);

    });
};

Estimote.prototype.startRanging = function startRanging(region) {
    var options = {};

    if (typeof region === 'object') {
        options = region;
    } else if (typeof region === 'string') {
        options.region = region;
    }

    var resolve_function = function(info) {
        if (info.beacons.length > 0) {
            cordova.fireDocumentEvent('beaconsReceived', info);
        }
    };

    var reject_function = function(error) {
        console.error(error);
    };

    exec(resolve_function, reject_function, 'Estimote', 'startRanging', [options]);

}

Estimote.prototype.stopRanging = function stopRanging() {
    exec(null, null, 'Estimote', 'stopRanging', []);
};

var estimote = new Estimote();

module.exports = estimote;

var argscheck   = require('cordova/argscheck'),
    channel     = require('cordova/channel'),
    exec        = require('cordova/exec'),
    cordova     = require('cordova'),

    bridgefyOperations = {
        BRIDGEFY_INITIALIZE: 'initialize',
        BRIDGEFY_START: 'start',
        BRIDGEFY_STOP: 'stop'
    },

    bridgefyListeners = {
        BRIDGEFY_DEVICE_LISTENER: 'deviceListener',
        BRIDGEFY_MESSAGE_LISTENER: 'messageListener'
    },

    bridgefyActions = {
        BRIDGEFY_MESSAGE_SEND: 'sendMessage',
        BRIDGEFY_MESSAGE_SEND_BROADCAST: 'sendBroadcastMessage',
        BRIDGEFY_CONNECT_DEVICE: 'connectDevice',
        BRIDGEFY_DISCONNECT_DEVICE: 'disconnectDevice'
    },

    BRIDGEFY_SERVICE = 'Bridgefy';

channel.createSticky('onCordovaInfoReady');
// Tell cordova channel to wait on the CordovaInfoReady event
channel.waitForInitialization('onCordovaInfoReady');

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Bridgefy () {
    channel.onCordovaReady.subscribe(function () {
        channel.onCordovaInfoReady.fire();
    });
}

/**
 * Start Bridgefy SDK
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 * @param apiKey
 */
Bridgefy.prototype.init = function (successCallback, errorCallback, apiKey) {
    argscheck.checkArgs('ffs', bridgefyOperations.BRIDGEFY_INITIALIZE, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyOperations.BRIDGEFY_INITIALIZE, [apiKey]);
};
/**
 * Start device listener
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Bridgefy.prototype.deviceListener = function (successCallback, errorCallback) {
    argscheck.checkArgs('fF', bridgefyListeners.BRIDGEFY_DEVICE_LISTENER, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyListeners.BRIDGEFY_DEVICE_LISTENER, []);
};

/**
 * Start message listener
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Bridgefy.prototype.messageListener = function (successCallback, errorCallback) {
    argscheck.checkArgs('fF', bridgefyListeners.BRIDGEFY_MESSAGE_LISTENER, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyListeners.BRIDGEFY_MESSAGE_LISTENER, []);
};

/**
 * Start Operations of Bridgefy
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 * @param options - Configure Bridgefy operation mode
 */
Bridgefy.prototype.start = function (successCallback, errorCallback, options) {
    argscheck.checkArgs('fFO', bridgefyOperations.BRIDGEFY_START, arguments);
    var configureOptions = {
        autoConnect: true,
        encryption: true,
        engineProfile: 'BFConfigProfileDefault',
        energyProfile: 'HIGH_PERFORMANCE',
        ...options
    };
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyOperations.BRIDGEFY_START, [configureOptions]);
};

/**
 * Start Operations of Bridgefy
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Bridgefy.prototype.stop = function (successCallback, errorCallback) {
    argscheck.checkArgs('fF', bridgefyOperations.BRIDGEFY_STOP, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyOperations.BRIDGEFY_STOP, []);
};

/**
 * Enqueue message to sent by mesh or direct
 * @param successCallback
 * @param errorCallback
 * @param device
 * @param message
 */
Bridgefy.prototype.sendMessage = function (successCallback, errorCallback, device, message) {
    argscheck.checkArgs('fFOO', bridgefyActions.BRIDGEFY_MESSAGE_SEND, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyActions.BRIDGEFY_MESSAGE_SEND, [device, message]);
};

/**
 * Enqueue message to sent broadcast
 * @param successCallback
 * @param errorCallback
 * @param message
 */
Bridgefy.prototype.sendBroadcastMessage = function (successCallback, errorCallback, message) {
    argscheck.checkArgs('fFO', bridgefyActions.BRIDGEFY_MESSAGE_SEND_BROADCAST, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyActions.BRIDGEFY_MESSAGE_SEND_BROADCAST, [message]);
};

/**
 * Connect to device with userId
 * @param successCallback
 * @param errorCallback
 * @param userId
 */
Bridgefy.prototype.connectDevice = function(successCallback, errorCallback, userId) {
    argscheck.checkArgs('fFs', bridgefyActions.BRIDGEFY_CONNECT_DEVICE, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyActions.BRIDGEFY_CONNECT_DEVICE, [userId]);
};

/**
 * Disconnect device for userId
 * @param successCallback
 * @param errorCallback
 * @param userId
 */
Bridgefy.prototype.disconnectDevice = function(successCallback, errorCallback, userId) {
    argscheck.checkArgs('fFs', bridgefyActions.BRIDGEFY_DISCONNECT_DEVICE, arguments);
    exec(successCallback, errorCallback, BRIDGEFY_SERVICE, bridgefyActions.BRIDGEFY_DISCONNECT_DEVICE, [userId]);
};

/**
 * Bridgefy Version Library
 * @type {string}
 */
Bridgefy.prototype.version = '2.0.1';
/**
 * Bridgefy Default Options
 * @type {{autoConnect: boolean, energyProfile: number, encryption: boolean, engineProfile: number}}
 */
Bridgefy.prototype.options = {
    autoConnect: true, // tells Bridgefy if we want Automatic on On-demand mode
    encryption: true, // All communication is secure
    engineProfile: 'BFConfigProfileDefault' | 'BFConfigProfileHighDensityNetwork' |
        'BFConfigProfileSparseNetwork' | 'BFConfigProfileLongReach' |
        'BFConfigProfileShortReach' | 'BFConfigProfileNoFowarding', // Android only, see https://github.com/bridgefy/bridgefy-android-samples#engine-profile
    energyProfile: 'ENERGY_SAVER' | 'BALANCED' | 'HIGH_PERFORMANCE' // Android only, see https://github.com/bridgefy/bridgefy-android-samples#energy-profile
};

module.exports = new Bridgefy();

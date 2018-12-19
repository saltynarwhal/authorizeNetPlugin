// Empty constructor
function authorizeNetPlugin() {}

authorizeNetPlugin.prototype.initMerchant = function(InitObject, successCallback, errorCallback) {
  if(InitObject.environment != 'sandbox' &&
     InitObject.environment != 'production'){
      throw new Error('Environment not valid. '+ InitObject.environment );
  }

  var args = [
    InitObject.device_id,
    InitObject.device_description,
    InitObject.device_number,
    InitObject.username,
    InitObject.password,
    InitObject.environment
  ];

  cordova.exec(successCallback, errorCallback, 'authorizeNetPlugin', 'initMerchant', args);
}

authorizeNetPlugin.createEMVTransaction = function(options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'authorizeNetPlugin', 'createEMVTransaction', [options]);
}

authorizeNetPlugin.createNonEMVTransaction = function(options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'authorizeNetPlugin', 'createNonEMVTransaction', [options]);
}

//var PLUGIN_NAME = 'AuthorizeNetPlugin';

/*AuthorizeNetPlugin = {
  initMerchant: function(options, done, error) {
    if(options.environment != 'sandbox' &&
       options.environment != 'production'){
        throw new Error('Environment not valid. '+ options.environment );
    }

    var args = [
      options.device_id,
      options.device_description,
      options.device_number,
      options.username,
      options.password,
      options.environment
    ];

    cordova.exec(done, error, PLUGIN_NAME, 'initMerchant', args);
  },
  createEMVTransaction: function(options, done, error) {
    cordova.exec(done, error, PLUGIN_NAME, 'createEMVTransaction', [options]);
  },
  createNonEMVTransaction: function(options, done, error) {
    cordova.exec(done, error, PLUGIN_NAME, 'createNonEMVTransaction', [options]);
  },
  Environment: {
    SANDBOX: 'sandbox',
    PRODUCTION: 'production'
  }
};*/

//module.exports = AuthozizeNetPlugin;

// Installation constructor that binds AuthorizeNetPlugin to window
authorizeNetPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.authorizeNetPlugin = new authorizeNetPlugin();
  return window.plugins.authorizeNetPlugin;
};
cordova.addConstructor(authorizeNetPlugin.install);

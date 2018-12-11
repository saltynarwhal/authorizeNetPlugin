// Empty constructor
function AuthorizeNetPlugin() {}

var PLUGIN_NAME = 'AuthorizeNetPlugin';

AuthozizeNetPlugin = {
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
};

//module.exports = AuthozizeNetPlugin;

// Installation constructor that binds AuthorizeNetPlugin to window
AuthozizeNetPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.toastyPlugin = new AuthozizeNetPlugin();
  return window.plugins.AuthozizeNetPlugin;
};
cordova.addConstructor(AuthozizeNetPlugin.install);

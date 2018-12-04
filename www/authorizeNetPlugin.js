var exec = require('cordova/exec');

var PLUGIN_NAME = 'AuthorizeNetPlugin';

var AuthozizeNetPlugin = {
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

    exec(done, error, PLUGIN_NAME, 'initMerchant', args);
  },
  createEMVTransaction: function(options, done, error) {
    exec(done, error, PLUGIN_NAME, 'createEMVTransaction', [options]);
  },
  createNonEMVTransaction: function(options, done, error) {
    exec(done, error, PLUGIN_NAME, 'createNonEMVTransaction', [options]);
  },
  Environment: {
    SANDBOX: 'sandbox',
    PRODUCTION: 'production'
  }
};

module.exports = AuthozizeNetPlugin;

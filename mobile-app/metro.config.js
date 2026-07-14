const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

// If you need to add any custom options, do it here, e.g.:
// config.transformer.minifierConfig.mangle = false;

module.exports = config;
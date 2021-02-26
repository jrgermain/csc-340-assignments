/**
 * Merge the settings in defaults.js and args.js into one object.
 * Options from args.js take precedence over those in defaults.js.
 * 
 * Available options are as follows:
 *   - port: integer
 *   - debug: boolean
 */
const defaults = require("./defaults");
const args = require("./args");

module.exports = { ...defaults, ...args };
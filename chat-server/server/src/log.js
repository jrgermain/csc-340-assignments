/**
 * Provides 2 logging functions.
 * 
 * {print} prints data to standard output unconditionally.
 * 
 * {debug} prints data to standard output if and only if the "debug" flag has been set,
 * adding "DEBUG:" to the beginning of the message.
 */

const { debug } = require("../config");

exports.print = console.log;

exports.debug = function (...info) {
    if (debug) {
        console.log("DEBUG: ", ...info);
    }
}
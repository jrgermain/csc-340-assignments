/**
 * Provides a utility function for parsing data received from the client.
 * 
 * If the data follows the chat server protocol, a command object is returned,
 * containing "name" and "argument" properties.
 * 
 * If the data does not follow the protocol, null is returned instead.
 */

// Regular expressions to test if a command is valid and, if so, what the argument provided is
const commands = {
    ENTER: /^ENTER ([A-Za-z0-9]{1,16})\n?$/,
    JOIN: /^JOIN ([A-Za-z0-9]{1,16})\n?$/,
    TRANSMIT: /^TRANSMIT (.{1,1024})(?=\n|$)/,
    EXIT: /^EXIT\n?$/
}

// Given a string of data potentially containing a command, parse out the command (if it is valid), otherwise return null
function parseCommand(data) {
    for (const command in commands) {
        const commandExpr = commands[command];
        if (commandExpr.test(data)) {
            return {
                name: command,
                argument: data.match(commandExpr)[1]
            }
        }
    }
    return null;
}

module.exports = parseCommand;
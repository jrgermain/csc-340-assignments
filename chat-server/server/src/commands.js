// Regular expressions to test if a command is valid
const commands = {
    enter: /^ENTER ([A-Za-z0-9]{1,16})\n?$/,
    join: /^JOIN ([A-Za-z0-9]{1,16})\n?$/,
    transmit: /^TRANSMIT (.{1,1024})(?=\n|$)/,
    exit: /^EXIT\n?$/
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
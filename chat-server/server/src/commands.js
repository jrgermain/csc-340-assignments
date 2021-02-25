// Regular expressions to test if a command is valid
// e.g. exports.ENTER matches a valid enter command

exports.ENTER = /^ENTER ([A-Za-z0-9]{1,16})\n?$/
exports.JOIN = /^JOIN ([A-Za-z0-9]{1,16})\n?$/
exports.TRANSMIT = /^TRANSMIT (.{1,1024})(?=\n|$)/
exports.EXIT = /^EXIT\n?$/

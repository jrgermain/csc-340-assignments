const Server = require("net").Server;
const commands = require("./commands");
const { port, debug } = require("../config");

const server = new Server();
const openConnections = new Set();

// When the server gets a new connection, listen to it
server.on("connection", socket => {
    // This is a function that runs every time a new client connects
    if (debug) {
        console.log("DEBUG: New client connected");
    }

    // Keep track of open connections
    openConnections.add(socket);
    socket.on("close", () => {
        if (debug) {
            console.log("DEBUG: Client disconnected");
        }
        openConnections.delete(socket);
    });

    // Handle data received
    socket.setEncoding("utf8");
    socket.on("data", data => {
        // Check if the command received is valid. If so, get the argument and process the command.
        if (commands.ENTER.test(data)) {
            // Received a valid ENTER command
            const name = data.match(commands.ENTER)[1];
            if (debug) {
                console.log(`DEBUG: Valid "ENTER" command; name = "${name}"`);
            }
        } else if (commands.JOIN.test(data)) {
            // Received a valid JOIN command
            const room = data.match(commands.JOIN)[1];
            if (debug) {
                console.log(`DEBUG: Valid "JOIN" command; room = "${room}"`);
            }
        } else if (commands.TRANSMIT.test(data)) {
            // Received a valid TRANSMIT command
            const message = data.match(commands.TRANSMIT)[1];
            if (debug) {
                console.log(`DEBUG: Valid "TRANSMIT" command; message = "${message}"`);
            }
        } else if (commands.EXIT.test(data)) {
            // Received a valid EXIT command
            if (debug) {
                console.log(`DEBUG: Valid "EXIT" command`);
            }
        } else {
            // Invlaid command
            console.error(`Invalid input: "${data}"`);
        }
    });
});

// When the program is killed (ctrl-c), close all connections
process.on("SIGINT", () => {
    if (debug) {
        console.log("DEBUG: Shutting down");
    }
    server.close(); // Stop accepting new connections
    openConnections.forEach(socket => socket.destroy()); // Close existing connections
});

// Start listening and print out a debug message
server.listen(port, () => {
    if (debug) {
        console.log("DEBUG: Now listening on port " + port);
    }
});
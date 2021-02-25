const Server = require("net").Server;
const parseCommand = require("./commands");
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
        // Check if the command received is valid. If so, process the command.
        const command = parseCommand(data);
        if (command == null) {
            console.error(`Invalid input: "${data}"`);
            return;
        }

        console.log(`DEBUG: Valid "${command.name}" command; argument is "${command.argument}"`);
        switch (command.name) {
            case "enter":
                break;
            case "join":
                break;
            case "transmit":
                break;
            case "exit":
                break;
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
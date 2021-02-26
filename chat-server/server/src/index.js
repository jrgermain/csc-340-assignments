/**
 * Chat Server
 * Written by Joey Germain, Phil Nam, and Ryan Clark
 * 
 * This file is the main entry point of the server program.
 */

const { Server } = require("net");
const logger = require("./log");
const { port } = require("../config");
const parseCommand = require("./commands");

const server = new Server();
const openConnections = new Map();

// When the server gets a new connection, listen to it
server.on("connection", socket => {
    // This is a function that runs every time a new client connects
    logger.debug("New client connected");

    // Keep track of open connections
    openConnections.set(socket, {});
    socket.on("close", () => {
        logger.debug("Client disconnected");
        openConnections.delete(socket);
    });

    // Handle data received
    socket.setEncoding("utf8");
    socket.on("data", data => {
        // Check if the command received is valid. If so, process the command.
        const command = parseCommand(data);
        if (command == null) {
            logger.print(`Received invalid input: "${data}"`);
            return;
        }

        logger.print("Received command: ", command);

        const currentUserData = openConnections.get(socket);
        switch (command.name) {
            case "enter":
                currentUserData.room = "0";
                currentUserData.name = command.argument;
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`ENTERING ${command.argument}\n`);
                    }
                });
                socket.write(`ACK ${data}\n`);
                break;
            case "join":
                // TODO (Joey)
                break;
            case "transmit":
                // TODO (Phillip)
                break;
            case "exit":
                // TODO (Ryan)
        }
        logger.debug("Currently connected: ", Array.from(openConnections.values()));
    });
});

// When the program is killed (ctrl-c), close all connections
process.on("SIGINT", () => {
    logger.debug("Shutting down");
    server.close(); // Stop accepting new connections
    
    // Close existing connections
    for (const socket of openConnections.keys()) {
        socket.destroy();
    }
});

// Start listening and print out a debug message
server.listen(port, () => {
    logger.debug("Now listening on port " + port);
});
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
const openConnections = new Set();

// When the server gets a new connection, listen to it
server.on("connection", socket => {
    // This is a function that runs every time a new client connects
    logger.debug("New client connected");

    // Keep track of open connections
    openConnections.add(socket);
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
        switch (command.name) {
            case "enter":
                // TODO
                break;
            case "join":
                // TODO
                break;
            case "transmit":
                // TODO
                break;
            case "exit":
                // TODO
        }
    });
});

// When the program is killed (ctrl-c), close all connections
process.on("SIGINT", () => {
    logger.debug("Shutting down");
    server.close(); // Stop accepting new connections
    openConnections.forEach(socket => socket.destroy()); // Close existing connections
});

// Start listening and print out a debug message
server.listen(port, () => {
    logger.debug("Now listening on port " + port);
});
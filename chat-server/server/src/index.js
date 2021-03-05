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
server.on("connection", (socket) => {
    // This is a function that runs every time a new client connects
    logger.debug("New client connected");

    // Keep track of open connections
    openConnections.set(socket, {});
    socket.on("close", () => {
        loggoer.debug("Client disconnected");
        openConnections.delete(socket);
        logger.debug("Currently connected: ", Array.from(openConnections.values()));
    });

    // Handle data received
    socket.setEncoding("utf8");
    socket.on("data", (data) => {
        // Check if the command received is valid. If so, process the command.
        const command = parseCommand(data);
        if (command == null) {
            logger.print(`Received invalid input: "${data}"`);
            return;
        }

        logger.print("Received command: ", command);

        const currentUserData = openConnections.get(socket);
        switch (command.name) {
            case "ENTER":
                // Set user data
                currentUserData.room = "0";
                currentUserData.name = command.argument;

                // Broadcast "entering" to clients in the same room as the user
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`ENTERING ${currentUserData.name}\n`);
                    }
                });

                // Send acknowledgement back to the user
                socket.write(`ACK ${command.name} ${command.argument}\n`);
                logger.debug("Currently connected: ", Array.from(openConnections.values()));
                break;
            case "JOIN":
                // Broadcast "exiting" to clients in the user's old room
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`EXITING ${currentUserData.name}\n`);
                    }
                });

                // Move user to new room
                currentUserData.room = command.argument;

                // Broadcast "entering" to clients in the user's new room
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`ENTERING ${currentUserData.name}\n`);
                    }
                });

                // Send acknowledgement back to the user
                socket.write(`ACK ${command.name} ${command.argument}\n`);
                logger.debug("Currently connected: ", Array.from(openConnections.values()));
                break;
            case "TRANSMIT":
                // Broadcast message to all clients in the same room as the user
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`NEWMESSAGE ${currentUserData.name} ${command.argument}\n`);
                    }
                });

                // Send acknowledgement back to the user
                socket.write(`ACK ${command.name} ${command.argument}\n`);
                break;
            case "EXIT":
                openConnections.forEach((data, connection) => {
                    if (data.room === currentUserData.room) {
                        connection.write(`EXITING ${currentUserData.name}\n`);
                    }
                });
                socket.write(`ACK ${command.name}\n`);
                socket.destroy();
        }
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

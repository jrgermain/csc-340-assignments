const net = require("net");
const { port } = require("../config/settings");

// Keep track of open connections so we can close them when the server shuts down
const sockets = new Set();

const server = net.createServer((socket) => {
    // This is a function that runs every time a new client connects
    console.log("DEBUG: New client connected ");

    // Keep track of open connections
    sockets.add(socket);
    socket.on("close", () => {
        console.log("DEBUG: Client disconnected ");
        sockets.delete(socket);
    });
});

// Start listening and print out a debug message
server.listen(port, () => {
    console.log("DEBUG: Now listening on port " + port);
});

// When the program is killed (ctrl-c), close all connections
process.on("SIGINT", () => {
    console.log("DEBUG: Shutting down");
    server.close(); // Stop accepting new connections
    sockets.forEach(socket => socket.destroy()); // Close existing connections
});
const Server = require("net").Server;
const { port } = require("../config/settings");

const server = new Server();
const openConnections = new Set();

// When the server gets a new connection, listen to it
server.on("connection", socket => {
    // This is a function that runs every time a new client connects
    console.log("DEBUG: New client connected");

    // Keep track of open connections
    openConnections.add(socket);
    socket.on("close", () => {
        console.log("DEBUG: Client disconnected");
        openConnections.delete(socket);
    });

    // Handle data received
    socket.setEncoding("utf8");
    socket.on("data", data => {
        // TODO: do something with the data
        console.log(`DEBUG: Data received: "${data}"`);
    });
});

// When the program is killed (ctrl-c), close all connections
process.on("SIGINT", () => {
    console.log("DEBUG: Shutting down");
    server.close(); // Stop accepting new connections
    openConnections.forEach(socket => socket.destroy()); // Close existing connections
});

// Start listening and print out a debug message
server.listen(port, () => {
    console.log("DEBUG: Now listening on port " + port);
});
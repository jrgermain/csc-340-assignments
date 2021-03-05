# Chat Server

## How to run the server:
First, make sure you've installed node 12.x or later
### The "easy" way
1. Navigate to the "server" directory
2. Run `npm start` to start the server in debug mode on the default port, 1518
### The "advanced" way
1. Navigate to the "server" directory
2. Run `node src [-d, --debug] [-p, --port PORT]`

## How to run the client:
1. Navigate to directory containing ChatClient.java
2. Run `javac ChatClient.java`
3. Run `java ChatClient`

## Description of the task division:
Server side: Joey, Ryan, Phil
Client side: Kevin, Brian, James

### Individual roles
Joey: Server starter code, enter and join commands

Ryan: Exiting command

Phillip: Transmitting a message

Brian: Connecting to server, Message transmition 

Kevin: Change name, Room changing

James: Client properly indicates message recieved

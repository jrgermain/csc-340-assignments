const args = process.argv.slice(2);
const settings = {};

for (let i = 0; i < args.length; i++) {
    switch (args[i]) {
        case "--port":
        case "-p":
            settings.port = args[++i];
            break;
        case "--debug":
        case "-d":
            settings.debug = true;
            break;
        default:
            throw new Error("Unexpected argument: " + args[i]);
    }
}

module.exports = settings;
function connect(login) {
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += ":" + loc.port + "/greeter/login";

    var socket = new WebSocket(new_uri);
}
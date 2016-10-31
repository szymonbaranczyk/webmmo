var socket;
function connect(login) {
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += "/greeter/" + login;

    socket = new WebSocket(new_uri);

    socket.onmessage = function (m) {
        //document.getElementById("debug").innerText = document.getElementById("debug").innerText + m.data;
        var data = JSON.parse(m.data);
        //console.log(m.data);
        data.playersData.forEach(function(player){
            var tank = undefined;
            tanks.forEach(function (t) {
                if(t.id===player.id){
                    tank=t;
                }
            });
            if(tank==null){
                tank=new Tank(stage,{x:player.x,y:player.y},player.id);
                tanks.push(tank);
            }else{
                tank.move(player.x,player.y,-player.rotation,player.turretRotation)
            }
        })
    };
    socket.onclose = function () {
      console.log("closed");
    };
}
connect(makeId());
var left=false;
var right=false;
var down=false;
var up=false;
window.addEventListener('keydown', function(event) {
    switch (event.keyCode) {
        case 65: // Left
            left=true;
            break;

        case 87: // Up
            up=true;
            break;

        case 68: // Right
            right=true;
            break;

        case 83: // Down
            down=true;
            break;
    }
}, false);
window.addEventListener('keyup', function(event) {
    switch (event.keyCode) {
        case 65: // Left
            left=false;
            break;

        case 87: // Up
            up=false;
            break;

        case 68: // Right
            right=false;
            break;

        case 83: // Down
            down=false;
            break;
    }
}, false);
setInterval(function () {
    var acc = up ? 1 : (down ? -1 : 0);
    var rot = left ? 1 : (right ? -1 : 0);
    var input = {
        "acceleration" : acc,
        "rotation" : rot,
        "turretRotation" : 0,
        "shot": false
    };
    console.log(JSON.stringify(input));
    socket.send(JSON.stringify(input));
},100);

function makeId()
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 5; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}
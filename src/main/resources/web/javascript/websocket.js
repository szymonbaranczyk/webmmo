var socket;
var myLogin;
function connect(login) {
    myLogin = login;
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += "/greeter/" + login;

    socket = new WebSocket(new_uri);
    setInterval(interval, 100);
    var border = new Border(stage, 4000);
    socket.onmessage = function (m) {
        //document.getElementById("debug").innerText = document.getElementById("debug").innerText + m.data;
        var cameraTranslation = {x: 0, y: 0};
        var stageHeight = stage.canvas.height;
        var stageWidth = stage.canvas.width;
        var data = JSON.parse(m.data);
        data.playersData.forEach(function (player) {
            if (player.id === myLogin) {
                cameraTranslation.x = stageWidth / 2 - player.x;
                cameraTranslation.y = stageHeight / 2 - player.y;
            }
        });
        border.move(cameraTranslation.x, cameraTranslation.y);
        data.playersData.forEach(function(player){
            var tank = undefined;
            if (player.id == myLogin) {
                updateMeta(player.meta);
            }
            tanks.forEach(function (t) {
                if(t.id===player.id){
                    tank=t;
                }
            });
            if(tank==null){
                tank = new Tank(stage, {
                    x: player.x + cameraTranslation.x,
                    y: player.y + cameraTranslation.y
                }, player.id, player.id == myLogin, {chassis: player.rotation, turret: player.turretRotation});
                tanks.push(tank);
            }else{
                tank.move(player.x + cameraTranslation.x, player.y + cameraTranslation.y, -player.rotation, -player.turretRotation)
            }
        });
        data.bulletData.forEach(function (bulletData) {
            var bullet = undefined;
            bullets.forEach(function (b) {
                if (b.id === bulletData.id) {
                    bullet = b;
                }
            });
            if (bullet == null) {
                bullet = new Bullet(stage, {
                    x: bulletData.x + cameraTranslation.x,
                    y: bulletData.y + cameraTranslation.y
                }, bulletData.id);
                bullets.push(bullet);
            } else {
                bullet.move(bulletData.x + cameraTranslation.x, bulletData.y + cameraTranslation.y);
            }
        });
        if (data.playersData.length != tanks.length) {
            var missingTanks = [];
            tanks.forEach(function (t) {
                exist = false;
                data.playersData.forEach(function (p) {
                    if (p.id == t.id) {
                        exist = true;
                    }
                });
                if (!exist) {
                    missingTanks.push(t)
                }
            });
            missingTanks.forEach(function (e) {
                tanks.splice(tanks.indexOf(e), 1);
                e.kill();
            });
        }
        if (data.bulletData.length != bullets.length) {
            var missingBullets = [];
            bullets.forEach(function (t) {
                exist = false;
                data.bulletData.forEach(function (p) {
                    if (p.id == t.id) {
                        exist = true;
                    }
                });
                if (!exist) {
                    missingBullets.push(t)
                }
            });
            missingBullets.forEach(function (e) {
                explode(stage, {x: e.bullet.x, y: e.bullet.y});
                bullets.splice(bullets.indexOf(e), 1);
                e.kill();
            });
        }
    };
    socket.onclose = function () {
      console.log("closed");
    };
}
var left=false;
var right=false;
var down=false;
var up=false;
var shot = false;
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
window.addEventListener('mousedown', function (event) {
    shot = true;
}, false);

function clockwiseDistance(x, y) {
    return y > x ? y - x : 360 - (x - y);
}
function counterClockwiseDistance(x, y) {
    return y > x ? 360 - (y - x) : x - y;
}
function interval() {
    var acc = up ? 1 : (down ? -1 : 0);
    var rot = left ? 1 : (right ? -1 : 0);

    var mytank = tanks.filter(function(t){
        return t.id === myLogin;
    });
    var degree = Math.atan((mytank[0].chassis.y-stage.mouseY)/(mytank[0].chassis.x-stage.mouseX)) * (180/Math.PI);
    if(mytank[0].chassis.x<stage.mouseX)
    {
        if(mytank[0].chassis.y<stage.mouseY){
            degree = 90 + degree;
        }
        if(mytank[0].chassis.y>stage.mouseY){
            degree = 90 + degree;
        }
    }else{
        if(mytank[0].chassis.y<stage.mouseY){
            degree += 270;
        }
        if(mytank[0].chassis.y>stage.mouseY){
            degree += 270;
        }
    }
    var turretDegree = mytank[0].turret.rotation >= 0? mytank[0].turret.rotation % 360 : 360 + mytank[0].turret.rotation % 360;
    var turRot = clockwiseDistance(turretDegree, degree) > counterClockwiseDistance(turretDegree, degree)? -1 : 1;
    degree = degree + 180 >= 360 ? degree + 180 - 360 : degree + 180;
    if(Math.abs(turretDegree - degree) < 10){
        turRot = 0;
    }
    var input = {
        "acceleration" : acc,
        "rotation" : rot,
        "turretRotation" : turRot,
        "shot": shot
    };
    shot = false;
    socket.send(JSON.stringify(input));
}
function makeId()
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 5; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}
function updateMeta(meta) {
    var lives = parseInt(meta[0]);
    if (lives != $(".glyphicon-heart").length) {
        var hearts = $(".heart");
        for (var i = 0; i < hearts.length; i++) {
            if (i < lives) {
                $(hearts[i]).addClass("glyphicon-heart").removeClass("glyphicon-heart-empty");
            } else {
                $(hearts[i]).removeClass("glyphicon-heart").addClass("glyphicon-heart-empty");
            }
        }
    }
}


var stage;
var tanks = [];
var bullets = [];
var size = 4000;
function Border(stage, size) {
    this.paint = new createjs.Shape();
    this.paint.graphics.beginFill("#000000").drawRect(0, 0, size, 4);
    this.paint.graphics.beginFill("#000000").drawRect(0, 0, 4, size);
    this.paint.graphics.beginFill("#000000").drawRect(0, size - 4, size, 4);
    this.paint.graphics.beginFill("#000000").drawRect(size - 4, 0, 4, size);
    for (var i = 1; i - 1 < size / 100; i++) {
        this.paint.graphics.beginFill("#688bea").drawRect(0, i * 100, size, 1);
        this.paint.graphics.beginFill("#688bea").drawRect(i * 100, 0, 1, size);
    }

    stage.addChild(this.paint);
    this.move = function (x, y) {
        createjs.Tween.get(this.paint).to({x: x, y: y}, 100)
    }
}
function Tank(stage, location, id, player, rotation) {
    this.id=id;
    this.chassis = new createjs.Shape();
    var chassisColor = player ? "#000033" : "#003300";
    var turretColor = player ? "#000066" : "#006600";
    this.chassis.graphics.beginFill(chassisColor).moveTo(25, 40).lineTo(25, -40).lineTo(-25, -40).lineTo(-25, 40).lineTo(25, 40);
    this.chassis.graphics.beginFill("#404040").moveTo(-15,-25).lineTo(15,-25).lineTo(15,-35).lineTo(-15,-35).lineTo(-15,-25);
    this.chassis.x = location.x;
    this.chassis.y = location.y;
    if (typeof rotation !== 'undefined') {
        this.chassis.rotation = rotation.chassis
    }
    stage.addChild(this.chassis);
    this.turret =  new createjs.Shape();
    this.turret.graphics.beginFill(turretColor).drawCircle(0, 0, 10);
    this.turret.graphics.beginFill(turretColor).drawRect(-3, 10, 6, 42);
    this.turret.x = location.x;
    this.turret.y = location.y;
    if (typeof rotation !== 'undefined') {
        this.turret.rotation = rotation.turret
    }
    stage.addChild(this.turret);

    this.move = function(x,y,rotation,turretRotation){
        createjs.Tween.get(this.chassis)
            .to({ x: x, y:y, rotation:rotation }, 100);
        createjs.Tween.get(this.turret)
            .to({ x: x, y:y, rotation:turretRotation }, 100);
    };
    this.kill = function () {
        stage.removeChild(this.chassis);
        stage.removeChild(this.turret);
    };
}
function Bullet(stage, location, id) {
    this.id = id;
    this.bullet = new createjs.Shape();
    this.bullet.graphics.beginFill("#000000").drawCircle(0, 0, 5);
    this.bullet.x = location.x;
    this.bullet.y = location.y;
    stage.addChild(this.bullet);
    this.move = function (x, y) {
        createjs.Tween.get(this.bullet)
            .to({x: x, y: y}, 100);
    };
    this.kill = function () {
        stage.removeChild(this.bullet);
    };
}
function randomFloat(min,max){
    return (Math.random() * max) + min;
}
function explode(stage,location){
    var count=10;
    for (var angle=0; angle<360; angle += Math.round(360/count)){
        var params={};
        var speed=randomFloat(50,100);
        params.speedX = speed * Math.cos(angle * Math.PI / 180.0);
        params.speedY = speed * Math.sin(angle * Math.PI / 180.0);
        params.time = randomFloat(500,600);
        params.size = randomFloat(5,15);
        params.x = location.x + Math.cos(angle * Math.PI / 180.0) * randomFloat(5,10);
        params.y = location.y + Math.sin(angle * Math.PI / 180.0) * randomFloat(5,10);
        params.color = Math.random()>0.5 ? "#525252" : "#FFA318";
        createParticle(stage,params)

    }
    function createParticle(stage, params){
        var speedX=params.speedX;
        var speedY=params.speedY;
        var time=params.time;
        var x=params.x;
        var y=params.y;
        var size=params.size;
        var color=params.color;
        var p=new createjs.Shape();
        p.graphics.beginFill(color).drawCircle(x,y,size);
        stage.addChild(p);
        createjs.Tween.get(p).to({x:x+speedX,y:y+speedY,scaleX:0, scaleY:0},time);
        setTimeout(function () {
            stage.removeChild(p);
        },time);
    }
}

window.onload = function() {
    function init() {
        stage = new createjs.Stage("demoCanvas");
        resizeCanvas();
        createjs.Ticker.setFPS(60);
        createjs.Ticker.addEventListener("tick", stage);
    }
    init();
    $('#myModal').modal('show');
};
function connectWithLogin() {
    connect($("#login")[0].value)
}
function connectWithRandomLogin() {
    connect(makeId())
}
function resizeCanvas() {
    var canvas = stage.canvas;
    canvas.width = $("body").width();
    canvas.height = $("body").height() - 4;
}


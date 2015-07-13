var demoGame = {
    comms : {
        stompClient: null,

        connect: function(rcvMessage, username, game) {
           var socket = new SockJS('/message');
           demoGame.comms.stompClient = Stomp.over(socket);
           demoGame.comms.stompClient.connect({'X-Username':username}, function(frame) {
               console.log('Connected: ' + frame);
               demoGame.comms.stompClient.subscribe('/topic/zombietime_'+game, rcvMessage, {});
           }, function(error){
                alert(error.headers.message);
           });
        },

        disconnect: function() {
            if (demoGame.comms.stompClient != null) {
                demoGame.comms.stompClient.disconnect();
                console.log("Disconnected");
            }
        },

        sendMessage: function(username, game, text) {
            demoGame.comms.stompClient.send("/app/message", {}, JSON.stringify({'game':game, 'action': text, 'x':0, 'y':1}));
        }
    },
    /////////////////////////////////////////////////////////////////////////
    gui : {
        username: "",
        game:"",
        login: function(){
            demoGame.gui.username = $("#username").val();
            demoGame.gui.game = $("#game").val();
            $("#login").hide();
            $("#chat").show();
            demoGame.comms.connect(demoGame.gui.receiveMessage, demoGame.gui.username,demoGame.gui.game);
        },

        receiveMessage: function(message) {
            var jsonMsg = JSON.parse(message.body);
            demoGame.gui.showMessage(jsonMsg.game, jsonMsg.action);
        },

        sendMessage: function(){
            var text = $("#text").val();
            $("#text").val("");
            demoGame.comms.sendMessage(demoGame.gui.username, demoGame.gui.game, text);
        },

        showMessage: function(game, action){
            var message;

            message = $("<div class='message right'><span>[" + game + "] - " + action +"&nbsp;</span><span></div>");

            $("#history").append(message);
            $("#history").scrollTop ($("#history")[0].scrollHeight);
        }
    }
}


$( document ).ready(function() {
    $("#loginForm").submit(function( event ) {
        event.preventDefault();
        demoGame.gui.login();
    });

    $("#messageForm").submit(function( event ) {
        event.preventDefault();
        demoGame.gui.sendMessage();
    });
});
var demoGame = {
    comms : {
        stompClient: null,

        connect: function(rcvMessage, username, game, password) {
           var socket = new SockJS('/message');
           demoGame.comms.stompClient = Stomp.over(socket);
           var headers;
           demoGame.comms.stompClient.connect({'x-username':username, 'x-password':password}, function(frame) {
               console.log('Connected: ' + frame);
               demoGame.comms.stompClient.subscribe('/topic/zombietime_'+game, rcvMessage, {});
           }, function(error){
                if (error.headers !== undefined) {
                    alert(error.headers.message);
                } else {
                    alert(errors);
                }
           });
        },

        disconnect: function() {
            if (demoGame.comms.stompClient != null) {
                demoGame.comms.stompClient.disconnect();
                console.log("Disconnected");
            }
        },

        sendMessage: function(username, game, text) {
            demoGame.comms.stompClient.send("/topic/zombietime_"+game, {}, JSON.stringify({'game':game, 'action': text, 'x':0, 'y':1}));
        }
    },
    /////////////////////////////////////////////////////////////////////////
    gui : {
        username: "",
        game:"",
        password:"",
        login: function(){
            demoGame.gui.username = $("#username").val();
            demoGame.gui.game = $("#gameId").val();
            demoGame.gui.password = $("#password").val();
            $("#chat").show();
            demoGame.comms.connect(demoGame.gui.receiveMessage, demoGame.gui.username,demoGame.gui.game, demoGame.gui.password);
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

    $("#messageForm").submit(function( event ) {
        event.preventDefault();
        demoGame.gui.sendMessage();
    });
    demoGame.gui.login();

});
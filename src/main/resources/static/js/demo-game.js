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

        sendMessage: function(type, username, game, data) {
            demoGame.comms.stompClient.send("/app/message", {}, JSON.stringify({'game':game, 'type':type, 'data': data}));
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
            demoGame.gui.showMessage(jsonMsg.type, jsonMsg.user, jsonMsg.data);
        },

        sendMessage: function(){
            var data = {};

            var name;
            var value;

            var type = $("#type").val();

            name = $("#name1").val();
            value = $("#value1").val();

            if (name !== "") {
                data[name] = value
            }

            name = $("#name2").val();
            value = $("#value2").val();

            if (name !== "") {
                data[name] = value
            }

            name = $("#name3").val();
            value = $("#value3").val();

            if (name !== "") {
                data[name] = value
            }

            name = $("#name4").val();
            value = $("#value4").val();

            if (name !== "") {
                data[name] = value
            }

            name = $("#name5").val();
            value = $("#value5").val();

            if (name !== "") {
                data[name] = value
            }



            $("#name1").val("");
            $("#value1").val("");
            $("#name2").val("");
            $("#value2").val("");
            $("#name3").val("");
            $("#value3").val("");
            $("#name4").val("");
            $("#value4").val("");
            $("#name5").val("");
            $("#value5").val("");

            demoGame.comms.sendMessage(type, demoGame.gui.username, demoGame.gui.game, data);
        },

        showMessage: function(type, user, data){
            var message;
            var info;
            if (type == 'CHAT') {
                info = data.text;
            }

            message = $("<div class='message right'><span>[" + type + "] - &lt;" + user + "&gt; - " + info +"&nbsp;</span><span></div>");

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
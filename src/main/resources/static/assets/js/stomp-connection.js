/*global $, SockJS, Stomp, conf, utils */

"use strict";

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var StompConnection = (function () {
  function StompConnection() {
    _classCallCheck(this, StompConnection);

    var qp = utils.getQueryParams();

    this.isConnected = false;
    if (qp && qp.username && qp.password && qp.game) this.connect(qp.username, qp.password, qp.game);
  }

  _createClass(StompConnection, [{
    key: "connect",
    value: function connect(username, password, game) {
      var _this = this;

      var headers = {
        "x-username": username,
        "x-password": password
      },
          onConnect = function onConnect(frame) {
        _this.isConnected = true;
        _this.client.subscribe("/topic/zombietime_" + game, _this.onMessage, {});
      },
          onError = function onError(error) {
        _this.isConnected = false;
        console.log('======================================');
        console.log('= ERROR IN STOMP CONNECTION');
        console.log(error);
        console.log('======================================');
      };

      this.game = game;
      this.socket = new SockJS(conf.websocketsUrl + '/message');
      this.client = Stomp.over(this.socket);
      this.client.connect(headers, onConnect, onError);
    }
  }, {
    key: "getWebsocketClient",
    value: function getWebsocketClient() {
      this.client = Stomp.over(this.socket);
    }
  }, {
    key: "onMessage",
    value: function onMessage(message) {
      $(window).trigger("message.stomp.zt", JSON.parse(message.body));
    }
  }, {
    key: "sendMessage",
    value: function sendMessage(type, data) {
      var message = {
        "game": this.game,
        "type": type,
        "data": data
      },
          jsonMessage = JSON.stringify(message);

      this.client.send("/app/message", {}, jsonMessage);
    }
  }]);

  return StompConnection;
})();
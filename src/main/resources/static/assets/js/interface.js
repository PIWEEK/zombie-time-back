"use strict";

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Interface = (function () {
  function Interface() {
    _classCallCheck(this, Interface);

    this.els = document.querySelectorAll(".interface");
    this.clock = document.querySelector("#top-right-interface.clock");

    this.registerEvents();
  }

  _createClass(Interface, [{
    key: "hide",
    value: function hide() {
      var hideFn = function hideFn(el) {
        return el.style.display = "none";
      };

      R.forEach(hideFn, this.els);
    }
  }, {
    key: "show",
    value: function show() {
      var showFn = function showFn(el) {
        return el.style.display = "block";
      };

      R.forEach(showFn, this.els);
    }
  }, {
    key: "registerEvents",
    value: function registerEvents() {
      var w = $(window),
          menuElements = [["#user-profile.menu-element", "showProfile"], ["#inventory.menu-element", "showInventory"], ["#attack-button.menu-element", "attack"], ["#move-button.menu-element", "move"], ["#search-button.menu-element", "search"], ["#noise-button.menu-element", "noise"], ["#chat-button.menu-element", "chat"], ["#end-turn-button.menu-element", "endTurn"]],
          addClickListener = function addClickListener(el) {
        document.querySelector(R.head(el)).addEventListener("click", function () {
          w.trigger("buttonClick.interface.zt", R.last(el));
        });
      };

      R.map(addClickListener, menuElements);

      document.querySelector(".chat-form").addEventListener("submit", function (e) {
        e.preventDefault();
        w.trigger("sendChat.interface.zt");
      });

      document.querySelector("#log").addEventListener("click", function () {
        w.trigger("toggleLog.interface.zt");
      });

      document.querySelector("#choose-character .ready").addEventListener("click", function () {
        w.trigger("buttonClick.ready.zt");
      });

      document.querySelector("#inventory-info").addEventListener("mouseleave", function () {
        document.querySelector("#inventory-info").style.visibility = "hidden";
      });

      document.querySelector("#user-profile").addEventListener("mouseenter", function () {
        document.querySelector("#inventory-info").style.visibility = "hidden";
      });

      document.querySelector("#attack-button").addEventListener("mouseenter", function () {
        document.querySelector("#inventory-info").style.visibility = "hidden";
      });

      document.querySelector("#log").addEventListener("mouseenter", function () {
        document.querySelector("#inventory-info").style.visibility = "hidden";
      });

      document.querySelector(".leader .team-photo").addEventListener("drop", function (ev) {
        var slug = ev.dataTransfer.getData("slug");
        w.trigger("drop.interface.zt", { "slug": slug, "leader": true });
      });

      document.querySelector(".follower .team-photo").addEventListener("drop", function (ev) {
        var slug = ev.dataTransfer.getData("slug");
        w.trigger("drop.interface.zt", { "slug": slug, "leader": false });
      });

      document.querySelector(".leader .team-photo").addEventListener("dragover", function (ev) {
        ev.preventDefault();
      });

      document.querySelector(".follower .team-photo").addEventListener("dragover", function (ev) {
        ev.preventDefault();
      });

      Mousetrap.bind("m", function () {
        w.trigger("buttonClick.interface.zt", "move");
      }, "keydown");

      Mousetrap.bind("a", function () {
        w.trigger("buttonClick.interface.zt", "attack");
      }, "keydown");

      Mousetrap.bind("s", function () {
        w.trigger("buttonClick.interface.zt", "search");
      }, "keydown");

      Mousetrap.bind("n", function () {
        w.trigger("buttonClick.interface.zt", "noise");
      }, "keydown");

      Mousetrap.bind("c", function () {
        w.trigger("buttonClick.interface.zt", "chat");
      }, "keydown");
    }
  }]);

  return Interface;
})();
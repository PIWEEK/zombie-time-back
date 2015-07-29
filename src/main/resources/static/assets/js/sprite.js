/*global utils */

"use strict";

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Sprite = (function () {
  function Sprite(url, imageWidth, imageHeight, sizeX, sizeY) {
    var _this = this;

    _classCallCheck(this, Sprite);

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.sizeX = sizeX;
    this.sizeY = sizeY;

    this.loadPromise = utils.loadImage(url);
    this.loadPromise.then(function (image) {
      _this.image = image;
    });
  }

  _createClass(Sprite, [{
    key: "getImageCoords",
    value: function getImageCoords(position) {
      return utils.getCellCoords(position, this.sizeX, this.sizeY);
    }
  }]);

  return Sprite;
})();
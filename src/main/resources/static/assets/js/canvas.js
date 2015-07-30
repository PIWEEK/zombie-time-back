/*global $, utils, R, Image, Transform, Sprite, conf, Mousetrap */

"use strict";

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Canvas = (function () {
  function Canvas() {
    _classCallCheck(this, Canvas);

    this.zombieTime = false;
    this.gameSprite = new Sprite(conf.serverUrl + "/assets/imgs/tile.png", conf.tileWidth, conf.tileHeight, conf.spriteSizeX, conf.spriteSizeY);
    var el = document.createElement("canvas");
    el.id = "mainCanvas";
    document.querySelector('#content').appendChild(el);
    this.el = el;
    this.ctx = el.getContext("2d");
    this.currentScale = 1;
    this.grid = {};
    this.map = {};
    this.transform = new Transform();

    this.registerEvents();
  }

  _createClass(Canvas, [{
    key: "resize",
    value: function resize() {
      var viewportSize = utils.getViewportSize(),
          el = this.el;

      el.width = viewportSize.width;
      el.height = viewportSize.height;
      this.redraw();
    }
  }, {
    key: "redraw",
    value: function redraw() {
      var _this = this;

      var drawCell = function drawCell(cellNumber, cellContent) {
        var cellOccupation = R.prop(cellNumber, _this.gridOccupation),
            getTypeOccupation = function getTypeOccupation(type) {
          if (cellOccupation === undefined) {
            return 0;
          } else {
            if (cellOccupation[type] === undefined) {
              return 0;
            } else {
              return cellOccupation[type];
            }
          }
        },
            zombieOccupation = getTypeOccupation("zombies"),
            survivorOccupation = getTypeOccupation("survivors"),
            totalOccupation = zombieOccupation + survivorOccupation,
            drawBackgroundInCell = R.curry(_this.drawBackground.bind(_this))(R.__, cellNumber),
            drawZombieInCell = R.curry(_this.drawCharacter.bind(_this))(R.__, cellNumber, "zombie", R.__, zombieOccupation, totalOccupation),
            drawSurvivorInCell = R.curry(_this.drawCharacter.bind(_this))(R.__, cellNumber, "survivor", R.__, survivorOccupation, totalOccupation);

        drawBackgroundInCell(cellContent.floor);
        drawBackgroundInCell(cellContent.wall);
        drawBackgroundInCell(cellContent.item);
        if (cellContent.zombies) {
          R.forEachIndexed(function (val, idx, list) {
            return drawZombieInCell(val.avatar, idx);
          }, cellContent.zombies);
        }
        if (cellContent.survivors) {
          R.forEachIndexed(function (val, idx, list) {
            return drawSurvivorInCell(val.avatar, idx);
          }, cellContent.survivors);
        }
        if (cellContent.noise) {
          _this.drawNoise(cellNumber, cellContent.noise);
        }

        if (cellContent.searchPoint) {
          _this.drawSearchPoint(cellNumber);
        }

        if (cellContent.victoryCondition) {
          _this.drawVictoryConditionPoint(cellNumber);
        }
      };

      var viewportSize = utils.getViewportSize();

      this.transform.scale(this.currentScale, this.currentScale);
      this.applyTransform();

      this.gameSprite.loadPromise.then(function () {
        _this.ctx.clearRect(0, 0, _this.map.sizeX * conf.tileWidth, _this.map.sizeY * conf.tileHeight);
        R.forEach(function (el) {
          return drawCell(el[0], el[1]);
        }, R.toPairs(_this.grid));
        if (_this.currentAction == "move") {
          _this.ctx.globalAlpha = 0.4;
          _this.ctx.fillStyle = "#3333FF";
          R.forEach(_this.drawRectangle.bind(_this), _this.player.canMoveTo);
          _this.ctx.globalAlpha = 1;
          _this.ctx.fillStyle = "#000000";
        } else if (_this.currentAction == "attack") {
          _this.ctx.globalAlpha = 0.4;
          _this.ctx.fillStyle = "#FF0000";
          R.forEach(_this.drawRectangle.bind(_this), _this.player.canAttackTo);
          _this.ctx.globalAlpha = 1;
          _this.ctx.fillStyle = "#000000";
        }
      });
    }
  }, {
    key: "getCellCoords",
    value: function getCellCoords(position) {
      return utils.getCellCoords(position, this.map.sizeX, this.map.sizeY);
    }
  }, {
    key: "drawRectangle",
    value: function drawRectangle(cellPos) {
      var cellCoords = this.getCellCoords(cellPos);

      this.ctx.fillRect(cellCoords.x * conf.tileWidth, cellCoords.y * conf.tileWidth, conf.tileWidth, conf.tileHeight);
    }
  }, {
    key: "drawBackground",
    value: function drawBackground(spritePos, cellPos) {
      var spriteCoords = this.gameSprite.getImageCoords(spritePos),
          sx = spriteCoords.x * this.gameSprite.imageWidth,
          sy = spriteCoords.y * this.gameSprite.imageHeight,
          cellCoords = this.getCellCoords(cellPos),
          dx = cellCoords.x * conf.tileWidth,
          dy = cellCoords.y * conf.tileHeight;

      this.ctx.drawImage(this.gameSprite.image, sx, sy, this.gameSprite.imageWidth, this.gameSprite.imageHeight, dx, dy, conf.tileWidth, conf.tileHeight);
    }
  }, {
    key: "drawNoise",
    value: function drawNoise(cellPos, noiseLevel) {
      var _this2 = this;

      var cellCoords = this.getCellCoords(cellPos),
          dx = cellCoords.x * conf.tileWidth,
          dy = cellCoords.y * conf.tileHeight;

      utils.loadImage(conf.serverUrl + "/assets/imgs/botonruidocasilla.png").then(function (image) {
        _this2.ctx.drawImage(image, 0, 0, 64, 64, dx, dy, 64, 64);

        _this2.ctx.fillStyle = "#FFFFFF";
        _this2.ctx.font = "35px Dead";
        _this2.ctx.fillText(noiseLevel, dx + 20, dy + 47);
        _this2.ctx.fillStyle = "#000000";
      });
    }
  }, {
    key: "drawVictoryConditionPoint",
    value: function drawVictoryConditionPoint(cellPos) {
      this.drawBackground(297, cellPos);
    }
  }, {
    key: "drawSearchPoint",
    value: function drawSearchPoint(cellPos) {
      this.drawBackground(296, cellPos);
    }
  }, {
    key: "drawCharacter",
    value: function drawCharacter(spritePos, cellPos, type, number, typeOccupation, totalOccupation) {
      if (this.zombieTime && type === "zombie") {
        var specialZombiePositions = [1, 2, 3, 4, 5],
            random = function random(limit) {
          return Math.floor(Math.random() * limit);
        },
            randomZombieSpritePos = specialZombiePositions[random(specialZombiePositions.length)];

        spritePos = randomZombieSpritePos;
      }

      var spriteCoords = this.gameSprite.getImageCoords(spritePos),
          sx = spriteCoords.x * this.gameSprite.imageWidth,
          sy = spriteCoords.y * this.gameSprite.imageHeight,
          cellCoords = this.getCellCoords(cellPos),
          dx = cellCoords.x * conf.tileWidth,
          dy = cellCoords.y * conf.tileHeight + conf.tileHeight / 4,
          halfTileWidth = conf.tileWidth / 2,
          halfTileHeight = conf.tileHeight / 2;

      if (typeOccupation < totalOccupation) {
        if (type === "zombie") {
          this.ctx.drawImage(this.gameSprite.image, sx, sy, this.gameSprite.imageWidth, this.gameSprite.imageHeight, dx, dy, halfTileWidth, halfTileHeight);

          if (typeOccupation > 1) {
            var radius = conf.tileWidth / 8,
                x = dx + conf.tileWidth / 2 - 20,
                y = dy + conf.tileWidth / 2 - 20,
                startAngle = 0,
                endingAngle = 2 * Math.PI;

            // contorno del círculo 800000
            this.ctx.beginPath();
            this.ctx.arc(x, y, radius, startAngle, endingAngle);
            // Contorno
            this.ctx.lineWidth = 5;
            this.ctx.fillStyle = "#800000";
            this.ctx.stroke();
            this.ctx.lineWidth = 1;
            // Interior
            this.ctx.fillStyle = "#F5D8D8";
            this.ctx.fill();
            // Texto
            this.ctx.fillStyle = "#7E0101";
            this.ctx.font = "55px Dead";
            this.ctx.fillText(typeOccupation, x - conf.tileWidth / 16 + 5, y + conf.tileWidth / 16 + 3);
            this.ctx.fillStyle = "#000000";
          }
        } else {
          // antes de pintar, typeOccupation nos da un shift hacia arriba que tenemos que pintar teniendo en cuenta number
          var shift = 25 * number;

          this.ctx.drawImage(this.gameSprite.image, sx, sy, this.gameSprite.imageWidth, this.gameSprite.imageHeight, dx + conf.tileWidth / 2, dy - shift, halfTileWidth, halfTileHeight);
        }
      } else {
        if (type === "zombie") {
          this.ctx.drawImage(this.gameSprite.image, sx, sy, this.gameSprite.imageWidth, this.gameSprite.imageHeight, dx + conf.tileWidth / 4, dy, halfTileWidth, halfTileHeight);

          if (typeOccupation > 1) {
            var radius = conf.tileWidth / 8,
                x = dx + conf.tileWidth / 4 * 3 - 20,
                y = dy + conf.tileWidth / 2 - 20,
                startAngle = 0,
                endingAngle = 2 * Math.PI;

            // contorno del círculo 800000
            this.ctx.beginPath();
            this.ctx.arc(x, y, radius, startAngle, endingAngle);
            // Contorno
            this.ctx.lineWidth = 5;
            this.ctx.fillStyle = "#800000";
            this.ctx.stroke();
            this.ctx.lineWidth = 1;
            // Interior
            this.ctx.fillStyle = "#F5D8D8";
            this.ctx.fill();
            // Texto
            this.ctx.fillStyle = "#7E0101";
            this.ctx.font = "55px Dead";
            this.ctx.fillText(typeOccupation, x - conf.tileWidth / 16 + 5, y + conf.tileWidth / 16 + 3);
            this.ctx.fillStyle = "#000000";
          }
        } else {
          var shift = 25 * number;

          this.ctx.drawImage(this.gameSprite.image, sx, sy, this.gameSprite.imageWidth, this.gameSprite.imageHeight, dx + conf.tileWidth / 4, dy - shift, halfTileWidth, halfTileHeight);
        }
      }
    }
  }, {
    key: "zoomIn",
    value: function zoomIn(delta) {
      var signedDelta = delta ? delta : conf.defaultZoomIncrement,
          viewportSize = utils.getViewportSize(),
          currentZoom = this.transform.m[0],
          futureZoom = currentZoom + currentZoom * signedDelta,
          futureTilesWidth = conf.tileWidth * conf.maxTilesWhenZoomIn * futureZoom,
          canZoomIn = futureTilesWidth < viewportSize.width;

      if (canZoomIn) {
        this.scale(signedDelta);
      } else {
        this.zoomInToMax();
      }
    }
  }, {
    key: "zoomOut",
    value: function zoomOut(delta) {
      var signedDelta = delta ? delta * -1 : conf.defaultZoomIncrement * -1,
          viewportSize = utils.getViewportSize(),
          currentZoom = this.transform.m[0],
          futureZoom = currentZoom + currentZoom * signedDelta,
          futureBackgroundWidth = conf.tileWidth * this.map.sizeX * futureZoom,
          canZoomOut = futureBackgroundWidth > viewportSize.width;

      if (canZoomOut) {
        this.scale(signedDelta);
      } else {
        this.zoomOutToMax();
      }
    }
  }, {
    key: "zoomInToMax",
    value: function zoomInToMax() {
      var tilesWidth = conf.tileWidth * conf.maxTilesWhenZoomIn,
          viewportSize = utils.getViewportSize(),
          zoomToApply = viewportSize.width / tilesWidth,
          currentZoom = this.transform.m[0],
          scaleToApply = (zoomToApply - currentZoom) / currentZoom;

      this.scale(scaleToApply);
    }
  }, {
    key: "zoomOutToMax",
    value: function zoomOutToMax() {
      var viewportSize = utils.getViewportSize(),
          totalBackgroundWidth = conf.tileWidth * this.map.sizeX,
          currentZoom = this.transform.m[0],
          zoomToApply = viewportSize.width / totalBackgroundWidth,
          scaleToApply = (zoomToApply - currentZoom) / currentZoom;

      this.scale(scaleToApply);
    }
  }, {
    key: "zoomReset",
    value: function zoomReset() {
      this.transform.reset();
      this.applyTransform();
      this.redraw();
    }
  }, {
    key: "scale",
    value: function scale(_scale) {
      this.currentScale += _scale;
      this.redraw();
      this.currentScale = 1;
    }
  }, {
    key: "translate",
    value: function translate(x, y) {
      this.transform.translate(x, y);
      this.applyTransform();
    }
  }, {
    key: "applyTransform",
    value: function applyTransform() {
      var m = this.transform.m,
          viewportSize = utils.getViewportSize(),
          widthLimit = (conf.tileWidth * this.map.sizeX * m[0] - viewportSize.width) * -1,
          heightLimit = (conf.tileHeight * this.map.sizeY * m[3] - viewportSize.height) * -1;

      if (m[4] > 0) {
        m[4] = 0;
      } else if (m[4] < widthLimit) {
        m[4] = widthLimit;
      }

      if (m[5] > 0) {
        m[5] = 0;
      } else if (m[5] < heightLimit) {
        m[5] = heightLimit;
      }

      this.ctx.setTransform(m[0], m[1], m[2], m[3], m[4], m[5]);
    }
  }, {
    key: "getRelativeMouseCoords",
    value: function getRelativeMouseCoords(x, y) {
      var m = this.transform.m,
          relX = (Math.abs(m[4]) + x) / m[0],
          relY = (Math.abs(m[5]) + y) / m[3];

      return { x: relX, y: relY };
    }
  }, {
    key: "registerEvents",
    value: function registerEvents() {
      var _this3 = this;

      /**********************************************
       * Window events
       **********************************************/
      window.onresize = function () {
        return _this3.resize();
      };

      /**********************************************
       * Keybindings
       **********************************************/
      Mousetrap.bind('i', function () {
        _this3.zoomIn(0.4);
      });

      Mousetrap.bind('o', function () {
        _this3.zoomOut(0.4);
      });

      Mousetrap.bind('r', function () {
        _this3.zoomReset();
      });

      /**********************************************
       * Mouse scroll
       **********************************************/
      this.el.onwheel = function (e) {
        if (e.deltaY >= 0) {
          _this3.zoomOut();
        } else {
          _this3.zoomIn();
        }
      };

      /**********************************************
       * Drag and Drop
       **********************************************/
      this.el.addEventListener("mousedown", function (e) {
        _this3.drag = {
          x: e.x,
          y: e.y,
          initialX: e.x,
          initialY: e.y
        };
      });

      this.el.addEventListener("mousemove", function (e) {
        if (_this3.drag) {
          var deltaX = e.x - _this3.drag.x,
              deltaY = e.y - _this3.drag.y;

          _this3.drag.x = e.x;
          _this3.drag.y = e.y;

          _this3.translate(deltaX, deltaY);
          _this3.redraw();
        }
      });

      var isClick = function isClick(x, y, drag) {
        var d = conf.clickPixelDelta;

        return drag.initialX + d >= x && drag.initialX - d <= x && drag.initialY + d >= y && drag.initialY - d <= y;
      };

      this.el.addEventListener("mouseup", function (e) {
        if (isClick(e.x, e.y, _this3.drag)) {
          var w = $(window),
              mouseCoords = _this3.getRelativeMouseCoords(e.x, e.y),
              clickedCell = utils.getCellForCoords(mouseCoords.x, mouseCoords.y, _this3.map.sizeX);

          w.trigger("cellClick.canvas.zt", clickedCell);
        }

        _this3.drag = undefined;
      });

      this.el.addEventListener("mouseleave", function (e) {
        _this3.drag = undefined;
      });
    }
  }]);

  return Canvas;
})();
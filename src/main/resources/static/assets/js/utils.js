/*global Image, location, conf */

"use strict";

var utils = {
  getViewportSize: function getViewportSize() {
    return { width: window.innerWidth, height: window.innerHeight };
  },
  loadImage: function loadImage(imageUrl) {
    return new Promise(function (resolve, reject) {
      var image = new Image();
      image.onload = function () {
        return resolve(image);
      };
      image.src = imageUrl;
    });
  },
  getCellCoords: function getCellCoords(position, sizeX, sizeY) {
    var cellFloor = position % sizeX,
        cellModule = Math.floor(position / sizeX);

    return { x: cellFloor, y: cellModule };
  },
  getCellForCoords: function getCellForCoords(x, y, sizeX) {
    var posX = Math.floor(x / conf.tileWidth),
        posY = Math.floor(y / conf.tileHeight);

    return posY * sizeX + posX;
  },
  getQueryParams: function getQueryParams() {
    var searchString = location.search;

    if (searchString === "") {
      return undefined;
    }

    if (searchString[0] == "?") {
      searchString = searchString.slice(1, searchString.length);
    }

    var pairs = R.split("&", searchString),
        queryParams = {},
        processPair = function processPair(pairString) {
      var pair = R.split("=", pairString);

      queryParams[R.head(pair)] = R.last(pair);
    };

    R.map(processPair, pairs);

    return queryParams;
  }
};
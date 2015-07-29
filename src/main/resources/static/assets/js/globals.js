/*global Http */

"use strict";

var conf = {
  tileWidth: 256,
  tileHeight: 256,
  spriteSizeX: 16,
  spriteSizeY: 22,
  maxTilesWhenZoomIn: 4,
  defaultZoomIncrement: 0.07,
  clickPixelDelta: 5,
  serverUrl: "",
  websocketsUrl: ""
};

var http = new Http(conf.serverUrl);
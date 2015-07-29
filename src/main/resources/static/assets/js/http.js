/*global $, R */

'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var Http = (function () {
  function Http(url) {
    _classCallCheck(this, Http);

    this.url = url;
  }

  _createClass(Http, [{
    key: 'get',
    value: function get(url, args) {
      var _this = this;

      return new Promise(function (resolve, reject) {
        $.get(_this.url + url + _this.getUrlParams(args)).fail(function (jqXHR) {
          reject(jqXHR);
        }).done(function (data, textStatus, jqXHR) {
          resolve(data);
        });
      });
    }
  }, {
    key: 'post',
    value: function post(url, data) {
      var _this2 = this;

      return new Promise(function (resolve, reject) {
        $.post(_this2.url + url, data).fail(function (jqHXR) {
          reject(jqHXR);
        }).done(function (data, textStatus, jqHXR) {
          resolve(data);
        });
      });
    }
  }, {
    key: 'getUrlParams',
    value: function getUrlParams(args) {
      var filtersToApply = R.toPairs(args),
          appliedFilters = R.map(R.join('='), filtersToApply),
          concatenatedFilters = R.join('&', appliedFilters),
          concatIfNotEmpty = R.ifElse(R.isEmpty, R.always(''), R.concat('?'));

      return concatIfNotEmpty(concatenatedFilters);
    }
  }]);

  return Http;
})();
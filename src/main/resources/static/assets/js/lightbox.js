'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var Lightbox = (function () {
  function Lightbox() {
    _classCallCheck(this, Lightbox);

    this.lb = document.querySelector(".lightbox");
    this.lbs = document.querySelectorAll('.inner-lb');
    this.close = document.querySelector('#close-lb');
  }

  _createClass(Lightbox, [{
    key: 'hideAll',
    value: function hideAll() {
      var hideFn = function hideFn(el) {
        return el.style.display = "none";
      };
      R.forEach(hideFn, R.concat(this.lbs, R.concat([this.lb], [this.close])));
      document.querySelector('.veil').style.display = "none";
    }
  }, {
    key: 'show',
    value: function show(id) {
      this.lb.style.display = "block";
      this.close.style.display = "block";
      document.querySelector('' + id).style.display = "block";
      document.querySelector('.veil').style.display = "block";
    }
  }]);

  return Lightbox;
})();
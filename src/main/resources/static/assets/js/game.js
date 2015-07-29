/*global $, R, StompConnection, Interface, Sprite, Canvas, conf, game, moment, utils */

'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var Game = (function () {
  function Game() {
    _classCallCheck(this, Game);

    this.grid = {};
    this.stomp = new StompConnection();
    this.registerEventHandlers();
    this.initialized = false;
    this['interface'] = new Interface();
    this.lightbox = new Lightbox();
    this.currentAction = undefined;
    this.myTurn = false;
  }

  _createClass(Game, [{
    key: 'initialize',
    value: function initialize(gameInfo) {
      this.initialized = true;
      this.parseGameInfo(gameInfo);
      this.canvas = new Canvas();
      this.canvas.map = this.map;
      this.canvas.grid = this.grid;
      this.canvas.resize();

      this.parseGameInfo(gameInfo);
    }
  }, {
    key: 'startMusic',
    value: function startMusic(music) {
      if (document.querySelector('#zt-music-survivor').paused) {
        this.playMusic('select-survivor');
      }
    }
  }, {
    key: 'playMusic',
    value: function playMusic(music) {
      console.log(music);
      document.querySelector('#zt-music').pause();
      document.querySelector('#zt-music-survivor').pause();
      document.querySelector('#zt-music-end').pause();

      if (music === 'select-survivor') {
        document.querySelector('#zt-music-survivor').play();
        document.querySelector('#zt-music-survivor').loop = true;
      }
      if (music === 'game') {
        document.querySelector('#zt-music').play();
        document.querySelector('#zt-music').loop = true;
      }
      if (music === 'end-game') {
        document.querySelector('#zt-music-end').play();
        document.querySelector('#zt-music-end').loop = true;
      }
    }
  }, {
    key: 'getGridOccupation',
    value: function getGridOccupation(data) {
      var gridOccupation = {},
          includePointsOnList = function includePointsOnList(list, points) {
        var addToPoint = function addToPoint(point) {
          if (gridOccupation[point] === undefined) {
            gridOccupation[point] = {};
          }
          gridOccupation[point][list] = gridOccupation[point][list] === undefined ? 1 : gridOccupation[point][list] + 1;
        };

        R.forEach(addToPoint, points);
      },
          survivorPoints = includePointsOnList('survivors', R.map(R.prop('point'), data.survivors)),
          zombiePoints = includePointsOnList('zombies', R.map(R.prop('point'), data.zombies));

      return gridOccupation;
    }
  }, {
    key: 'parseGameInfo',
    value: function parseGameInfo(gameInfo) {
      var _this = this;

      var username = utils.getQueryParams().username,
          getLeader = R.find(R.propEq('leader', true)),
          isFromPlayer = R.filter(R.propEq('player', username)),
          getPlayer = R.compose(getLeader, isFromPlayer);
      var processSimpleLayer = function processSimpleLayer(layer, val, idx, list) {
        var shiftedVal = val - 1;

        if (_this.grid[idx] == undefined) {
          _this.grid[idx] = {};
        }
        _this.grid[idx][layer] = shiftedVal;
      };
      var processSimpleLayerCurried = R.curry(processSimpleLayer),
          processFloor = processSimpleLayerCurried('floor', R.__, R.__, R.__),
          processWall = processSimpleLayerCurried('wall', R.__, R.__, R.__),
          processItem = processSimpleLayerCurried('item', R.__, R.__, R.__);

      var processComplexLayer = function processComplexLayer(layer, val, idx, list) {
        var position = val.point,
            getShiftedVal = function getShiftedVal(val) {
          val.avatar -= 1;
          return val;
        },
            shiftedVal = getShiftedVal(val);

        if (position !== -1) {
          if (_this.grid[position][layer]) {
            _this.grid[position][layer].push(shiftedVal);
          } else {
            _this.grid[position][layer] = [shiftedVal];
          }
        }
      };
      var processComplexLayerCurried = R.curry(processComplexLayer),
          processSurvivors = processComplexLayerCurried('survivors', R.__, R.__, R.__),
          processZombies = processComplexLayerCurried('zombies', R.__, R.__, R.__);
      var processNoise = function processNoise(noiseCell) {
        _this.grid[noiseCell.point]['noise'] = _this.grid[noiseCell.point]['noise'] === undefined ? noiseCell.level : _this.grid[noiseCell.point]['noise'] + noiseCell.level;
      };

      var processSearchPoints = function processSearchPoints(searchPointCell) {
        _this.grid[searchPointCell.point]['searchPoint'] = true;
      };

      var processVictoryConditions = function processVictoryConditions(victoryConditionCell) {
        _this.grid[victoryConditionCell.point]['victoryCondition'] = true;
      };

      this.grid = {};
      this.interval = gameInfo.zombieTimeInterval;
      this.map = {
        sizeX: gameInfo.data.map.width,
        sizeY: gameInfo.data.map.height
      };
      this.victoryConditions = gameInfo.data.victoryConditions;
      this.missions = gameInfo.data.missions;
      this.catchedSurvivors = gameInfo.data.catchedSurvivors;

      R.forEachIndexed(processFloor, gameInfo.data.map.floorTiles);
      R.forEachIndexed(processWall, gameInfo.data.map.wallTiles);
      R.forEachIndexed(processItem, gameInfo.data.map.itemTiles);
      R.forEach(processNoise, gameInfo.data.noise);
      R.forEachIndexed(processSurvivors, gameInfo.data.survivors);
      R.forEachIndexed(processZombies, gameInfo.data.zombies);

      R.forEach(processSearchPoints, gameInfo.data.searchPoints);
      R.forEach(processVictoryConditions, gameInfo.data.victoryConditions);

      this.player = getPlayer(gameInfo.data.survivors);
      if (this.player) {
        this.myTurn = gameInfo.data.playerTurn === this.player.player;
        document.querySelector('#user-profile img').src = conf.serverUrl + '/assets/imgs/survivors/' + this.player.slug + '.png';
      }
      this.survivors = gameInfo.data.survivors;
      this.gridOccupation = this.getGridOccupation(gameInfo.data);

      if (this.canvas !== undefined) {
        this.canvas.grid = this.grid;
        this.canvas.player = getPlayer(gameInfo.data.survivors);
        this.canvas.gridOccupation = this.getGridOccupation(gameInfo.data);
      };

      if (this.player !== undefined) {

        if (this.player.inventory !== undefined) {
          this.drawInventory(this.player, this.player.inventory);
        }

        console.log(this.player.currentLife);
        if (this.player.currentLife !== undefined) {
          console.log('OK', this.player.currentLife);
          this.drawLife(this.player.currentLife);
        }

        if (this.player.currentActions !== undefined) {
          this.drawActions(this.player.currentActions);
        }

        if (this.player.weapon !== undefined) {
          this.drawWeapon(this.player.weapon.slug);
          this.drawAmmo(parseInt(this.player.weapon.currentAmmo), this.player.weapon.longRange);
          this.drawDamage(parseInt(this.player.weapon.damage));
        }

        this.enableActionsButtons(this.player);
      }
    }
  }, {
    key: 'enableActionsButtons',
    value: function enableActionsButtons(player) {

      if (this.player.currentActions === undefined || parseInt(this.player.currentActions) === 0) {
        $('#move-button').addClass('nouse');
        $('#attack-button').addClass('nouse');
        $('#search-button').addClass('nouse');
        $('#noise-button').addClass('nouse');
      } else {
        $('#noise-button').removeClass('nouse');
        if (this.player.canMoveTo.length === 0) {
          $('#move-button').addClass('nouse');
        } else {
          $('#move-button').removeClass('nouse');
        }

        if (this.player.canAttackTo.length === 0) {
          $('#attack-button').addClass('nouse');
        } else {
          $('#attack-button').removeClass('nouse');
        }

        if (this.player.canSearch) {
          $('#search-button').removeClass('nouse');
        } else {
          $('#search-button').addClass('nouse');
        }
      }
    }
  }, {
    key: 'drawLife',
    value: function drawLife(life) {
      $('#user-profile .life .text').text(life);
    }
  }, {
    key: 'drawActions',
    value: function drawActions(actions) {
      $('#end-turn-button .actions').text(actions);
    }
  }, {
    key: 'drawWeapon',
    value: function drawWeapon(weapon) {
      $('#attack-button').css('background-image', 'url(/assets/imgs/' + weapon + '.png)');
    }
  }, {
    key: 'drawAmmo',
    value: function drawAmmo(ammo, longRange) {
      $('#attack-button.menu-element .ammo').removeClass('bullet1');
      $('#attack-button.menu-element .ammo').removeClass('bullet2');
      $('#attack-button.menu-element .ammo').removeClass('bullet3');
      $('#attack-button.menu-element .ammo').removeClass('bullet4');
      $('#attack-button.menu-element .ammo').removeClass('bullet5');

      $('#attack-button.menu-element .ammo').removeClass('hit1');
      $('#attack-button.menu-element .ammo').removeClass('hit2');
      $('#attack-button.menu-element .ammo').removeClass('hit3');
      $('#attack-button.menu-element .ammo').removeClass('hit4');
      $('#attack-button.menu-element .ammo').removeClass('hit5');

      if (ammo > 0 && ammo < 6) {
        if (longRange === true) {
          $('#attack-button.menu-element .ammo').addClass('bullet' + ammo);
        } else {
          $('#attack-button.menu-element .ammo').addClass('hit' + ammo);
        }
      }
    }
  }, {
    key: 'drawDamage',
    value: function drawDamage(damage) {
      $('#attack-button.menu-element .damage .text').text(damage);
    }
  }, {
    key: 'drawInventory',
    value: function drawInventory(player, inventory) {
      var currentInventory = parseInt(player.currentInventory);
      $('#inventory .content').html('');
      var i = 0;
      for (i = 0; i < inventory.length; i++) {

        var item = $('<div />');
        item.addClass('item');
        item.addClass('item' + currentInventory);

        var img = $('<img />');
        img.attr('src', '/assets/imgs/' + inventory[i].slug + '.png');
        img.data('id', inventory[i].id);
        img.data('item', inventory[i]);
        item.append(img);
        $('#inventory .content').append(item);

        if (inventory[i].id == player.defense.id || inventory[i].id == player.weapon.id) {
          item.addClass('selected');
        }

        item.mousedown(function (e) {
          e.preventDefault();
          var img = $(this).find('img');
          switch (e.which) {
            case 1:
              game.useItem(img);
              break;
            case 3:
              game.discardItem(img);
              break;
            default:
              break;
          }
        });

        img.mouseover(function (e) {
          var item = $(this);
          game.showInventoryItem(item.data('item'));
        });
      }

      for (i = inventory.length; i < currentInventory; i++) {
        var item = $('<div />');
        item.addClass('item');
        item.addClass('item' + currentInventory);
        $('#inventory .content').append(item);
      }

      for (i = currentInventory; i < 6; i++) {
        var item = $('<div />');
        item.addClass('item');
        item.addClass('invalid');
        item.addClass('item' + currentInventory);
        $('#inventory .content').append(item);
      }
    }
  }, {
    key: 'showInventoryItem',
    value: function showInventoryItem(item) {
      var info = $('#inventory-info');
      info.find('.name').text(item.name);
      info.find('.image').attr('src', '/assets/imgs/' + item.slug + '.png');
      info.find('.description').text(item.description);

      var characteristics = '';

      if (item.currentLevel) {
        characteristics += '<div class=\'damage damage-img\'><div class=\'text\'>' + item.currentLevel + '</div></div>';
      }

      if (item.damage) {
        characteristics += '<div class=\'damage damage-img\'><div class=\'text\'>' + item.damage + '</div></div>';
        if (item.longRange === true) {
          characteristics += '<div class=\'ammo bullet' + item.currentAmmo + '\' />';
        } else {
          characteristics += '<div class=\'ammo hit' + item.currentAmmo + '\' />';
        }
      }

      info.find('.characteristics').html(characteristics);

      info.css('visibility', 'visible');
    }
  }, {
    key: 'useItem',
    value: function useItem(item) {
      document.querySelector('#inventory-info').style.visibility = 'hidden';
      this.stomp.sendMessage('USE_OBJECT', { item: $(item).data('id') });
    }
  }, {
    key: 'discardItem',
    value: function discardItem(item) {
      game.stomp.sendMessage('DISCARD_OBJECT', { item: $(item).data('id') });
      document.querySelector('#inventory-info').style.visibility = 'hidden';
    }
  }, {
    key: 'unequip',
    value: function unequip(item) {
      this.stomp.sendMessage('UNEQUIP', { item: $(item).data('id') });
    }
  }, {
    key: 'updateCatched',
    value: function updateCatched(gameInfo) {
      var survivors = gameInfo.data.catchedSurvivors,
          cleanText = function cleanText(x) {
        return x.innerHTML = '';
      },
          characterList = document.querySelectorAll('#list-character li p');

      R.map(cleanText, characterList);

      for (var s in survivors) {
        document.querySelector('#list-character .' + s + ' p').innerHTML = R.join(', ', survivors[s]);
      }
    }
  }, {
    key: 'updatePregame',
    value: function updatePregame(preGameInfo) {
      this.startMusic();
      var username = utils.getQueryParams().username;
      var survivors = preGameInfo.data.survivors;
      var listCharacter = $('#choose-character .list-character');
      listCharacter.html('');
      for (var s in survivors) {
        var survivorData = survivors[s];
        var survivorContainer = $('<div class=\'container\' />');
        var player = $('<div class=\'player\' />');
        var survivor = $('<img class=\'survivor\'/>');
        survivor.attr('src', '/assets/imgs/survivors/' + survivorData.slug + '.png');
        survivor.data('survivordata', survivorData);

        survivor.mouseover(function (e) {
          var s = $(this);
          game.previewSurvivor(s.data('survivordata'));
        });

        if (survivorData.player !== '') {
          survivor.addClass('selected');
          survivor.attr('draggable', false);
          player.text(survivorData.player);

          if (survivorData.player == username) {
            if (survivorData.leader) {
              $('#choose-character .team .leader img').attr('src', '/assets/imgs/survivors/' + survivorData.slug + '.png');
            } else {
              $('#choose-character .team .follower img').attr('src', '/assets/imgs/survivors/' + survivorData.slug + '.png');
            }
          }
        } else {
          player.text(' ');
          survivor.attr('draggable', true);
          survivor[0].addEventListener('dragstart', function (ev) {
            var s = $(ev.target);
            ev.dataTransfer.setData('slug', s.data('survivordata').slug);
          });
        }
        survivorContainer.append(survivor);
        survivorContainer.append(player);
        listCharacter.append(survivorContainer);
      }
    }
  }, {
    key: 'selectTeam',
    value: function selectTeam(ev, data) {
      if (data !== undefined && data.slug !== undefined) {
        game.stomp.sendMessage('SELECT_SURVIVOR', { leader: data.leader, survivor: data.slug });
      }
    }
  }, {
    key: 'previewSurvivor',
    value: function previewSurvivor(survivorData) {
      $('#choose-character .selected-character .photo').attr('src', '/assets/imgs/survivors/' + survivorData.slug + '.png');
      $('#choose-character .selected-character .name').text(survivorData.name);
      $('#choose-character .selected-character .description').text(survivorData.description);
    }
  }, {
    key: 'playerReady',
    value: function playerReady() {
      game.stomp.sendMessage('PLAYER_READY', {});
    }
  }, {
    key: 'setGoals',
    value: function setGoals() {
      var player = game.player.player;
      document.querySelector('#goals #own-goals .title').innerHTML = 'PERSONAL MISSION: ' + game.missions[player].name;
      document.querySelector('#goals #own-goals .content').innerHTML = game.missions[player].description;;

      var teamGoalsText = '';
      for (var v in game.victoryConditions) {
        var vic = game.victoryConditions[v];
        teamGoalsText += vic['description'] + '<br />';
      }
      document.querySelector('#goals #team-goals .title').innerHTML = 'TEAM MISSION: ' + game.victoryConditions[0]['name'];
      document.querySelector('#goals #team-goals .content').innerHTML = teamGoalsText;
    }
  }, {
    key: 'finalCountDown',
    value: function finalCountDown() {
      var time = 900,
          duration = moment.duration(time * 1000, 'milliseconds'),
          interval = 1000;

      setInterval(function () {
        duration = moment.duration(duration.asMilliseconds() - interval, 'milliseconds');
        $('#top-right-interface').text(moment(duration.asMilliseconds()).format('mm:ss'));
      }, interval);
    }
  }, {
    key: 'sendAttackMessage',
    value: function sendAttackMessage(point) {
      this.stomp.sendMessage('ATTACK', { point: point.toString() });
    }
  }, {
    key: 'sendMoveMessage',
    value: function sendMoveMessage(point) {
      this.stomp.sendMessage('MOVE', { point: point.toString() });
    }
  }, {
    key: 'sendSearchMessage',
    value: function sendSearchMessage() {
      this.stomp.sendMessage('SEARCH', {});
    }
  }, {
    key: 'sendSearchMoreMessage',
    value: function sendSearchMoreMessage(token) {
      this.stomp.sendMessage('SEARCH_MORE', { token: token });
    }
  }, {
    key: 'sendNoiseMessage',
    value: function sendNoiseMessage() {
      this.stomp.sendMessage('NOISE', {});
    }
  }, {
    key: 'sendChatMessage',
    value: function sendChatMessage(text) {
      this.stomp.sendMessage('CHAT', { text: text });
    }
  }, {
    key: 'sendEndTurnMessage',
    value: function sendEndTurnMessage() {
      this.stomp.sendMessage('END_TURN', {});
    }
  }, {
    key: 'getSurvivorById',
    value: function getSurvivorById(id) {
      return R.find(R.propEq('id', id), this.survivors);
    }
  }, {
    key: 'setSurvivorClass',
    value: function setSurvivorClass(element, className) {
      element.removeClass('pablo');
      element.removeClass('xenia');
      element.removeClass('miguel');
      element.removeClass('laura');
      element.removeClass('yami');
      element.removeClass('alex');
      element.addClass(className);
    }
  }, {
    key: 'getItem',
    value: function getItem() {
      this.stomp.sendMessage('GET_OBJECT', { item: this.foundItem });
      this.lightbox.hideAll();
    }
  }, {
    key: 'showLogAttack',
    value: function showLogAttack(survivor, weapon, deaths) {
      var logEntry = $('<div />');
      var survivorImg = $('<div />');
      survivorImg.addClass('survivor-img');
      survivorImg.addClass(survivor);
      logEntry.append(survivorImg);
      var text = $('<span />');
      text.text('Attacks with ' + weapon + ' and kills ' + deaths);
      logEntry.append(text);

      var zombieImg = $('<div />');
      zombieImg.addClass('survivor-img');
      zombieImg.addClass('zombie');
      logEntry.append(zombieImg);

      $('#log').append(logEntry);

      document.querySelector('#log').scrollTop = document.querySelector('#log').scrollHeight;
    }
  }, {
    key: 'showLogZombieAttack',
    value: function showLogZombieAttack(survivor, damage, death) {
      var logEntry = $('<div />');

      var zombieImg = $('<div />');
      zombieImg.addClass('survivor-img');
      zombieImg.addClass('zombie');
      logEntry.append(zombieImg);

      var text = $('<span />');
      text.text('Does ' + damage + ' damage to ');
      logEntry.append(text);

      var survivorImg = $('<div />');
      survivorImg.addClass('survivor-img');
      survivorImg.addClass(survivor);
      logEntry.append(survivorImg);

      if (death) {
        var _text = $('<span />');
        _text.text(' (R.I.P)');
        logEntry.append(_text);
      }

      $('#log').append(logEntry);

      document.querySelector('#log').scrollTop = document.querySelector('#log').scrollHeight;
    }
  }, {
    key: 'showLogSearch',
    value: function showLogSearch(survivor) {
      this.showGenericLog(survivor, 'Search the room');
    }
  }, {
    key: 'showLogMove',
    value: function showLogMove(survivor) {
      this.showGenericLog(survivor, 'Moves');
    }
  }, {
    key: 'showLogStartTurn',
    value: function showLogStartTurn(survivor) {
      this.showGenericLog(survivor, 'START TURN!');
    }
  }, {
    key: 'showGenericLog',
    value: function showGenericLog(survivor, text) {
      var logEntry = $('<div />');
      var survivorImg = $('<div />');
      survivorImg.addClass('survivor-img');
      survivorImg.addClass(survivor);
      logEntry.append(survivorImg);
      var spn = $('<span />');
      spn.text(text);
      logEntry.append(spn);

      $('#log').append(logEntry);

      document.querySelector('#log').scrollTop = document.querySelector('#log').scrollHeight;
    }
  }, {
    key: 'findItem',
    value: function findItem(user, survivor, items) {
      this.showLogSearch(survivor);
      if (user == game.player.player) {
        this.lightbox.hideAll();

        $('#find-item .content .item1 .image').css('background-image', 'url(/assets/imgs/' + items[0].slug + '.png)');
        $('#find-item .content .info1 .item-title').text(items[0].name);
        $('#find-item .content .info1 .item-description').text(items[0].description);

        if (items[0].currentAmmo !== undefined) {
          if (items[0].longRange === true) {
            document.querySelector('#find-item .content .item1 .ammo').className = 'ammo bullet' + items[0].currentAmmo;
          } else {
            document.querySelector('#find-item .content .item1 .ammo').className = 'ammo hit' + items[0].currentAmmo;
          }
        } else {
          document.querySelector('#find-item .content .item1 .ammo').className = 'ammo hidden';
        }

        if (items[0].damage !== undefined) {
          document.querySelector('#find-item .content .item1 .damage').className = 'damage';
          document.querySelector('#find-item .content .item1 .damage').innerHTML = '<span class=\'text\'>' + items[0].damage + '</span>';
        } else {
          document.querySelector('#find-item .content .item1 .damage').className = 'damage hidden';
        }

        if (items[0].currentLevel !== undefined) {
          document.querySelector('#find-item .content .item1 .defense').className = 'defense';
          document.querySelector('#find-item .content .item1 .defense').innerHTML = '<span class=\'text\'>' + items[0].currentLevel + '</span>';
        } else {
          document.querySelector('#find-item .content .item1 .defense').className = 'defense hidden';
        }

        this.foundItem = items[0].id;

        this.lightbox.show('#find-item');
      }
    }
  }, {
    key: 'showZombieAttack',
    value: function showZombieAttack(user, survivor, damage, death) {
      this.showLogZombieAttack(survivor, damage, death);
      if (user == game.player.player) {
        this.lightbox.hideAll();
        this.setSurvivorClass($('#zombie-attack .survivor'), survivor);
        var text = 'Does ' + damage + ' damage';
        if (death) {
          text += ' (R.I.P.)';
        }
        $('#zombie-attack .info').text(text);
        this.lightbox.show('#zombie-attack');
      }
    }
  }, {
    key: 'startTurn',
    value: function startTurn(user, survivor) {
      this.showLogStartTurn(survivor);

      if (user == game.player.player) {
        $('#move-button').css('visibility', 'visible');
        $('#attack-button').css('visibility', 'visible');
        $('#search-button').css('visibility', 'visible');
        $('#noise-button').css('visibility', 'visible');
        $('#end-turn-button').css('visibility', 'visible');
      } else {

        $('#move-button').css('visibility', 'hidden');
        $('#attack-button').css('visibility', 'hidden');
        $('#search-button').css('visibility', 'hidden');
        $('#noise-button').css('visibility', 'hidden');
        $('#end-turn-button').css('visibility', 'hidden');

        $('#log').removeClass('closed');
        $('#log').removeClass('small');
      }
    }
  }, {
    key: 'showChat',
    value: function showChat(dataSurvivor, dataText) {
      var msg = $('<div />');
      msg.addClass('message');
      var img = $('<div />');
      img.addClass('survivor-img');
      img.addClass(dataSurvivor);
      msg.append(img);
      var text = $('<span />');
      text.addClass('text');
      text.text(dataText);
      msg.append(text);

      $('#chat .chat-messages').append(msg);

      $('#chat .chat-messages')[0].scrollTop = $('#chat .chat-messages')[0].scrollHeight;

      $('#chat').show();
    }
  }, {
    key: 'showZombieTime',
    value: function showZombieTime(damages, numNewZombies) {
      this.showGenericLog('zombie', 'ZOMBIE TIME');
      this.lightbox.hideAll();

      document.querySelector('#zt-audio').play();

      $('#zombie-time .survivors').html('');

      var i = 0;
      $('#zombie-time .survivors').html('');
      $('#zombie-time .info').html('');
      for (i = 0; i < damages.length; i++) {
        var survivorImg = $('<div />');
        survivorImg.addClass('survivor');
        survivorImg.addClass(damages[i].survivor);

        $('#zombie-time .survivors').append(survivorImg);

        var text = damages[i].damage + ' damage';
        if (damages[i].death) {
          text += ' (R.I.P.)';
        }

        var survivor = $('<div />');
        survivor.addClass('survivor');
        survivor.text(text);
        $('#zombie-time .info').append(survivor);
      }

      $('#zombie-time .newzombies .text').text(numNewZombies + ' new zombies!');
      this.lightbox.show('#zombie-time');
    }
  }, {
    key: 'startGame',
    value: function startGame() {
      $('#choose-character').hide();
      this['interface'].show();
      this.finalCountDown();
      this.setGoals();
      this.playMusic('game');
    }
  }, {
    key: 'endGame',
    value: function endGame(data) {
      this.lightbox.hideAll();

      var text = 'You lose :(';
      if (data.win) {
        text = 'You win!';
      }

      $('#end-game .main-mission .result').text(text);

      $('#end-game .personal-missions .result').html('');
      R.forEachIndexed(function (missionInfo) {
        var mission = $('<div />');
        mission.addClass('mission');
        var missionImage = $('<img />');
        missionImage.attr('src', '/assets/imgs/survivors/' + missionInfo.survivor + '.png');
        mission.append(missionImage);
        mission.append($('<div class=\'name\'>' + missionInfo.name + '</div>'));
        mission.append($('<div>' + missionInfo.description + '</div>'));
        if (missionInfo.success) {
          mission.append($('<div class=\'success\'>SUCCESS</div>'));
        } else {
          mission.append($('<div class=\'fail\'>FAIL</div>'));
        }

        $('#end-game .personal-missions .result').append(mission);
      }, data.missions);

      $('#end-game').show();
      this.playMusic('end-game');
    }
  }, {
    key: 'registerEventHandlers',
    value: function registerEventHandlers() {
      var _this2 = this;

      var w = $(window),
          onMessage = function onMessage(e, message) {

        switch (message.type) {
          case 'PRE_GAME':
            _this2.updatePregame(message);
            break;
          case 'FULL_GAME':
            _this2.initialized ? _this2.parseGameInfo(message) : _this2.initialize(message);
            break;
          case 'START_GAME':
            _this2.startGame();
            break;
          case 'ANIMATION_MOVE':
            _this2.showLogMove(message.data.survivor);
            break;
          case 'ANIMATION_ATTACK':
            _this2.showLogAttack(message.data.survivor, message.data.weapon, message.data.deaths);
            break;
          case 'FIND_ITEM':
            _this2.findItem(message.user, message.data.survivor, message.data.items);
            break;
          case 'ZOMBIE_TIME':
            _this2.showZombieTime(message.data.damages, message.data.numNewZombies);
            break;
          case 'ZOMBIE_ATTACK':
            _this2.showZombieAttack(message.user, message.data.survivor, message.data.damage, message.data.death);
            break;
          case 'END_GAME':
            _this2.endGame(message.data);
            break;
          case 'START_TURN':
            _this2.startTurn(message.user, message.data.survivor);
            break;
          case 'CHAT':
            _this2.showChat(message.data.survivor, message.data.text);
            break;
        }
        if (_this2.canvas !== undefined) {
          _this2.canvas.redraw();
        }
      },
          onCellClick = function onCellClick(e, cell) {
        if (_this2.canvas.currentAction == 'move' && R.contains(cell, _this2.player.canMoveTo)) {
          if (_this2.player.canMoveTo.length > 0) {
            _this2.sendMoveMessage(cell);
            _this2.canvas.currentAction = undefined;
          }
        } else if (_this2.canvas.currentAction == 'attack' && R.contains(cell, _this2.player.canAttackTo)) {
          if (_this2.player.canAttackTo.length > 0) {
            _this2.sendAttackMessage(cell);
            _this2.canvas.currentAction = undefined;
          }
        }
        _this2.canvas.redraw();
      },
          onInterfaceButtonClick = function onInterfaceButtonClick(e, action, searchMoreToken) {

        if (action == 'chat') {
          $('#chat').toggle();
        } else if (_this2.myTurn) {
          switch (action) {
            case 'search':
              if (_this2.player.canSearch) {
                _this2.sendSearchMessage();
                _this2.canvas.currentAction = undefined;
              }
              break;
            case 'searchMore':
              _this2.sendSearchMoreMessage(searchMoreToken);
              _this2.canvas.currentAction = undefined;
              break;
            case 'noise':
              _this2.sendNoiseMessage();
              _this2.canvas.currentAction = undefined;
              break;
            case 'endTurn':
              _this2.sendEndTurnMessage();
              _this2.canvas.currentAction = undefined;
              break;
            default:
              _this2.canvas.currentAction = action;
          }
          _this2.canvas.redraw();
        }
      },
          onSendChat = function onSendChat(e) {
        var text = document.querySelector('.chat-text').value;
        document.querySelector('.chat-text').value = '';
        _this2.sendChatMessage(text);
      },
          onToggleLog = function onToggleLog() {
        if ($('#log').hasClass('closed')) {
          $('#log').removeClass('closed');
        } else if ($('#log').hasClass('small')) {
          $('#log').removeClass('small');
          $('#log').addClass('closed');
        } else {
          $('#log').addClass('small');
        }
      };

      w.on('message.stomp.zt', onMessage);
      w.on('cellClick.canvas.zt', onCellClick);
      w.on('buttonClick.interface.zt', onInterfaceButtonClick);
      w.on('sendChat.interface.zt', onSendChat);
      w.on('toggleLog.interface.zt', onToggleLog);
      w.on('drop.interface.zt', this.selectTeam);
      w.on('buttonClick.ready.zt', this.playerReady);

      w.bind('contextmenu', function (e) {
        e.preventDefault();
      });
    }
  }]);

  return Game;
})();
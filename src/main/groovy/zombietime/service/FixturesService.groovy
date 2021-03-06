package zombietime.service

import groovy.json.JsonSlurper
import zombietime.domain.Mission
import zombietime.domain.Point
import zombietime.domain.SearchPoint
import zombietime.domain.VictoryCondition
import zombietime.repository.MissionRepository
import zombietime.repository.PersonalMissionRepository
import zombietime.repository.TileRepository

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import zombietime.repository.SurvivorRepository
import zombietime.repository.ZombieRepository
import zombietime.repository.DefenseRepository
import zombietime.repository.WeaponRepository
import zombietime.repository.ItemRepository

@Service
class FixturesService {

    @Autowired
    SurvivorRepository survivorRepository
    @Autowired
    ZombieRepository zombieRepository
    @Autowired
    DefenseRepository defenseRepository
    @Autowired
    WeaponRepository weaponRepository
    @Autowired
    ItemRepository itemRepository
    @Autowired
    MissionRepository missionRepository
    @Autowired
    TileRepository tileRepository
    @Autowired
    PersonalMissionRepository personalMissionRepository

    @PostConstruct
    def loadFixtures() {

        def jsonSlurper = new JsonSlurper()
        def result

        // survivors
        File survivors = new File('src/main/resources/fixtures/survivor.json')
        result = jsonSlurper.parseText(survivors.text)
        for (survivor in result) {
            survivorRepository.create(
                    name: survivor.name,
                    slug: survivor.slug,
                    avatar: survivor.avatar,
                    description: survivor.description,
                    life: survivor.life,
                    actions: survivor.actions,
                    inventory: survivor.inventory,
                    defense: survivor.defense,
                    movement: survivor.movement,
                    noise: survivor.noise
            )
        }

        // zombies
        File zombies = new File('src/main/resources/fixtures/zombie.json')
        result = jsonSlurper.parseText(zombies.text)
        for (zombie in result) {
            zombieRepository.create(
                    name: zombie.name,
                    slug: zombie.slug,
                    avatar: zombie.avatar,
                    life: zombie.life,
                    damage: zombie.damage
            )
        }

        // defenses
        File defenses = new File('src/main/resources/fixtures/defense.json')
        result = jsonSlurper.parseText(defenses.text)
        for (defense in result) {
            defenseRepository.create(
                    name: defense.name,
                    slug: defense.slug,
                    description: defense.description,
                    avatar: defense.avatar,
                    level: defense.level
            )
        }

        // weapons
        File weapons = new File('src/main/resources/fixtures/weapon.json')
        result = jsonSlurper.parseText(weapons.text)
        for (weapon in result) {
            weaponRepository.create(
                    name: weapon.name,
                    slug: weapon.slug,
                    description: weapon.description,
                    avatar: weapon.avatar,
                    ammo: weapon.ammo,
                    damage: weapon.damage,
                    noise: weapon.noise,
                    longRange: weapon.longRange
            )
        }

        // item - ammo
        File ammos = new File('src/main/resources/fixtures/ammo.json')
        result = jsonSlurper.parseText(ammos.text)
        for (ammo in result) {
            itemRepository.create(
                    name: ammo.name,
                    description: ammo.description,
                    slug: ammo.slug,
                    avatar: ammo.avatar,
                    ammo: ammo.ammo
            )
        }

        // item - gas
        File gases = new File('src/main/resources/fixtures/gas.json')
        result = jsonSlurper.parseText(gases.text)
        for (gas in result) {
            itemRepository.create(
                    name: gas.name,
                    slug: gas.slug,
                    description: gas.description,
                    avatar: gas.avatar,
                    gas: gas.gas
            )
        }

        // item - harmonic
        File harmonics = new File('src/main/resources/fixtures/harmonic.json')
        result = jsonSlurper.parseText(harmonics.text)
        for (harmonic in result) {
            itemRepository.create(
                    name: harmonic.name,
                    description: harmonic.description,
                    slug: harmonic.slug,
                    avatar: harmonic.avatar,
                    noise: harmonic.noise
            )
        }

        // item - medicine
        File medicines = new File('src/main/resources/fixtures/medicine.json')
        result = jsonSlurper.parseText(medicines.text)
        for (medicine in result) {
            itemRepository.create(
                    name: medicine.name,
                    slug: medicine.slug,
                    description: medicine.description,
                    avatar: medicine.avatar,
                    life: medicine.life
            )
        }

        // item - skates
        File skates = new File('src/main/resources/fixtures/skates.json')
        result = jsonSlurper.parseText(skates.text)
        for (skate in result) {
            itemRepository.create(
                    name: skate.name,
                    slug: skate.slug,
                    description: skate.description,
                    avatar: skate.avatar,
                    movement: skate.movement,
                    persistent: skate.persistent
            )
        }

        // tiles
        File tiles = new File('src/main/resources/fixtures/tiles.json')
        result = jsonSlurper.parseText(tiles.text)
        for (tile in result) {
            tileRepository.create(
                    num: tile.num,
                    up: tile.up,
                    right: tile.right,
                    down: tile.down,
                    left: tile.left,
                    search: tile.search
            )
        }

        // Missions
        File missions = new File('src/main/resources/fixtures/missions.json')
        result = jsonSlurper.parseText(missions.text)
        for (mission in result) {

            def objects = mission.objects.collect { _findElementBySlug(it) }

            def startSurvivalPoints = []
            mission.startSurvivalPoints.each {
                startSurvivalPoints << new Point(x: it.x, y: it.y)
            }

            def startZombiePoints = []
            mission.startZombiePoints.each {
                startZombiePoints << new Point(x: it.x, y: it.y)
            }

            def entryZombiePoints = []
            mission.entryZombiePoints.each {
                entryZombiePoints << new Point(x: it.x, y: it.y)
            }

            def victoryConditions = []
            mission.victoryConditions.each { vc ->
                victoryConditions << new VictoryCondition(
                        point: new Point(x: vc.point.x, y: vc.point.y),
                        things: vc.things,
                        name: vc.name,
                        description: vc.description
                )
            }

            def searchPoints = []
            mission.searchPoints.each { sp ->
                searchPoints << new SearchPoint(
                        point: new Point(x: sp.point.x, y: sp.point.y),
                        thing: _findElementBySlug(sp.thing)
                )
            }


            Mission m = missionRepository.create(
                    mission.slug,
                    mission.map.width,
                    mission.map.height,
                    mission.map.floorTiles,
                    mission.map.wallTiles,
                    mission.map.itemTiles,
                    objects,
                    startSurvivalPoints,
                    startZombiePoints,
                    entryZombiePoints,
                    victoryConditions,
                    searchPoints
            )
        }

        // Personal missions
        File personalMissions = new File('src/main/resources/fixtures/personal_missions.json')
        result = jsonSlurper.parseText(personalMissions.text)
        for (mission in result) {
            personalMissionRepository.create(
                    name: mission.name,
                    description: mission.description,
                    things: mission.things,
                    type: mission.type,
                    value: mission.value
            )
        }

    }


    Object _findElementBySlug(String slug) {
        def element = defenseRepository.get(slug)
        if (!element) {
            element = weaponRepository.get(slug)
        }
        if (!element) {
            element = itemRepository.get(slug)
        }

        return element
    }


}

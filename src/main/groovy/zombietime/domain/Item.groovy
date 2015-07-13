package zombietime.domain

class Item {

    String name
    Integer avatar

    Integer life
    Integer gas
    Integer ammo
    Integer movement
    Integer noise
    Integer defense
    Integer inventory
    Integer action

    Boolean persistent

    def addsLife() {
        return life > 0
    }

    def addsGas() {
        return gas > 0
    }

    def addsAmmo() {
        return ammo > 0
    }

    def addsMovement() {
        return movement > 0
    }

    def makesNoise() {
        return noise > 0
    }

    def addsDefense() {
        return defense > 0
    }

    def addsInventory() {
        return inventory > 0
    }

    def addsAction() {
        return action > 0
    }
}

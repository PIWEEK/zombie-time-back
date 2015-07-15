package zombietime.domain

class Item {

    String name
    String slug
    Integer avatar
    String description

    Integer life = 0
    Integer gas = 0
    Integer ammo = 0
    Integer movement = 0
    Integer noise = 0
    Integer defense = 0
    Integer inventory = 0
    Integer action = 0

    boolean persistent = false

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

    ItemStatus createStatus() {
        return new ItemStatus(item: this)
    }
}

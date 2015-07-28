package zombietime.domain

class Survivor {

    String name
    String slug
    String description
    Integer avatar
    Integer life
    Integer actions
    Integer inventory
    Integer defense
    Integer movement
    Integer noise


    SurvivorStatus createStatus() {
        return new SurvivorStatus(survivor: this, remainingLife: this.life, remainingActions: this.actions, remainingInventory: this.inventory, remainingDefense: this.defense, remainingMovement: this.movement, remainingNoise: this.noise)
    }


    Map asMap() {
        return [
                name       : name,
                slug       : slug,
                avatar     : avatar,
                life       : life,
                actions    : actions,
                inventory  : inventory,
                defense    : defense,
                movement   : movement,
                noise      : noise,
                description: description
        ]
    }
}

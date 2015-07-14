package zombietime.domain

class Survivor {

    String name
    String slug
    Integer avatar
    Integer life
    Integer actions
    Integer inventory
    Integer defense


    SurvivorStatus createStatus() {
        return new SurvivorStatus(survivor: this, remainingLife: this.life, remainingActions: this.actions, remainingInventory: this.inventory, remainingDefense: this.defense,)
    }
}

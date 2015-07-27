package zombietime.domain

class Defense {

    String name
    String slug
    String description
    Integer avatar
    Integer level = 1

    DefenseStatus createStatus() {
        return new DefenseStatus(defense: this, remainingLevel: level)
    }

}

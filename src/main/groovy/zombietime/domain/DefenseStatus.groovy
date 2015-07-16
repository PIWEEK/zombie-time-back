package zombietime.domain

class DefenseStatus {

    String id = UUID.randomUUID()
    Integer remainingLevel

    Defense defense

    Map asMap() {
        return [
                id          : id,
                name        : defense.name,
                avatar      : defense.avatar,
                maxLevel    : defense.level,
                currentLevel: remainingLevel
        ]
    }

    String getSlug() {
        return defense.slug
    }

}

package zombietime.domain

class DefenseStatus {

    String id = UUID.randomUUID()
    Integer remainingLevel

    Defense defense

    Map asMap() {
        return [
                id          : id,
                slug        : defense.slug,
                name        : defense.name,
                avatar      : defense.avatar,
                maxLevel    : defense.level,
                currentLevel: remainingLevel,
                description : defense.description
        ]
    }

    String getSlug() {
        return defense.slug
    }

}

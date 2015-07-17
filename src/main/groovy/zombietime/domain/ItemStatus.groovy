package zombietime.domain

class ItemStatus implements Status {

    String id = UUID.randomUUID()
    Item item

    Map asMap() {
        return [
                id         : id,
                name       : item.name,
                slug       : item.slug,
                avatar     : item.avatar,
                persistent : item.persistent,
                description: item.description
        ]
    }

    String getSlug() {
        return item.slug
    }

}

package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.Item

import java.util.concurrent.ConcurrentHashMap

@Component
class ItemRepository {
    ConcurrentHashMap<String, Item> items = new ConcurrentHashMap<String, Item>()

    void create(Map args = [:]) {
        def item = new Item(args)
        items.put(item.slug, item)
    }

    Item get(String slug) {
        return items.get(slug)
    }

    void remove(String slug) {
        items.remove(slug)
    }

    List<Item> list() {
        return items.values().toList()
    }
}

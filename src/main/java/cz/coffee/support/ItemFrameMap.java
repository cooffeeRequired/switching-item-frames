package cz.coffee.support;

import org.bukkit.block.Container;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemFrameMap<K extends ItemFrame, V extends ItemStack, E extends Container> {
    private final Map<K, List<V>> map = new HashMap<>();
    private final Map<K, Integer> indexMap = new HashMap<>();
    private final Map<K, E> containerMap = new HashMap<>();

    public void add(K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        indexMap.put(key, map.get(key).size() - 1);
    }

    public void addAll(K key, Collection<V> values) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
        indexMap.put(key, map.get(key).size() - 1);
    }

    public void remove(K key) {
        map.remove(key);
        indexMap.remove(key);
        containerMap.remove(key);
    }

    public List<V> get(K key) {
        return map.get(key);
    }
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public int getIndex(K key) {
        return indexMap.getOrDefault(key, 0);
    }

    public void setIndex(K key, int index) {
        indexMap.put(key, index);
    }

    public void clear() {
        map.clear();
        indexMap.clear();
        containerMap.clear();
    }


    /*
        ==================== CONTAINERS ================================
     */


    public void addContainer(K key, E container) {
        containerMap.put(key, container);
    }

    public Optional<E> getContainer(K key) {
        return Optional.ofNullable(containerMap.get(key));
    }

    public boolean hasContainer(K key) {
        return containerMap.containsKey(key);
    }

    public synchronized void updateContainer(E key) {
        Container container = containerMap.get(key);
        if (container == null) return;
        container.getBlock().getState().update(true, false);
    }

    public K getFrameByContainer(E container) {
        K found = null;
        for (var entry: containerMap.entrySet()) {
            var value = entry.getValue();
            var key = entry.getKey();
            if (value.equals(container)) {
                found = key;
                break;
            }
        }
        return found;
    }
}
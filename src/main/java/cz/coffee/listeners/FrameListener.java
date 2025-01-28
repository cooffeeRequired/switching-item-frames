package cz.coffee.listeners;

import cz.coffee.RPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener class for interactions with item frames.
 */
public class FrameListener implements Listener {

    private static final int MAX_ITEMS = 10;

    private final Map<ItemFrame, List<ItemStack>> frameItems = new HashMap<>();
    private final Map<ItemFrame, Integer> frameIndices = new HashMap<>();

    /**
     * Handles player interactions with item frames.
     *
     * @param event The player interaction event.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) {
            return;
        }

        RPlayer player = new RPlayer(event.getPlayer());
        if (player.getBukkitPlayer().isSneaking()) {
            event.setCancelled(true);
            initializeFrameIfAbsent(itemFrame);

            ItemStack heldItem = player.getBukkitPlayer().getInventory().getItemInMainHand();
            if (heldItem.getType() != Material.AIR) {
                addItemToFrame(player, itemFrame, heldItem);
            }
        }
    }

    /**
     * Prevents entities from interacting with item frames.
     *
     * @param event The entity interaction event.
     */
    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    /**
     * Rotates items in all registered item frames.
     */
    public synchronized void rotateItems() {
        for (var world : Bukkit.getWorlds()) {
            for (var frame : world.getEntitiesByClass(ItemFrame.class)) {
                if (frameItems.containsKey(frame)) {
                    List<ItemStack> items = frameItems.get(frame);
                    if (!items.isEmpty()) {
                        int currentIndex = frameIndices.getOrDefault(frame, 0);
                        currentIndex = (currentIndex + 1) % items.size();
                        frameIndices.put(frame, currentIndex);
                        frame.setItem(items.get(currentIndex));
                    }
                }
            }
        }
    }

    /**
     * Clears all stored data for item frames.
     */
    public void clearData() {
        frameItems.clear();
        frameIndices.clear();
    }

    /**
     * Initializes an item frame in the data maps if not already present.
     *
     * @param itemFrame The item frame to initialize.
     */
    private void initializeFrameIfAbsent(ItemFrame itemFrame) {
        frameItems.putIfAbsent(itemFrame, new ArrayList<>());
        frameIndices.putIfAbsent(itemFrame, 0);
    }

    public void initializeFrameIfAbsent(ItemFrame itemFrame, List<ItemStack> items) {
        frameItems.putIfAbsent(itemFrame, items);
        frameIndices.putIfAbsent(itemFrame, 0);
    }


    /**
     * Adds an item to the rotation of an item frame.
     *
     * @param player    The player performing the action.
     * @param itemFrame The item frame to update.
     * @param item      The item to add.
     */
    private void addItemToFrame(RPlayer player, ItemFrame itemFrame, ItemStack item) {
        List<ItemStack> items = frameItems.get(itemFrame);
        if (items.size() < MAX_ITEMS) {
            items.add(item.clone());
            player.info("&7Added item '&e%s&7' to the frame's rotation.", item.getI18NDisplayName());
        } else {
            player.info("&cYou can't add more than %d items to the frame.", MAX_ITEMS);
        }
    }
}
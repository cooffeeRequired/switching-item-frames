package cz.coffee.listeners;

import cz.coffee.CustomItemFrame;
import cz.coffee.ItemFrameRotator;
import cz.coffee.support.ChatUitls;
import cz.coffee.support.ItemFrameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static cz.coffee.CustomItemFrame.isCustomItemFrame;

/**
 * Listener class for interactions with item frames.
 */
public class FrameListener implements Listener {

    private static final int MAX_ITEMS = 10;

    private final ItemFrameMap<ItemFrame, ItemStack, Container> frameMap = new ItemFrameMap<>();

    private static final NamespacedKey ITEM_KEY = CustomItemFrame.ITEM_KEY;

    private static final List<Material> ALLOWED_STORAGES = List.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.SHULKER_BOX,
            Material.ENDER_CHEST,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER
    );


    /**
     * Handles the placement of a custom item frame when a player interacts with an item frame item.
     *
     * @param event The PlayerInteractEvent triggered by the player's interaction.
     */
    @EventHandler
    public void onPlaceItemFrame(PlayerInteractEvent event) {
        if (!ItemFrameRotator.ROTATE_ITEMS) return;

        if (event.getItem()!= null && event.getItem().getType() == Material.ITEM_FRAME) {
            var item = event.getItem();
            var meta = item.getItemMeta();
            if (isCustomItemFrame(meta.getPersistentDataContainer()) && event.getPlayer().isSneaking()) {
                switch (event.getAction()) {
                    case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> {
                        if (
                                event.getItem()!= null &&
                                        event.getItem().getType() == Material.ITEM_FRAME &&
                                        isCustomItemFrame(meta.getPersistentDataContainer())
                        ) {
                            Entity placedEntity = event.getPlayer().getWorld().spawn(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), ItemFrame.class);
                            var player = event.getPlayer();

                            if (placedEntity instanceof ItemFrame itemFrame) {
                                var attachedBlock = event.getClickedBlock();
                                if (isAllowedStorage(attachedBlock)) {
                                    ChatUitls.info("&7Special &bRotated Item Frame&7 placed!", player);
                                    itemFrame.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "special_frame");
                                    handleStorageInteraction(itemFrame, attachedBlock, player);
                                } else {
                                    itemFrame.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "special_frame");
                                    ChatUitls.info("&7Special &bRotated Item Frame&7 placed!", player);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Called when player trying remove items from item frame
     * @param event The player damage event
     */
    @EventHandler
    public void onDamageItemFrame(EntityDamageByEntityEvent event) {
        if (!ItemFrameRotator.ROTATE_ITEMS) return;
        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
                event.setCancelled(true);
                if (player.isSneaking()) {
                    ChatUitls.info("&7Removed diamond item frame.", player);
                    itemFrame.remove();
                } else {
                    ChatUitls.info("&cYou can't remove item frames without sneaking.", player);
                }
            }
        }
    }

    /**
     * Called when player trying remove item frame
     * @param event The player damage event
     */
    @EventHandler
    public void onDamageItemFrame(HangingBreakByEntityEvent event) {
        if (!ItemFrameRotator.ROTATE_ITEMS) return;
        if (event.getRemover() instanceof Player player) {
            if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
                event.setCancelled(true);
                if (player.isSneaking()) {
                    ChatUitls.info("&7Removed diamond item frame.", player);
                    itemFrame.remove();
                } else {
                    ChatUitls.info("&cYou can't remove item frames without sneaking.", player);
                }
            }
        }
    }

    /**
     * Handles player interactions with item frames.
     *
     * @param event The player interaction event.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!ItemFrameRotator.ROTATE_ITEMS) return;
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            event.setCancelled(true);

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType()!= Material.AIR) {
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
        if (!ItemFrameRotator.ROTATE_ITEMS) return;
        if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Container container = (Container) event.getInventory().getHolder();
        var frame = frameMap.getFrameByContainer(container);
        if (container!= null && frameMap.contains(frame)) { // Check if container is in the map
            updateStorageItems(container);
        }
    }

    private void updateStorageItems(Container container) {
        Map<ItemStack, Double> storageItems = collectStorageItems(container);

        List<ItemStack> items = storageItems.entrySet().stream().map(entry -> {
            ItemStack item = entry.getKey().clone();
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatUitls.translate(String.format("&f&o%s &bx%.0f", item.getType().toString().toLowerCase().replaceAll("_", " "), entry.getValue())));
            item.setItemMeta(meta);
            return item;
        }).toList();


        var frame = frameMap.getFrameByContainer(container);
        frameMap.remove(frame);
        ChatUitls.info("&7Updated items in the frame.", container.getBlock().getWorld().getPlayers().toArray(new Player[0]));
        frameMap.addAll(frame, items);
        frameMap.addContainer(frame, container);
    }

    /**
     * Rotates items in all registered item frames.
     */
    public synchronized void rotateItems() {
        for (var world: Bukkit.getWorlds()) {
            for (var frame: world.getEntitiesByClass(ItemFrame.class)) {
                if (frameMap.contains(frame)) {
                    List<ItemStack> items = frameMap.get(frame);
                    if (!items.isEmpty()) {
                        int currentIndex = frameMap.getIndex(frame);
                        currentIndex = (currentIndex + 1) % items.size();
                        frameMap.setIndex(frame, currentIndex);
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
        frameMap.clear();
    }


    /**
     * Adds an item to the rotation of an item frame.
     *
     * @param player    The player performing the action.
     * @param itemFrame The item frame to update.
     * @param item      The item to add.
     */
    private void addItemToFrame(Player player, ItemFrame itemFrame, ItemStack item) {
        List<ItemStack> items = frameMap.get(itemFrame);
        if (items == null) {
            items = new ArrayList<>();
            frameMap.addAll(itemFrame, items); // Initialize with an empty list
        }
        if (items.size() < MAX_ITEMS) {
            items.add(item.clone());
            ChatUitls.info("&7Added item '&e%s&7' to the frame's rotation.", player, item.getI18NDisplayName());
        } else {
            ChatUitls.info("&cYou can't add more than %d items to the frame.", player, MAX_ITEMS);
        }
    }


    /**
     * Collects items from the storage block into a map of ItemStack and their quantities.
     *
     * @param container The storage block container.
     * @return A map of ItemStack to their total quantities.
     */
    private Map<ItemStack, Double> collectStorageItems(Container container) {
        Map<ItemStack, Double> storageItems = new HashMap<>();
        for (ItemStack item: container.getInventory().getContents()) {
            if (item!= null) {
                storageItems.merge(item, (double) item.getAmount(), Double::sum);
            }
        }
        return storageItems;
    }

    /**
     * Handles interactions between an item frame and a storage block.
     *
     * @param itemFrame The item frame involved.
     * @param storageBlock The storage block involved.
     * @param player The player performing the action.
     */
    private void handleStorageInteraction(ItemFrame itemFrame, Block storageBlock, Player player) {
        Container container = (Container) storageBlock.getState();
        frameMap.addContainer(itemFrame, container);
        Map<ItemStack, Double> storageItems = collectStorageItems(container);

        List<ItemStack> items = storageItems.entrySet().stream().map(entry -> {
            ItemStack item = entry.getKey().clone();
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatUitls.translate(String.format("&f&o%s &bx%.0f", item.getType().toString().toLowerCase().replaceAll("_", " "), entry.getValue())));
            item.setItemMeta(meta);
            return item;
        }).toList();

        frameMap.addAll(itemFrame, items);
        ChatUitls.info("&7Detected a storage behind it! &bRotating counting the items in it.", player);
    }

    /**
     * Determines if the block is a valid storage block.
     *
     * @param block The block to check.
     * @return True if the block is an allowed storage, false otherwise.
     */
    private boolean isAllowedStorage(Block block) {
        return ALLOWED_STORAGES.contains(block.getType()) && block.getState() instanceof Container;
    }
}
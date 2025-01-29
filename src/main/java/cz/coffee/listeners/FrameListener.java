package cz.coffee.listeners;

import cz.coffee.CustomItemFrame;
import cz.coffee.RPlayer;
import cz.coffee.utils.ChatUitls;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static cz.coffee.CustomItemFrame.ITEM_KEY;
import static cz.coffee.CustomItemFrame.isCustomItemFrame;

/**
 * Listener class for interactions with item frames.
 */
public class FrameListener implements Listener {

    private static final int MAX_ITEMS = 10;

    private final Map<ItemFrame, List<ItemStack>> frameItems = new HashMap<>();
    private final Map<ItemFrame, Integer> frameIndices = new HashMap<>();

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
        if (event.getItem() != null && event.getItem().getType() == Material.ITEM_FRAME) {
            var item = event.getItem();
            var meta = item.getItemMeta();
            if (isCustomItemFrame(meta.getPersistentDataContainer()) && event.getPlayer().isSneaking()) {
                switch (event.getAction()) {
                    case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> {
                        if (
                            event.getItem() != null &&
                            event.getItem().getType() == Material.ITEM_FRAME &&
                            isCustomItemFrame(meta.getPersistentDataContainer())
                        ) {
                            Entity placedEntity = event.getPlayer().getWorld().spawn(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), ItemFrame.class);
                            var rPlayer = new RPlayer(event.getPlayer());

                            if (placedEntity instanceof ItemFrame itemFrame) {
                                var attachedBlock = event.getClickedBlock();
                                if (isAllowedStorage(attachedBlock)) {
                                    itemFrame.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "special_frame");
                                    handleStorageInteraction(itemFrame, attachedBlock, rPlayer);
                                } else {
                                    itemFrame.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "special_frame");
                                    rPlayer.info("&7Special &bRotated Item Frame&7 placed!");
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
        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
                event.setCancelled(true);
                var rPlayer = new RPlayer(player);
                if (player.isSneaking()) {
                    rPlayer.info("&7Removed diamond item frame.");
                    itemFrame.remove();
                } else {
                    rPlayer.info("&cYou can't remove item frames without sneaking.");
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
        if (event.getRemover() instanceof Player player) {
            if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
                var rPlayer = new RPlayer(player);
                event.setCancelled(true);
                if (rPlayer.getBukkitPlayer().isSneaking()) {
                    rPlayer.info("&7Removed diamond item frame.");
                    itemFrame.remove();
                } else {
                    rPlayer.info("&cYou can't remove item frames without sneaking.");
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
        if (event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer())) {
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


    /**
     * Collects items from the storage block into a map of ItemStack and their quantities.
     *
     * @param container The storage block container.
     * @return A map of ItemStack to their total quantities.
     */
    private Map<ItemStack, Double> collectStorageItems(Container container) {
        Map<ItemStack, Double> storageItems = new HashMap<>();
        for (ItemStack item : container.getInventory().getContents()) {
            if (item != null) {
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
    private void handleStorageInteraction(ItemFrame itemFrame, Block storageBlock, RPlayer player) {
        Container container = (Container) storageBlock.getState();
        Map<ItemStack, Double> storageItems = collectStorageItems(container);

        List<ItemStack> items = storageItems.entrySet().stream().map(entry -> {
            ItemStack item = entry.getKey().clone();
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatUitls.translate(String.format("&f&o%s &bx%.0f", item.getType().toString().toLowerCase().replaceAll("_", " "), entry.getValue())));
            item.setItemMeta(meta);
            return item;
        }).toList();

        initializeFrameIfAbsent(itemFrame, items);
        player.info("&bStorage contents loaded into the item frame rotation.");
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
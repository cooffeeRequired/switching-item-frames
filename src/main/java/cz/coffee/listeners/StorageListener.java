package cz.coffee.listeners;

import cz.coffee.RPlayer;
import cz.coffee.utils.ChatUitls;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Listener class for interactions between item frames and storage blocks.
 */
public class StorageListener implements Listener {

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

    private final FrameListener frameListener;

    /**
     * Constructs a StorageListener instance.
     *
     * @param frameListener The FrameListener instance to interact with.
     */
    public StorageListener(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    /**
     * Handles the placement of item frames and checks for connected storage blocks.
     *
     * @param event The HangingPlaceEvent.
     */
    @EventHandler
    public void onItemFramePlace(HangingPlaceEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) {
            return;
        }

        Block attachedBlock = getAttachedBlock(event, itemFrame);
        if (attachedBlock == null) {
            return;
        }

        RPlayer player = new RPlayer(event.getPlayer());
        if (player == null) {
            return;
        }

        if (isAllowedStorage(attachedBlock)) {
            handleStorageInteraction(itemFrame, attachedBlock, player);
        }
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

        frameListener.initializeFrameIfAbsent(itemFrame, items);
        player.info("&bStorage contents loaded into the item frame rotation.");
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
     * Gets the block attached to the item frame.
     *
     * @param event The HangingPlaceEvent.
     * @param itemFrame The item frame entity.
     * @return The attached block, or null if none found.
     */
    private Block getAttachedBlock(HangingPlaceEvent event, ItemFrame itemFrame) {
        Block attachedBlock = event.getBlock().getRelative(itemFrame.getAttachedFace().getOppositeFace());
        Block behindAttachedBlock = attachedBlock.getRelative(itemFrame.getAttachedFace()).getRelative(itemFrame.getAttachedFace());
        return isAllowedStorage(attachedBlock) ? attachedBlock : isAllowedStorage(behindAttachedBlock) ? behindAttachedBlock : null;
    }
}
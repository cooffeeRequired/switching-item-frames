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

import static cz.coffee.CustomItemFrame.isCustomItemFrame;

/**
 * Listener class for interactions between item frames and storage blocks.
 */
public class StorageListener implements Listener {



    /**
     * Handles the placement of item frames and checks for connected storage blocks.
     *
     * @param event The HangingPlaceEvent.
     */
/*
    @EventHandler
    public void onItemFramePlace(HangingPlaceEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame && isCustomItemFrame(itemFrame.getPersistentDataContainer()))) {
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
*/








}
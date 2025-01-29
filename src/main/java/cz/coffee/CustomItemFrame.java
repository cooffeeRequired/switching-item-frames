package cz.coffee;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CustomItemFrame {

    public static final NamespacedKey ITEM_KEY = new NamespacedKey("item_frame_rotator", "special_item_frame");

    public static ItemStack createCustomItemFrame() {
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§bDiamond Item Frame");
            meta.setUnbreakable(true);
            //meta.setCustomModelData(1); // TODO
            meta.setLore(List.of(
                    "§7A reinforced item frame",
                    "§7Crafted with diamonds",
                    "",
                    "§7Shift+Right place to any storage for counting items in it.",
                    "§7Shift+Left remove the counter from the storage.",
                    "§f§lINFO: §rYou can't remove the items from the item frame.",

                    "",
                    "§7Shift+Right add an item to the frame's rotation only when if behing it doesn't be any storage",
                    "§7Shift+Left remove the held item from the frame's rotation."
            ));

            // Custom tag to identify our special item frame
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(ITEM_KEY, PersistentDataType.STRING, "special_frame");

            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isCustomItemFrame(PersistentDataContainer container) {
        if (container == null) return false;
        if (container.has(ITEM_KEY, PersistentDataType.STRING)) return true;
        return false;
    }

    public static void registerRecipe(Plugin plugin) {
        ItemStack result = createCustomItemFrame();

        // Define custom recipe (same shape as vanilla, but replaces paper with diamond)
        ShapedRecipe recipe = new ShapedRecipe(ITEM_KEY, result);
        recipe.shape("D", "I");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('I', Material.ITEM_FRAME);

        plugin.getServer().addRecipe(recipe);
    }

    public static class CreativeInventoryListener implements Listener {
        @EventHandler
        public void onCreativeOpen(PrepareItemCraftEvent event) {
            if (event.getInventory().getType() == InventoryType.CREATIVE) {
                event.getInventory().addItem(CustomItemFrame.createCustomItemFrame());
            }
        }
    }
}


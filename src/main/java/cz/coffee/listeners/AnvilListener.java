package cz.coffee.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import static cz.coffee.support.ChatUitls.translate;

/**
 * Listener class for interactions with anvils.
 */
public class AnvilListener implements Listener {

    /**
     * Handles item renaming in anvils.
     *
     * @param event The prepare anvil event.
     */
    @EventHandler
    public void onItemFrameInteract(PrepareAnvilEvent event) {
        if (event.getResult() == null || event.getResult().getItemMeta() == null) {
            return;
        }

        String renameText = event.getView().getRenameText();
        if (renameText == null || renameText.isBlank()) {
            return;
        }

        var result = event.getResult();
        var resultMeta = result.getItemMeta();
        resultMeta.setDisplayName(translate(renameText));
        result.setItemMeta(resultMeta);
        event.setResult(result);
    }
}
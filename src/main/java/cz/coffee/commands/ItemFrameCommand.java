package cz.coffee.commands;

import cz.coffee.ItemFrameRotator;
import cz.coffee.support.ChatUitls;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemFrameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("toggle")) {
                if (sender instanceof Player player) {
                    ItemFrameRotator.ROTATE_ITEMS = !ItemFrameRotator.ROTATE_ITEMS;
                    ChatUitls.info("Item frames was %s.", player, ItemFrameRotator.ROTATE_ITEMS ? "&aenabled" : "&cdisabled");
                    return true;
                }
            }
        }
        return false;
    }
}

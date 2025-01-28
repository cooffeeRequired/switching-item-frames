package cz.coffee;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static cz.coffee.utils.ChatUitls.translate;

public class RPlayer {
    Player player;

    public RPlayer(Player player) {
        this.player = player;
    }

    public void info(String message, Object... args) {
        player.sendMessage(translate(String.format("&e[IFR]&r " + message, args)));
    }

    public Player getBukkitPlayer() {
        return player;
    }
}

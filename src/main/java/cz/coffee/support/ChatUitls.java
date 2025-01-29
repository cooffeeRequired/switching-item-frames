package cz.coffee.support;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatUitls {
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static void info(String message, Object... args) {
        System.out.println(translate(String.format("&e[IFR]&r " + message, args)));
    }

    public static void info(String message, Player[] players, Object... args) {
        for (Player player : players) {
            info(message, player, args);
        }
    }

    public static void info(String message, Player player, Object... args) {
        if (player != null) {
            player.sendMessage(translate(String.format("&e[IFR]&r " + message, args)));
        } else {
            info(message, args);
        }
    }
}

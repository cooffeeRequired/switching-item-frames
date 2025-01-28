package cz.coffee.utils;

import org.bukkit.ChatColor;

public class ChatUitls {
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

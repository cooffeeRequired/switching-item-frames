package cz.coffee;

import cz.coffee.listeners.AnvilListener;
import cz.coffee.listeners.StorageListener;
import cz.coffee.listeners.FrameListener;
import cz.coffee.utils.ChatUitls;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Main class for the ItemFrameRotator plugin.
 */
public final class ItemFrameRotator extends JavaPlugin {

    private FrameListener frameListener;

    @Override
    public void onEnable() {
        frameListener = new FrameListener();
        registerListeners();
        startItemRotationTask();
        logToConsole("Plugin enabled.");
    }

    @Override
    public void onDisable() {
        frameListener.clearData();
        logToConsole("Plugin disabled.");
    }

    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(frameListener, this);
        pluginManager.registerEvents(new AnvilListener(), this);
        pluginManager.registerEvents(new StorageListener(frameListener), this);
    }

    /**
     * Starts the task that rotates items in item frames periodically.
     */
    private void startItemRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                frameListener.rotateItems();
            }
        }.runTaskTimer(this, 0, 50);
    }

    /**
     * Logs a message to the console with a plugin-specific prefix.
     *
     * @param message The message to log.
     */
    private void logToConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatUitls.translate("&e[ItemFrameRotator] &r" + message));
    }
}
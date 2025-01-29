package cz.coffee;

import cz.coffee.commands.ItemFrameCommand;
import cz.coffee.listeners.AnvilListener;
import cz.coffee.listeners.FrameListener;
import cz.coffee.support.ChatUitls;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Main class for the ItemFrameRotator plugin.
 */
public final class ItemFrameRotator extends JavaPlugin {

    private static ItemFrameRotator plugin;
    private static final String PLUGIN_NAME = "ItemFrameRotator";
    public static boolean ROTATE_ITEMS = false;
    private final FrameListener frameListener = new FrameListener();

    public static ItemFrameRotator getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        registerListeners();
        startItemRotationTask();
        CustomItemFrame.registerRecipe(this);
        this.getCommand("itemframerotator").setExecutor(new ItemFrameCommand());
        logToConsole("Plugin enabled.");
    }

    @Override
    public void onDisable() {
        logToConsole("Plugin disabled.");
    }

    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(frameListener, this);
        pluginManager.registerEvents(new AnvilListener(), this);
        pluginManager.registerEvents(new CustomItemFrame.CreativeInventoryListener(), this);
    }

    /**
     * Starts the task that rotates items in item frames periodically.
     */
    private void startItemRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ROTATE_ITEMS) frameListener.rotateItems();
            }
        }.runTaskTimer(this, 0, 20);
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
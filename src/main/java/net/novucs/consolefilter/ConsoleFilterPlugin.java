package net.novucs.consolefilter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ConsoleFilterPlugin extends JavaPlugin implements Listener {

    private final List<Pattern> deniedPatterns = new ArrayList<>();

    private final Filter filter = record -> {
        String message = record.getMessage();

        if (message == null) {
            return true;
        }

        for (Pattern pattern : deniedPatterns) {
            if (pattern.matcher(message).matches()) {
                return false;
            }
        }

        return true;
    };

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        filterLogs();
        getServer().getScheduler().runTask(this, this::filterLogs);
    }

    @Override
    public void onDisable() {
        getServer().getLogger().setFilter(null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadConfig();
        loadSettings();
        sender.sendMessage(ChatColor.YELLOW + "Successfully reloaded the configuration.");
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        filterLogs();
    }

    private void loadSettings() {
        deniedPatterns.clear();

        for (String filter : getConfig().getStringList("log-filters")) {
            deniedPatterns.add(Pattern.compile(filter));
        }
    }

    private void filterLogs() {
        getLogger().setFilter(filter);
        getServer().getLogger().setFilter(filter);
        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            plugin.getLogger().setFilter(filter);
        }

        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setFilter(filter);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFilter(filter);
        }
    }
}

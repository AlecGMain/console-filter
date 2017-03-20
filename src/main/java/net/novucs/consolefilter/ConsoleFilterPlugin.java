package net.novucs.consolefilter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.regex.Pattern;

public class ConsoleFilterPlugin extends JavaPlugin {

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
        getServer().getLogger().setFilter(filter);
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

    private void loadSettings() {
        deniedPatterns.clear();

        for (String filter : getConfig().getStringList("log-filters")) {
            deniedPatterns.add(Pattern.compile(filter));
        }
    }
}

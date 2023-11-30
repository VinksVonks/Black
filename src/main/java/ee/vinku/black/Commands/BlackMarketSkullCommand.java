package ee.vinku.black.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class BlackMarketSkullCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public BlackMarketSkullCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("BlackMarketSkull reloaded!");
            } else if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
                String key = args[1];
                StringJoiner valueJoiner = new StringJoiner(" ");
                for (int i = 2; i < args.length; i++) {
                    valueJoiner.add(args[i]);
                }
                String value = valueJoiner.toString();
                try {
                    double doubleValue = Double.parseDouble(value);
                    plugin.getConfig().set(key, doubleValue);
                } catch (NumberFormatException e) {
                    plugin.getConfig().set(key, value);
                }
                sender.sendMessage("BlackMarketSkull updated!");
            } else if (args[0].equalsIgnoreCase("save")) {
                plugin.saveConfig();
                sender.sendMessage("BlackMarketSkull saved!");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("set");
            completions.add("save");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // Suggest configuration keys as completions
            ConfigurationSection config = plugin.getConfig();
            for (String key : config.getKeys(true)) {
                if (!config.isConfigurationSection(key)) {
                    completions.add(key);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String key = args[1];
            Object value = plugin.getConfig().get(key);
            if (value != null) {
                completions.add(value.toString());
            }
        }
        return completions;
    }
}

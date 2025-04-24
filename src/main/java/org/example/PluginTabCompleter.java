package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

public class PluginTabCompleter implements TabCompleter {

    private final ResidenceContractor plugin;

    public PluginTabCompleter(ResidenceContractor plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return null;
        }

        List<String> suggestions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("rescontract")) {
            if (args.length == 1) {
                if (sender.hasPermission("rescontract.reload")) {
                    suggestions.add("reload");
                }
                suggestions.add("accept");
                suggestions.add("deny");
                suggestions.add("view");
            } else if (args.length == 2 && !Objects.equals(args[0], "reload")){
                try {
                    suggestions.addAll(plugin.getDatabaseManager().getPLayerContract(sender));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return suggestions;
    }
}
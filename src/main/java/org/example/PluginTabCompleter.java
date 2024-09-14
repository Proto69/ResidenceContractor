package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class PluginTabCompleter implements TabCompleter {

    private ResidenceContractor plugin;

    public PluginTabCompleter(ResidenceContractor plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
            } else if (args.length == 2){
                suggestions.add("accept");
                suggestions.add("deny");
                suggestions.add("view");
            } else if (args.length == 3 && !Objects.equals(args[1], "view")){
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
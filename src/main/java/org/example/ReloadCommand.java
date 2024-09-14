package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ReloadCommand implements CommandExecutor {
    private final ResidenceContractor plugin;

    public ReloadCommand(ResidenceContractor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checking the sender for required permission
        if (sender.hasPermission("rescontract.reload")) {

            // Getting the time before the reload
            long before = System.currentTimeMillis();

            // Reloading the plugin
            try {
                plugin.reload();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Getting the time after the reload
            long after = System.currentTimeMillis();

            long time = after - before;

            Map<String, String> map = new HashMap<>();
            map.put("time", String.valueOf(time));

            UsefulMethods.sendMessage(sender, map, "reloaded");
        }
        return true;
    }
}
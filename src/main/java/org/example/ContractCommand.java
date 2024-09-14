package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ContractCommand implements CommandExecutor {

    private ResidenceContractor plugin;

    public ContractCommand(ResidenceContractor plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2){
            Map<String, String> map = new HashMap<>();
            UsefulMethods.sendMessage(sender, map, "missing-residence-name");
            return true;
        }

        if (sender instanceof Player player) {
            String residenceName = args[1];
            if (args[0].equalsIgnoreCase("accept")) {

                try {
                    ContractAcceptanceTracker.setResponse(player, true, residenceName, plugin);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return true;
            } else if (args[0].equalsIgnoreCase("deny")) {

                try {
                    ContractAcceptanceTracker.setResponse(player, false, residenceName, plugin);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("view")){
                BookUtil.showContractBook(player, residenceName);
            }
        }
        return false;
    }
}

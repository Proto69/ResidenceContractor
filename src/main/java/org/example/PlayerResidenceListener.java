package org.example;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerResidenceListener implements Listener {

    private final ResidenceContractor plugin;

    public PlayerResidenceListener(ResidenceContractor plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onResidenceFlagChangeEvent(ResidenceCommandEvent event) throws SQLException {
        String[] args = event.getArgs();
        if (Objects.equals(args[0], "padd")){
            CommandSender sender = event.getSender();
            if (args.length == 2){
                if (!(sender instanceof Player playerSender)){
                    return;
                }

                Player player = Bukkit.getPlayer(args[1]);

                if (Bukkit.getPlayer(args[1]) == null && !Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
                    return;
                }

                Location loc = playerSender.getLocation();
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);

                if (res == null){
                    return;
                }

                if (!sender.getName().equals(res.getOwner())){
                    return;
                }

                Map<String, String> map = new HashMap<>();
                map.put("playerName", args[1]);
                map.put("residenceName", res.getResidenceName());
                map.put("residenceOwnerName", sender.getName());

                if (player == null) {
                    UsefulMethods.sendMessage(sender, map, "offline-player");
                    return;
                }
                String[] data = new String[] {res.getResidenceName(), player.getName()};
                plugin.getDatabaseManager().uploadContractData(data, sender);

                ContractAcceptanceTracker.trackContractResponse(player, res.getResidenceName(), plugin, sender);
                BookUtil.showContractBook(player, res.getResidenceName());

            } else {
                if (Bukkit.getPlayer(args[2]) == null && !Bukkit.getOfflinePlayer(args[2]).hasPlayedBefore()){
                    return;
                }

                Player player = Bukkit.getPlayer(args[2]);
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(args[1]);

                if (res == null){
                    return;
                }

                if (!sender.getName().equals(res.getOwner())){
                    return;
                }

                Map<String, String> map = new HashMap<>();
                map.put("playerName", args[2]);
                map.put("residenceName", res.getResidenceName());
                map.put("residenceOwnerName", sender.getName());

                if (player == null){
                    UsefulMethods.sendMessage(sender, map, "offline-player");
                    return;
                }

                String[] data = new String[] {res.getResidenceName(), player.getName()};
                plugin.getDatabaseManager().uploadContractData(data, sender);

                ContractAcceptanceTracker.trackContractResponse(player, res.getResidenceName(), plugin, sender);
                BookUtil.showContractBook(player, res.getResidenceName());
            }
        }
        else if (Objects.equals(args[0], "pdel")){
            CommandSender sender = event.getSender();
            if (args.length == 2){

                if (!(sender instanceof Player playerSender)){
                    return;
                }

                if (Bukkit.getPlayer(args[1]) == null && !Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
                    return;
                }

                Location loc = playerSender.getLocation();
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);

                if (res == null){
                    return;
                }

                if (!sender.getName().equals(res.getOwner())){
                    return;
                }

                plugin.getDatabaseManager().deleteContractData(res.getResidenceName(), args[1]);

            } else {
                if (Bukkit.getPlayer(args[2]) == null && !Bukkit.getOfflinePlayer(args[2]).hasPlayedBefore()){
                    return;
                }

                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(args[1]);

                if (res == null){
                    return;
                }

                if (!sender.getName().equals(res.getOwner())){
                    return;
                }

                plugin.getDatabaseManager().deleteContractData(res.getResidenceName(), args[2]);
            }
        }
    }
}

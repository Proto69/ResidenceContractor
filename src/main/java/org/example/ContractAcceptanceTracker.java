package org.example;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.SQLException;
import java.util.*;

public class ContractAcceptanceTracker {

    public static void trackContractResponse(Player player, String residenceName, ResidenceContractor plugin, CommandSender sender) {
        // Start a repeated task to check if the player responded or closed the book
        new BukkitRunnable() {
            @Override
            public void run() {

                    boolean response;
                    try {
                        response = plugin.getDatabaseManager().getContractData(residenceName, sender.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                    if (response){
                        this.cancel();
                    } else {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(residenceName);
                        res.getPermissions().removeAllPlayerFlags(player.getName());
                        sendPrompt(player, residenceName);
                    }
            }
        }.runTaskTimer(plugin, 0, 20 * 10); // Check every 10 seconds
    }

    // Called when the player responds to the contract
    public static void setResponse(Player player, boolean accepted, String residenceName, ResidenceContractor plugin) throws SQLException {

        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(residenceName);

        if (res == null){
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("playerName", player.getName());
        map.put("residenceName", res.getResidenceName());
        map.put("residenceOwnerName", res.getOwner());

        List<String> residences = plugin.getDatabaseManager().getPLayerContract(player);

        if (!residences.contains(res.getResidenceName())){
            UsefulMethods.sendMessage(player, map, "no-unsigned-contract");
            return;
        }

        Player owner = Bukkit.getPlayer(res.getOwnerUUID());

        int is_accepted = accepted ? 1 : 0;
        String[] data = new String[] { String.valueOf(is_accepted), res.getResidenceName(), player.getName()};

        DatabaseManager manager = plugin.getDatabaseManager();
        manager.signContract(data, player);

        if (accepted) {
            UsefulMethods.sendMessage(player, map, "accepting-contract-player");
            res.getPermissions().applyDefaultFlags(player, false);
            assert owner != null;
            UsefulMethods.sendMessage(owner, map, "accepting-contract-owner");
            ItemStack item = BookUtil.getBook(player, residenceName, UsefulMethods.readConfig("book.title-accepted"), UsefulMethods.readConfig("book.author"));
            if (UsefulMethods.hasEnoughSpace(player, item) && Objects.equals(UsefulMethods.readConfig("book.give-to-player"), "true")){
                player.getInventory().addItem(item);
            }
            if (UsefulMethods.hasEnoughSpace(owner, item) && Objects.equals(UsefulMethods.readConfig("book.give-to-owner"), "true")){
                owner.getInventory().addItem(item);
            }
        } else {
            UsefulMethods.sendMessage(player, map, "denying-contract-player");
            assert owner != null;
            UsefulMethods.sendMessage(owner, map, "denying-contract-owner");
            manager.deleteContractData(residenceName, player.getName());

            res.getPermissions().removeAllPlayerFlags(player.getName());
        }
    }

    private static void sendPrompt(Player player, String residenceName) {
        Map<String, String> map = new HashMap<>();
        map.put("playerName", player.getName());
        map.put("residenceName", residenceName);
        map.put("residenceOwnerName", Residence.getInstance().getResidenceManager().getByName(residenceName).getOwner());

        UsefulMethods.sendMessage(player, map, "prompt");

        TextComponent acceptButton = new TextComponent(ColorUtil.translateHexColorCodes(UsefulMethods.getMessage("accept-contract")));
        TextComponent denyButton = new TextComponent(ColorUtil.translateHexColorCodes(UsefulMethods.getMessage("deny-contract")));
        TextComponent viewButton = new TextComponent(ColorUtil.translateHexColorCodes(UsefulMethods.getMessage("view-contract")));

        // Set the click event to run a command when clicked
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rescontract accept " + residenceName));
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rescontract deny " + residenceName));
        viewButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rescontract view " + residenceName));

        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ColorUtil.translateHexColorCodes(UsefulMethods.readConfig("messages.accept-contract-hover"))).create()));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ColorUtil.translateHexColorCodes(UsefulMethods.readConfig("messages.deny-contract-hover"))).create()));
        viewButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ColorUtil.translateHexColorCodes(UsefulMethods.readConfig("messages.view-contract-hover"))).create()));

        player.spigot().sendMessage(acceptButton);
        player.spigot().sendMessage(denyButton);
        player.spigot().sendMessage(viewButton);
    }
}

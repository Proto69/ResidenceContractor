package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public class ResidenceContractor extends JavaPlugin {
    public static FileConfiguration config;
    private DatabaseManager databaseManager;

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    @Override
    public void onEnable() {

        if (getServer().getPluginManager().getPlugin("Residence") == null) {
            // Dependency not found, log a warning and disable the plugin
            getLogger().severe("Dependency 'Residence' not found! Disabling ResidenceContractor...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("'Residence' dependency found!");
        // Copy the config.yml in the plugin configuration folder if it doesn't exist.
        saveDefaultConfig();

        config = this.getConfig();

        boolean enable = getConfig().getBoolean("enable");

        if (enable) {
            // Registering the commands
            registerCommands();

            try {
                databaseEnable();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            registerEvents();

            // Logging the successful enabling of the plugin
            getLogger().info("ResidenceContractor is enabled!");
        } else {
            getLogger().severe("ResidenceContractor is not enabled in the config!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rescontract")) {
            if (args.length == 0) {
                String message = UsefulMethods.getMessage("usage");
                sender.sendMessage(ColorUtil.translateHexColorCodes(message));
                return true;
            }

            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("reload")) {
                ReloadCommand reload = new ReloadCommand(this);
                reload.onCommand(sender, command, label, args);
            } else if (subCommand.equals("accept") || subCommand.equals("deny") || (subCommand.equals("view") && sender instanceof Player)) {
                ContractCommand contractCommand = new ContractCommand(this);
                contractCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage("Unknown subcommand.");
            }
            return true;
        }

        return false;
    }

    // Reloads the plugin
    public void reload() throws SQLException {
        // Reloads the configuration
        reloadConfig();

        databaseDisable();

        HandlerList.unregisterAll(this);

        config = this.getConfig();

        if (getConfig().getBoolean("enable"))
        {
            // Registers all events
            registerEvents();

            // Connect the database
            databaseEnable();
        }
    }

    private void databaseEnable() throws SQLException {
        databaseManager = new DatabaseManager(getConfig(), getLogger());

        databaseManager.connect();
    }

    private void databaseDisable(){
        try {
            databaseManager.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerEvents(){
        getServer().getPluginManager().registerEvents(new PlayerResidenceListener(this), this);
    }

    private void registerCommands() {
        // Registers the reload command
        Objects.requireNonNull(this.getCommand("rescontract")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("rescontract")).setTabCompleter(new PluginTabCompleter(this));
    }

}
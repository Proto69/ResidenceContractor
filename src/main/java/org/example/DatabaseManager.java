package org.example;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseManager {
    private Connection connection;
    private final FileConfiguration config;

    private final Logger logger;

    public DatabaseManager(FileConfiguration config, Logger logger){
        this.config = config;
        this.logger = logger;
    }

    public void connect() {
        String url = "jdbc:mysql://" + config.getString("database.address") + "/" + config.getString("database.database") + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
        String user = config.getString("database.username");
        String password = config.getString("database.password");
        try {
            connection = DriverManager.getConnection(url, user, password);
            createTableIfNotExists();
            Bukkit.getLogger().info("Successfully connected to the database.");
        } catch (SQLException e) {
            // Log the error and continue loading the plugin
            Bukkit.getLogger().severe("Failed to connect to the database! Plugin will continue to load, but some features may not work.");
            e.printStackTrace();
            // Optionally, you can set connection to null or handle further logic
            connection = null;
        }
    }

    public void createTableIfNotExists() throws SQLException {
        String tableName = "rescontract_contracts";
        if (!doesTableExist(tableName)) {
            String query = "CREATE TABLE " + tableName + "(\n" +
                    "\tid int primary key auto_increment,\n" +
                    "    residence_name text not null,\n" +
                    "    player_name text not null,\n" +
                    "    is_accepted bool not null default false, \n" +
                    "    is_signed bool not null default false, \n" +
                    "    signed_at datetime \n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
            String query2 = "ALTER DATABASE " + config.getString("database.database") + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(query);
                statement.executeUpdate(query2);
                logger.info("Database table was created!");
            } catch (SQLException e) {
                logger.severe("Error creating table: " + e.getMessage());
                throw e;
            }
        } else {
            logger.info("Table already exists.");
        }
    }

    private boolean doesTableExist(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }


    public void uploadContractData(String[] data, CommandSender sender) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM rescontract_contracts WHERE residence_name = ? AND player_name = ?";
        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
        checkStatement.setString(1, data[0]);
        checkStatement.setString(2, data[1]);

        ResultSet resultSet = checkStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1); // Get the count of matching records

        resultSet.close();
        checkStatement.close();

        Map<String, String> map = new HashMap<>();
        map.put("playerName", data[1]);
        map.put("residenceName", data[0]);
        map.put("residenceOwnerName", sender.getName());

        if (count > 0) {
            UsefulMethods.sendMessage(sender, map, "contract-exists");
            return; // Don't insert if record already exists
        }

        // Insert the new record if no match was found
        String query = "INSERT INTO rescontract_contracts (residence_name, player_name) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, data[0]);
        statement.setString(2, data[1]);

        int num = statement.executeUpdate();
        if (num != 1) {
            UsefulMethods.sendMessage(sender, map, "error-creating-contract");
        } else {
            UsefulMethods.sendMessage(sender, map, "success-creating-contract");
        }

        statement.close();
    }


    public List<String> getPLayerContract(CommandSender sender) throws SQLException {
        String query = "SELECT residence_name FROM rescontract_contracts WHERE player_name = ? AND is_signed = false";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, sender.getName());

        ResultSet resultSet = statement.executeQuery();

        // Correct List initialization
        List<String> result = new ArrayList<>();

        // Loop through all rows in the ResultSet
        while (resultSet.next()) {
            result.add(resultSet.getString("residence_name"));
        }

        // Always close ResultSet and Statement to avoid memory leaks
        resultSet.close();
        statement.close();

        return result;
    }

    public void signContract(String[] data, CommandSender sender) throws SQLException {
        String query = "UPDATE rescontract_contracts SET is_accepted = ?, is_signed = true, signed_at = now() WHERE residence_name = ? AND player_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, data[0]);
        statement.setString(2, data[1]);
        statement.setString(3, data[2]);

        int num = statement.executeUpdate();

        Map<String, String> map = new HashMap<>();
        map.put("playerName", sender.getName());
        map.put("residenceName", data[1]);
        map.put("residenceOwnerName", Residence.getInstance().getResidenceManager().getByName(data[1]).getOwner());

        // Check if the update was successful
        if (num != 1) {
            UsefulMethods.sendMessage(sender, map, "error-signing-contract");
        }

        // Close the statement to avoid memory leaks
        statement.close();
    }


    public void deleteContractData(String residenceName, String playerName) throws SQLException {
        String query = "DELETE FROM rescontract_contracts \n" +
                " WHERE residence_name = ? AND player_name = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, residenceName);
            statement.setString(2, playerName);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Update successful. Rows affected: " + rowsUpdated);
            } else {
                logger.warning("No rows matched the criteria.");
            }
        }
    }

    public boolean getContractData(String residenceName, String playerName) throws SQLException {
        String query = "SELECT * FROM rescontract_contracts WHERE residence_name = ? AND player_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, residenceName);
        statement.setString(2, playerName);

        ResultSet resultSet = statement.executeQuery();

        boolean isSigned = true;  // Default to true if no record is found
        if (resultSet.next()) {
            isSigned = resultSet.getBoolean("is_signed");
        }

        resultSet.close();
        statement.close();

        return isSigned;
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
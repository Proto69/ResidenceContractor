package org.example;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BookUtil{
    public static void showContractBook(Player player, String residenceName) {

        ItemStack book = getBook(player, residenceName, "Contract", "Admin");

        // Force open the book for the player (Written Book)
        player.openBook(book);
    }

    public static ItemStack getBook(Player player, String residenceName, String title, String author){
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(residenceName);

        // Create a Written Book (uneditable)
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) writtenBook.getItemMeta();

        // Set book title, author, and content
        assert meta != null;
        meta.setTitle(title);
        meta.setAuthor(author);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, String> map = new HashMap<>();
        map.put("playerName", player.getName());
        map.put("residenceName", res.getResidenceName());
        map.put("residenceOwnerName", res.getOwner());
        map.put("dateTime", LocalDateTime.now().format(formatter));

        String pageContent = UsefulMethods.replacePlaceholders(UsefulMethods.readConfig("book.content"), map);

        // Set the book's content using JSON for clickable buttons
        meta.addPage(pageContent);

        writtenBook.setItemMeta(meta);

        return writtenBook;
    }
}

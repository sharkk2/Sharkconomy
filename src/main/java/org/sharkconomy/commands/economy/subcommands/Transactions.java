package org.sharkconomy.commands.economy.subcommands;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Transactions {
    public void execute(Player player, String[] args) {
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number!");
                return;
            }
        }

        UUID playerUUID = player.getUniqueId();
        List<JsonObject> transactions = PlayerData.getTransactions(playerUUID);

        int totalTransactions = transactions.size();
        int totalPages = (int) Math.ceil(totalTransactions / 5.0);
        if (page < 1 || page > totalPages) {
            player.sendMessage(ChatColor.RED + "Invalid page number! Total pages: " + totalPages);
            return;
        }

        player.sendMessage("§7A list of all the transactions that have sent to you:");
        player.sendMessage("§7This is page §f(§6" + page + "§f/§6" + totalPages + "§f)");
        player.sendMessage("§7Type §d/eco transactions <n>§7 where n is the page");
        player.sendMessage("§7=========================================");

        int startIndex = (page - 1) * 5;
        int endIndex = Math.min(startIndex + 5, totalTransactions);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy: h:mm a");

        for (int i = startIndex; i < endIndex; i++) {
            JsonObject transaction = transactions.get(i);
            double amount = transaction.get("amount").getAsDouble();
            String reason = transaction.get("reason").getAsString();
            long timestamp = transaction.get("date").getAsLong();
            int type = transaction.get("type").getAsInt();
            UUID otherPlayerUUID = UUID.fromString(type == 1 ? transaction.get("fromPlayer").getAsString() : transaction.get("toPlayer").getAsString());
            OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerUUID);

            String transactionType = (type == 1) ? "§a> Received: " : "§c< Sent: ";
            String formattedDate = dateFormat.format(timestamp);
            String formattedAmount = "§6" + amount + "sc";

            String message = transactionType + formattedAmount +
                    (type == 1 ? " §ffrom " : " §f to ") + "§b" + otherPlayer.getName() +
                    "§f: §8" + formattedDate;
            player.sendMessage(message);
            player.sendMessage("§7   Transaction reason: §7" + reason);
        }

        player.sendMessage("§7=========================================");
    }
}

package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;

import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard {
    public void execute(Player player, String[] args) {
        // Retrieve all player balances from PlayerData
        Map<UUID, Double> allBalances = PlayerData.getAllBalances();

        // Convert to a sorted list (highest balance first)
        List<Map.Entry<UUID, Double>> sortedBalances = allBalances.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Send leaderboard message
        player.sendMessage(ChatColor.GRAY + "Richest Server Players:");
        int rank = 1;

        for (Map.Entry<UUID, Double> entry : sortedBalances) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            double balance = entry.getValue();

            // Formatting based on rank
            String rankColor;
            switch (rank) {
                case 1 -> rankColor = ChatColor.AQUA.toString();
                case 2 -> rankColor = ChatColor.GREEN.toString() + ChatColor.GRAY;
                case 3 -> rankColor = ChatColor.GOLD.toString();
                default -> rankColor = ChatColor.DARK_GRAY.toString();
            }

            player.sendMessage(rankColor + rank + ". " + ChatColor.WHITE + playerName + ": " + ChatColor.GOLD + balance + "sc");
            rank++;
        }
    }
}

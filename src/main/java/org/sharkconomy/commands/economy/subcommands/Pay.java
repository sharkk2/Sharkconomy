package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;
import org.bukkit.ChatColor;


public class Pay {
    public void execute(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /economy pay <player> <amount>");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be larger than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a valid number!");
            return;
        }

        double balance = PlayerData.getBalance(player.getUniqueId());

        OfflinePlayer target = Bukkit.getPlayer(args[1]) != null
                ? Bukkit.getPlayer(args[1])
                : Bukkit.getOfflinePlayer(args[1]);

        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(ChatColor.RED + "Couldn't find player: " + args[1]);
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can't pay yourself!");
            return;
        }

        if (balance < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough sharcoins!");
            return;
        }

        // Adjust balances
        double targetBalance = PlayerData.getBalance(target.getUniqueId());
        PlayerData.setBalance(player.getUniqueId(), balance - amount);
        PlayerData.setBalance(target.getUniqueId(), targetBalance + amount);

        player.sendMessage(ChatColor.GREEN + "You've transferred §6§l" + amount + "sc§f to §2" + target.getName());

        // If the player is online, notify and play sound
        if (target.isOnline() && target.getPlayer() != null) {
            Player onlineTarget = target.getPlayer();
            onlineTarget.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            onlineTarget.sendMessage(ChatColor.GOLD + player.getName() + " has transferred you §6§l" + amount + "sc§r, say thanks!");
        }
    }
}

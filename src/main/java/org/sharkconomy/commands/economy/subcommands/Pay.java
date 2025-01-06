package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.Bukkit;
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


        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be larger than 0!");
                return;
            }

            double balance = PlayerData.getBalance(player.getUniqueId());
            Player target = Bukkit.getPlayer(args[1]);

            double target_balance = PlayerData.getBalance(target.getUniqueId());

            if (player.getUniqueId().equals(target.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You can't pay yourself!");
                return;
            }

            if (balance < amount) {
                player.sendMessage(ChatColor.RED + "You don't have enough sharkcoins!");
                return;
            }
            balance -= amount;
            target_balance += amount;

            PlayerData.setBalance(player.getUniqueId(), balance);
            PlayerData.setBalance(target.getUniqueId(), target_balance);

            player.sendMessage("You've transferred §6§l" + amount + "sc§f to§k§2 " + target.getName());

            target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            target.sendMessage("§2§l" + player.getName() + "§f§r has transferred you §6§l" + amount +"sc§r, say thanks!");


        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Amount must be a number!");
        }

    }
}
package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;
import org.bukkit.ChatColor;
import org.sharkconomy.utils.SharkCoin;

public class Withdraw {
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /economy withdraw <amount>");
            return;
        }

        double balance = PlayerData.getBalance(player.getUniqueId());

        try {
            double amount = Double.parseDouble(args[1]);

            if (balance < amount) {
                player.sendMessage(ChatColor.RED + "You don't have that much Sharcoins!");
                return;
            }

            if (balance <= 0) {
                player.sendMessage(ChatColor.RED + "You cant withdraw less than 1 Sharcoin!");
                return;
            }

            balance -= amount;
            PlayerData.setBalance(player.getUniqueId(), balance);
            int intamount = (int) Math.round(amount);
            SharkCoin.giveCoin(player, intamount);
            player.sendMessage("Withdrawed §6§l" + intamount +"sc§r from your bank.");


        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a number!");

        }
    }
}

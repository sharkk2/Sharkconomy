package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;
import org.bukkit.ChatColor;
import org.sharkconomy.utils.SharkCoin;

public class Deposit {
    public void execute(Player player, String[] args) {
        try {
            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "Usage: /economy deposit <amount>");
                return;
            }

            double amount = Double.parseDouble(args[1]);

            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "You cant deposit less than 1 Sharcoin!");
                return;
            }
            int intamount = (int) Math.round(amount);
            boolean taken = SharkCoin.takeCoin(player, intamount);

            if (!taken) {
                player.sendMessage(ChatColor.RED + "You don't have enough Sharcoins in your inventory!");
                return;
            }

            double balance = PlayerData.getBalance(player.getUniqueId());
            balance += intamount;
            PlayerData.setBalance(player.getUniqueId(), balance);
            player.sendMessage("Deposited §6§l" + intamount +"sc§r to your bank.");


        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a number!");

        }
    }
}

package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;

public class Help {
    public void execute(Player player, String[] args) {
        String msg = "Current commands:\n" +
                "  §k§1/economy balance: §o§7 Shows your current balance.\n" +
                "  §k§1/economy pay: §o§7 Pay a player. §oargs: < player, coins >\n" +
                "  §k§1/economy help: §o§7 Shows this message.\n" +
                "  §k§1/economy beg: §o§7 Requests coins. < player, coins >\n" +
                "  §k§1/economy leaderboard: §o§7 Top richest players < player, coins >\n" +
                "  §k§1/economy withdraw: §o§7 Withdraw coins from your bank\n" +
                "  §k§1/economy deposit: §o§7 Deposit coins to your bank.";
        player.sendMessage(msg);
    }
}

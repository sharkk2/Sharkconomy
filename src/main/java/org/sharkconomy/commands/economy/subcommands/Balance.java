package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;


public class Balance {
    public void execute(Player player, String[] args) {
        double balance = PlayerData.getBalance(player.getUniqueId());
        player.sendMessage("Your current balance: §6§l" + balance + "sc§r §o§7as " + player.getName() + ".");
    }
}
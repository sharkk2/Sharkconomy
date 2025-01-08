package org.sharkconomy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EconomyTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();


        if (!(sender instanceof Player)) {
            return suggestions;
        }

        if (args.length == 1) {
            suggestions.add("balance");
            suggestions.add("help");
            suggestions.add("pay");
            suggestions.add("leaderboard");
            suggestions.add("withdraw");
            suggestions.add("deposit");
            suggestions.add("sell");
            suggestions.add("buy");
        }

        else if (args.length == 2 && "pay".equalsIgnoreCase(args[0])) {
            for (Player player : sender.getServer().getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
        }

        else if (args.length == 2 && "sell".equalsIgnoreCase(args[0])) {
            Player player = (Player) sender;
            Inventory inventory = player.getInventory();

            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    suggestions.add(item.getType().name());
                }
            }
        }

        else if (args.length == 3 && "sell".equalsIgnoreCase(args[0])) {
            suggestions.add("1");
            suggestions.add("2");
            suggestions.add("5");
            suggestions.add("10");
        }


        else if (args.length == 4 && "sell".equalsIgnoreCase(args[0])) {
            suggestions.add("10");
            suggestions.add("30");
            suggestions.add("50");
            suggestions.add("100");
        }


        else if (args.length == 2 && "withdraw".equalsIgnoreCase(args[0])) {
            suggestions.add("5");
            suggestions.add("10");
            suggestions.add("25");
            suggestions.add("50");
            suggestions.add("100");
        }

        else if (args.length == 2 && "deposit".equalsIgnoreCase(args[0])) {
            suggestions.add("5");
            suggestions.add("10");
            suggestions.add("25");
            suggestions.add("50");
            suggestions.add("100");
        }

        else if (args.length == 3 && "pay".equalsIgnoreCase(args[0])) {
            suggestions.add("5");
            suggestions.add("10");
            suggestions.add("25");
            suggestions.add("50");
            suggestions.add("100");
        }

        return suggestions;
    }
}

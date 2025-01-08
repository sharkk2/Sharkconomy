package org.sharkconomy.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SharkCoin {

    public static ItemStack getSharkCoin() {
        ItemStack sharkCoin = new ItemStack(Material.PRISMARINE_SHARD, 1);

        ItemMeta meta = sharkCoin.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Sharcoin");

            meta.setUnbreakable(true);
            meta.setEnchantmentGlintOverride(true);

            meta.setCustomModelData(1);
        }

        sharkCoin.setItemMeta(meta);
        return sharkCoin;
    }

    // Method to give the coin to a player
    public static void giveCoin(Player player, int amount) {
        ItemStack coin = getSharkCoin();
        coin.setAmount(amount);
        player.getInventory().addItem(coin);
    }

    public static boolean takeCoin(Player player, int amount) {
        int coinsRemoved = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PRISMARINE_SHARD) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getDisplayName().equals(ChatColor.GOLD + "Sharcoin") &&
                        meta.hasCustomModelData() && meta.getCustomModelData() == 1) {

                    int stackAmount = item.getAmount();
                    if (stackAmount > amount - coinsRemoved) {
                        item.setAmount(stackAmount - (amount - coinsRemoved));
                        return true;
                    } else {
                        coinsRemoved += stackAmount;
                        player.getInventory().removeItem(item);
                    }

                    if (coinsRemoved >= amount) {
                        return true;
                    }
                }
            }
        }
        return false; // Not enough coins
    }
}

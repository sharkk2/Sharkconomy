package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.sharkconomy.utils.PlayerData;
import org.bukkit.Material;
import org.sharkconomy.utils.nameFormat;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class Sell {
    private static final int MAX_SLOTS = 54; // Maximum slots available in the shop inventory

    public void execute(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /sell <item> <quantity> <priceEach>");
            return;
        }

        Material itemType = Material.getMaterial(args[1].toUpperCase());

        if (itemType == null) {
            player.sendMessage(ChatColor.RED + "Invalid item type!");
            return;
        }

        try {
            int quantity = Integer.parseInt(args[2]);
            int price = Integer.parseInt(args[3]);

            if (quantity <= 0 || price <= 0) {
                player.sendMessage(ChatColor.RED + "Quantity and price can't be less than or equal to 0!");
                return;
            }


            if (player.getInventory().contains(itemType, quantity)) {
                if (hasEnchantedItems(player, itemType, quantity)) {
                    player.sendMessage(ChatColor.RED + "You can't sell enchanted items!");
                    return;
                }


                if (itemType.getMaxStackSize() == 1) {
                    int unstackableCount = countUnstackableItems(player, itemType);
                    if (unstackableCount < quantity) {
                        player.sendMessage(ChatColor.RED + "You don't have " + quantity + " " + nameFormat.formatItemName(itemType) + "(s) in your inventory!");
                        return;
                    }
                    removeUnstackableItems(player, itemType, quantity);
                } else {
                    player.getInventory().removeItem(new ItemStack(itemType, quantity));
                }

                if (PlayerData.hasItem(player.getUniqueId(), itemType.name()) && itemType.getMaxStackSize() > 1) {
                    PlayerData.addItemQuantity(player.getUniqueId(), itemType.name(), quantity);
                    PlayerData.editPrice(player.getUniqueId(), itemType.name(), price);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    player.sendMessage("You've added §6" + quantity + " more " + nameFormat.formatItemName(itemType) + "(s) §fto your shop and updated the price to §b" + price + "sc§f.");
                } else {
                    PlayerData.addItemToShop(player.getUniqueId(), itemType.name(), quantity, price);
                    player.sendMessage("You've added §6" + quantity + " " + nameFormat.formatItemName(itemType) + "(s) §fto your shop for §b" + price + "sc§f.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

            } else {
                player.sendMessage(ChatColor.RED + "You don't have " + quantity + " " + nameFormat.formatItemName(itemType) + "(s) in your inventory!");
            }

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Quantity and price must be a number!");
        }
    }



    private int countUnstackableItems(Player player, Material itemType) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemType) {
                count++;
            }
        }
        return count;
    }

    private boolean hasEnchantedItems(Player player, Material itemType, int quantity) {
        int enchantedCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemType && item.getEnchantments().size() > 0) {
                enchantedCount++;
            }
            if (enchantedCount >= quantity) {
                return true;
            }
        }
        return false;
    }

    private void removeUnstackableItems(Player player, Material itemType, int quantity) {
        int removed = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemType) {
                player.getInventory().removeItem(item);
                removed++;
                if (removed == quantity) {
                    break;
                }
            }
        }
    }
}

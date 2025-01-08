package org.sharkconomy.commands.economy.subcommands;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.sharkconomy.utils.PlayerData;
import org.sharkconomy.utils.nameFormat;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.util.*;

public class Buy implements Listener {

    public void execute(Player player, String[] args) {
        Inventory shopMenu = Bukkit.createInventory(null, 27, "Shops");

        for (UUID uuid : PlayerData.getAllBalances().keySet()) {
            if (PlayerData.getPlayerData(uuid).has("shop")) {
                JsonObject shop = PlayerData.getShop(uuid);
                if (shop.size() > 0) { // Check if the shop has any items
                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                    skullMeta.setDisplayName(ChatColor.AQUA + Bukkit.getOfflinePlayer(uuid).getName());
                    skull.setItemMeta(skullMeta);
                    shopMenu.addItem(skull);
                }
            }
        }

        player.openInventory(shopMenu);
    }

    @EventHandler
    public void onShopMenuClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory != null && event.getView().getTitle().equals("Shops")) {
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                // If the clicked item is a player head (shop), open the player's shop
                SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
                if (meta != null) {
                    UUID sellerUUID = Bukkit.getOfflinePlayer(meta.getOwningPlayer().getUniqueId()).getUniqueId();
                    openPlayerShop(player, sellerUUID); // Open the player's shop
                }
            }
        }
    }

    private void openPlayerShop(Player buyer, UUID sellerUUID) {
        Inventory playerShop = Bukkit.createInventory(null, 54, "Shop: " + Bukkit.getOfflinePlayer(sellerUUID).getName());

        JsonObject shop = PlayerData.getShop(sellerUUID);
        for (Map.Entry<String, JsonElement> entry : shop.entrySet()) {
            String itemName = entry.getKey();
            JsonObject itemData = entry.getValue().getAsJsonObject();
            int quantity = itemData.get("quantity").getAsInt();
            int price = itemData.get("price").getAsInt();

            Material material = Material.getMaterial(itemName.toUpperCase());
            if (material == null) continue;

            while (quantity > 0) {
                int stackQuantity = Math.min(quantity, material.getMaxStackSize());
                ItemStack item = new ItemStack(material, stackQuantity);
                ItemMeta meta = item.getItemMeta();

                if (quantity == 0) {
                    meta.setDisplayName(ChatColor.RED + "§l§oOUT OF STOCK");
                    meta.setLore(Collections.singletonList(ChatColor.GRAY + "Price: N/A"));
                } else {
                    meta.setDisplayName(ChatColor.YELLOW + nameFormat.formatItemName(material));
                    meta.setLore(Arrays.asList(
                            ChatColor.GRAY + "Price: " + ChatColor.GOLD + price + " SC",
                            ChatColor.GRAY + "Quantity: " + ChatColor.AQUA + quantity
                    ));
                }
                item.setItemMeta(meta);
                playerShop.addItem(item);

                quantity -= stackQuantity;
            }
        }

        buyer.openInventory(playerShop);
    }



    @EventHandler
    public void onPlayerShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player buyer = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory != null && event.getView().getTitle().startsWith("Shop: ")) {
            event.setCancelled(true);

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            UUID sellerUUID = getSellerFromTitle(event.getView().getTitle());

            if (clickedItem.getItemMeta().getLore() == null) {
                return;
            }

            double price = Double.parseDouble(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0).split(" ")[1]));
            int quantity = Integer.parseInt(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(1).split(" ")[1]));

            if (quantity > 0 && PlayerData.getBalance(buyer.getUniqueId()) >= price) {

                PlayerData.setBalance(buyer.getUniqueId(), PlayerData.getBalance(buyer.getUniqueId()) - price);
                PlayerData.setBalance(sellerUUID, PlayerData.getBalance(sellerUUID) + price);
                PlayerData.decreaseItemQuantity(sellerUUID, clickedItem.getType().name(), 1);
                buyer.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));

                OfflinePlayer seller = Bukkit.getOfflinePlayer(sellerUUID);
                if (seller.isOnline()) {
                    Player onlineSeller = seller.getPlayer();
                    onlineSeller.sendMessage("§a" + buyer.getName() + "§f has purchased a §b§l" + nameFormat.formatItemName(clickedItem.getType()) + "§r from your shop for §6" + price + "sc§f.");
                } else {
                    Bukkit.getLogger().info(buyer.getName() + " bought from " + seller.getName() + "'s shop while offline.");
                }

                buyer.sendMessage("You bought 1 §b§l" + clickedItem.getType().name() + "§r for §6" + price + "sc§r.");
                buyer.playSound(buyer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                updateShopInventoryDisplay(buyer, sellerUUID, clickedItem);
            } else {
                buyer.sendMessage(ChatColor.RED + "Not enough balance or item out of stock!");
            }
        }
    }

    private UUID getSellerFromTitle(String title) {
        // Extract the seller UUID from the inventory title
        String sellerName = title.replace("Shop: ", "");
        for (UUID uuid : PlayerData.getAllBalances().keySet()) {
            if (Bukkit.getOfflinePlayer(uuid).getName().equals(sellerName)) {
                return uuid;
            }
        }
        return null;  
    }

    private void updateShopInventoryDisplay(Player buyer, UUID sellerUUID, ItemStack clickedItem) {
        Inventory playerShop = buyer.getOpenInventory().getTopInventory();
        for (ItemStack item : playerShop.getContents()) {
            if (item != null && item.getType() == clickedItem.getType()) {
                // Decrease the item quantity in the UI
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    int currentQuantity = item.getAmount();
                    int newQuantity = currentQuantity - 1;
                    if (newQuantity > 0) {
                        item.setAmount(newQuantity);
                        meta.setLore(Arrays.asList(
                                ChatColor.GRAY + "Price: " + ChatColor.GOLD + getPriceFromLore(item) + " SC",
                                ChatColor.GRAY + "Quantity: " + ChatColor.AQUA + newQuantity
                        ));
                        item.setItemMeta(meta);
                    } else {
                        playerShop.removeItem(item);
                    }
                }
                break;
            }
        }
    }

    private String getPriceFromLore(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("Price:")) {
                return ChatColor.stripColor(line.split(" ")[1]);
            }
        }
        return "0"; // Default if price not found
    }
}

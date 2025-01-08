package org.sharkconomy.utils;

import com.google.gson.*;
import org.bukkit.entity.Player;
import org.sharkconomy.sharkEconomy;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.Enchantment;

import java.io.*;
import java.util.*;

public class PlayerData {
    private static final File dataFile = new File(sharkEconomy.getInstance().getServer().getWorldContainer(), "sharkeconomy.json");
    private static JsonObject data = loadData();

    private static JsonObject loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                try (FileWriter writer = new FileWriter(dataFile)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileReader reader = new FileReader(dataFile)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            } else {
                return new JsonObject();
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    private static void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void reloadData() {
        data = loadData();
    }

    public static JsonObject getPlayerData(UUID playerUUID) {
        reloadData();
        if (!data.has(playerUUID.toString())) {
            JsonObject playerData = new JsonObject();
            playerData.addProperty("balance", 0);
            playerData.add("shop", new JsonObject());
            data.add(playerUUID.toString(), playerData);
            saveData();
            return playerData;
        }
        return data.getAsJsonObject(playerUUID.toString());
    }

    public static double getBalance(UUID playerUUID) {
        JsonObject playerData = getPlayerData(playerUUID);
        return playerData.get("balance").getAsDouble();
    }

    public static void setBalance(UUID playerUUID, double balance) {
        JsonObject playerData = getPlayerData(playerUUID);
        playerData.addProperty("balance", balance);
        saveData();
    }

    public static boolean isFirstTime(UUID playerUUID) {
        reloadData();
        return !data.has(playerUUID.toString());
    }

    public static Map<UUID, Double> getAllBalances() {
        Map<UUID, Double> balances = new HashMap<>();
        for (String key : data.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);
                JsonObject playerData = data.getAsJsonObject(key);
                double balance = playerData.get("balance").getAsDouble();
                balances.put(uuid, balance);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return balances;
    }


    public static boolean addItemToShop(UUID playerUUID, String itemName, int quantity, int price) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject shop = playerData.getAsJsonObject("shop");


        JsonObject itemData = new JsonObject();
        itemData.addProperty("quantity", quantity);
        itemData.addProperty("price", price);
        shop.add(itemName, itemData);
        saveData();
        return true;

    }


    public static JsonObject getShop(UUID playerUUID) {
        JsonObject playerData = getPlayerData(playerUUID);
        return playerData.getAsJsonObject("shop");
    }

    public static boolean hasItem(UUID playerUUID, String itemName) {
        JsonObject shop = getShop(playerUUID);
        return shop.has(itemName);
    }

    public static int totalItems(UUID playerUUID) {
        JsonObject shop = getShop(playerUUID);
        return shop.size();
    }

    public static void removeItem(UUID playerUUID, String itemName) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject shop = playerData.getAsJsonObject("shop");
        if (shop.has(itemName)) {
            shop.remove(itemName);
            saveData();
        }
    }

    public static boolean decreaseItemQuantity(UUID playerUUID, String itemName, int quantity) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject shop = playerData.getAsJsonObject("shop");

        if (shop.has(itemName)) {
            JsonObject itemData = shop.getAsJsonObject(itemName);
            int currentQuantity = itemData.get("quantity").getAsInt();

            if (currentQuantity >= quantity) {
                itemData.addProperty("quantity", currentQuantity - quantity);
                if (currentQuantity - quantity == 0) {
                    shop.remove(itemName);
                }
                saveData();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean addItemQuantity(UUID playerUUID, String itemName, int quantity) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject shop = playerData.getAsJsonObject("shop");

        if (shop.has(itemName)) {
            JsonObject itemData = shop.getAsJsonObject(itemName);
            int currentQuantity = itemData.get("quantity").getAsInt();

            itemData.addProperty("quantity", currentQuantity + quantity);  // Decrease the quantity
            saveData();
            return true;
        } else {
            return false;
        }
    }

    public static boolean editPrice(UUID playerUUID, String itemName, int price) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject shop = playerData.getAsJsonObject("shop");

        if (shop.has(itemName)) {
            JsonObject itemData = shop.getAsJsonObject(itemName);

            itemData.addProperty("price", price);
            saveData();
            return true;
        } else {
            return false;
        }
    }



    public static void giveStartingBalance(Player player) {
        if (isFirstTime(player.getUniqueId())) {
            int start = 100;
            setBalance(player.getUniqueId(), start);
            player.sendMessage("§l§2Welcome to SharkSMP S3!!§r §8§7Your current bank balance is §6§l" + start + "SC (sharcoins)§r§7\n" +
                    "Spend them wisely, You can do services for players, sell items, etc for more SC.\n" +
                    "Beware of scams and have fun! §b§o(plugin by sharkk2)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }
}

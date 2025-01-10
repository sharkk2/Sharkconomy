package org.sharkconomy.utils;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sharkconomy.sharkEconomy;
import org.bukkit.Sound;
import java.util.Random;
import java.io.*;
import java.util.*;

public class PlayerData {
    private static final File dataFile = new File(sharkEconomy.getInstance().getServer().getWorldContainer(), "sharkeconomy.json");
    private static JsonObject data = loadData();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

    private static void saveData(UUID playerUUID, JsonObject playerData) {
        data.add(playerUUID.toString(), playerData);

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(data, writer);
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
            JsonObject dailyData = new JsonObject();
            dailyData.addProperty("last_daily", System.currentTimeMillis());
            dailyData.addProperty("extra", 0);
            playerData.add("daily", dailyData);
            playerData.add("transactions", new JsonArray());

            saveData(playerUUID, playerData);
            return playerData;
        }
        return data.getAsJsonObject(playerUUID.toString());
    }

    private static final long TIME_THRESHOLD = 60 * 1000;
    public static void registerTransaction(UUID fromPlayer, UUID toPlayer, double amount, String reason) {
        JsonObject toPlayerData = getPlayerData(toPlayer);
        JsonObject fromPlayerData = getPlayerData(fromPlayer);
        JsonArray toTransactions = toPlayerData.getAsJsonArray("transactions");
        JsonArray fromTransactions = fromPlayerData.getAsJsonArray("transactions");

        long currentTime = System.currentTimeMillis();
        boolean merged = false;

        for (JsonElement element : toTransactions) {
            JsonObject transaction = element.getAsJsonObject();
            UUID existingFromPlayer = UUID.fromString(transaction.get("fromPlayer").getAsString());
            String existingReason = transaction.get("reason").getAsString();
            long existingDate = transaction.get("date").getAsLong();

            if (existingFromPlayer.equals(fromPlayer) &&
                    existingReason.equals(reason) &&
                    (currentTime - existingDate) <= TIME_THRESHOLD) {

                double existingAmount = transaction.get("amount").getAsDouble();
                transaction.addProperty("amount", existingAmount + amount);
                transaction.addProperty("date", currentTime);
                merged = true;
                break;
            }
        }

        for (JsonElement element : fromTransactions) {
            JsonObject transaction = element.getAsJsonObject();
            UUID existingToPlayer = UUID.fromString(transaction.get("toPlayer").getAsString());
            String existingReason = transaction.get("reason").getAsString();
            long existingDate = transaction.get("date").getAsLong();

            if (existingToPlayer.equals(toPlayer) &&
                    existingReason.equals(reason) &&
                    (currentTime - existingDate) <= TIME_THRESHOLD) {

                double existingAmount = transaction.get("amount").getAsDouble();
                transaction.addProperty("amount", existingAmount + amount);
                transaction.addProperty("date", currentTime);
                merged = true;
                break;
            }
        }

        if (!merged) {
            JsonObject toTransaction = new JsonObject();
            Random random = new Random();
            int id = random.nextInt(1000) + 1;
            toTransaction.addProperty("fromPlayer", fromPlayer.toString());
            toTransaction.addProperty("toPlayer", fromPlayer.toString());
            toTransaction.addProperty("amount", amount);
            toTransaction.addProperty("reason", reason);
            toTransaction.addProperty("date", currentTime);
            toTransaction.addProperty("type", 1);
            toTransaction.addProperty("id", id);
            toTransactions.add(toTransaction);

            JsonObject fromTransaction = new JsonObject();
            fromTransaction.addProperty("toPlayer", toPlayer.toString());
            fromTransaction.addProperty("fromPlayer", toPlayer.toString());
            fromTransaction.addProperty("amount", amount);
            fromTransaction.addProperty("reason", reason);
            fromTransaction.addProperty("date", currentTime);
            fromTransaction.addProperty("type", 0);
            fromTransaction.addProperty("id", id);
            fromTransactions.add(fromTransaction);
        }

        saveData(toPlayer, toPlayerData);
        saveData(fromPlayer, fromPlayerData);
    }



    public static List<JsonObject> getTransactions(UUID playerUUID) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonArray transactions = playerData.getAsJsonArray("transactions");
        List<JsonObject> allTransactions = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            allTransactions.add(transactions.get(i).getAsJsonObject());
        }

        return allTransactions;
    }


    public static double getBalance(UUID playerUUID) {
        JsonObject playerData = getPlayerData(playerUUID);
        return playerData.get("balance").getAsDouble();
    }

    public static void setBalance(UUID playerUUID, double balance) {
        JsonObject playerData = getPlayerData(playerUUID);
        playerData.addProperty("balance", balance);
        saveData(playerUUID, playerData);
        Bukkit.getLogger().info("Updated balance for " + Bukkit.getOfflinePlayer(playerUUID).getName() + " To " + balance + "sc");
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
        saveData(playerUUID, playerData);
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
            saveData(playerUUID, playerData);
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
                saveData(playerUUID, playerData);
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

            itemData.addProperty("quantity", currentQuantity + quantity);
            saveData(playerUUID, playerData);
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
            saveData(playerUUID, playerData);
            return true;
        } else {
            return false;
        }
    }



    public static boolean giveStartingBalance(Player player) {
        if (isFirstTime(player.getUniqueId())) {
            int start = 100;
            setBalance(player.getUniqueId(), start);
            player.sendMessage("§l§2Welcome to SharkSMP S3!!§r §8§7Your current bank balance is §6§l" + start + "SC (sharcoins)§r§7\n" +
                    "Spend them wisely, You can do services for players, sell items, etc for more SC.\n" +
                    "Beware of scams and have fun! §b§o(plugin by sharkk2)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        }
        return false;
    }


    public static boolean giveDaily(UUID playerUUID) {
        JsonObject playerData = getPlayerData(playerUUID);
        JsonObject dailyData = playerData.getAsJsonObject("daily");

        long currentTime = System.currentTimeMillis();
        long lastDaily = dailyData.get("last_daily").getAsLong();
        int extra = dailyData.get("extra").getAsInt();

        if (currentTime - lastDaily >= 24 * 60 * 60 * 1000) {
            double reward = 25 + extra;
            playerData.addProperty("balance", getBalance(playerUUID) + reward); // its cuz when the setBalance is used and then we save the data, data saved by setBalance gets overwritten
            dailyData.addProperty("last_daily", currentTime);

            if (currentTime - lastDaily >= 48 * 60 * 60 * 1000) {
                dailyData.addProperty("extra", 0);
            } else {
                dailyData.addProperty("extra", extra + 5);
            }
            playerData.add("daily", dailyData);
            saveData(playerUUID, playerData);

            Player player = Bukkit.getPlayer(playerUUID);
            player.sendMessage(
                    "You've earned §6" + reward +"sc§r for joining daily!\n" +
                    "Keep the streak to earn more every day.\n" +
                    "Your current streak: §b" + ((extra + 5) / 5) + " day(s)§r."
            );
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

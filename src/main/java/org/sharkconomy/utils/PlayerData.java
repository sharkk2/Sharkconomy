package org.sharkconomy.utils;

import com.google.gson.*;
import org.bukkit.entity.Player;
import org.sharkconomy.sharkEconomy;
import org.bukkit.Sound;
import java.util.*;
import java.io.*;
import java.util.UUID;

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

    public static double getBalance(UUID playerUUID) {
        reloadData();
        if (data.has(playerUUID.toString())) {
            return data.get(playerUUID.toString()).getAsDouble();
        }
        return 0;
    }

    public static void setBalance(UUID playerUUID, double balance) {
        data.addProperty(playerUUID.toString(), balance);
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
                double balance = data.get(key).getAsDouble();
                balances.put(uuid, balance);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return balances;
    }

    public static void giveStartingBalance(Player player) {
        if (isFirstTime(player.getUniqueId())) {
            int start = 100;
            if (player.getName() == "mrclixy") {
                start = 1;
            }
            setBalance(player.getUniqueId(), 100); // Give 100 Shark Bucks (SC)
            player.sendMessage("§l§2Welcome to SharkSMP S3!!§r §8§7Your current bank balance is §6§l100SC (shark coins)§r§7\n" +
                    "Spend them wisely, You can do services for players, sell items, etc for more SC.\n" +
                    "Beware of scams and have fun! §b§o(plugin by sharkk2)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        }
    }
}
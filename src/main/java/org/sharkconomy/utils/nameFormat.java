package org.sharkconomy.utils;
import org.bukkit.Material;

public class nameFormat {
    public static String formatItemName(Material itemType) {
        String[] parts = itemType.name().toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String part : parts) {
            formattedName.append(part.substring(0, 1).toUpperCase()).append(part.substring(1)).append(" ");
        }
        return formattedName.toString().trim();
    }
}

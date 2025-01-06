package org.sharkconomy;

import org.bukkit.plugin.java.JavaPlugin;
import org.sharkconomy.commands.economy.EconomyCommand;
import org.sharkconomy.listeners.playerJoin;
import org.sharkconomy.commands.EconomyTabCompleter;

public final class sharkEconomy extends JavaPlugin {
    private static sharkEconomy instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("HELLO NIGGERS THIS IS SHARKK2");
        getCommand("economy").setExecutor(new EconomyCommand());
        getCommand("economy").setTabCompleter(new EconomyTabCompleter());
        getServer().getPluginManager().registerEvents(new playerJoin(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static sharkEconomy getInstance() {
        return instance;
    }

}

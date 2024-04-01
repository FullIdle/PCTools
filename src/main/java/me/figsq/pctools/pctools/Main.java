package me.figsq.pctools.pctools;

import me.figsq.pctools.pctools.api.util.Cache;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Cache.plugin = this;
        reloadConfig();

        PluginCommand command = getCommand(this.getDescription().getName().toLowerCase());
        CMD cmd = new CMD();
        command.setExecutor(cmd);
        command.setTabCompleter(cmd);

        getServer().getPluginManager().registerEvents(new PlayerListener(),this);

        new Papi().register();
        getLogger().info("§aPlugin Loaded!");
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        Cache.init();
    }

    @Override
    public void onDisable() {

    }
}
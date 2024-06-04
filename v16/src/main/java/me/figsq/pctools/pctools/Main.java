package me.figsq.pctools.pctools;

import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.PokeUtil;
import org.bukkit.command.PluginCommand;
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
        PokeUtil.init();
    }

    @Override
    public void onDisable() {

    }
}
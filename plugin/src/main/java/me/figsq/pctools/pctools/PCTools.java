package me.figsq.pctools.pctools;

import me.figsq.pctools.pctools.api.Config;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import me.figsq.pctools.pctools.api.PokeUtil;

public class PCTools extends JavaPlugin {
    public static PCTools INSTANCE;

    public PCTools() {
        INSTANCE = this;
        Config.plugin = this;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        reloadConfig();

        PluginCommand command = getCommand(this.getDescription().getName().toLowerCase());
        CMD cmd = new CMD();
        command.setExecutor(cmd);
        command.setTabCompleter(cmd);

        new Papi().register();

        getServer().getPluginManager().registerEvents(new PlayerListener(),this);

        getLogger().info("§aPlugin Loaded!");
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        Config.init();
        PokeUtil.init();
    }

    @Override
    public void onDisable() {

    }
}
package me.figsq.pctools_searchproperty.poketeams;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import me.gsqfi.poketeams.poketeams.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin implements ISearchProperty {
    @Override
    public void onEnable() {
        getLogger().info("§aSearchProperty-PokeStar ADDED!");
        SomeMethod.addSearchProperty("poketeams",this);
    }

    @Override
    public boolean hasProperty(Pokemon poke, String arg) {
        List<String> list = PlayerData.getConfig().getStringList(poke.getOwnerName() + "." + arg);
        if (list.isEmpty()) return false;
        return list.contains(poke.getUUID().toString());
    }

    @Override
    public List<String> onTabComplete(Player player, String value) {
        if (PlayerData.getConfig().contains(player.getName())) {
            return Lists.newArrayList(PlayerData.getConfig().
                    getConfigurationSection(player.getName()).getKeys(false));
        }
        return null;
    }
}
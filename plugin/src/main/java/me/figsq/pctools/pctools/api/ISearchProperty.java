package me.figsq.pctools.pctools.api;

import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.bukkit.entity.Player;

import java.util.List;

public interface ISearchProperty {
    String getName();
    boolean hasProperty(IPokemonWrapper<?> poke, String arg);
    List<String> onTabComplete(Player player,String value);

    static void addSearchProperty(String searchProperty_name, ISearchProperty searchProperty){
        Config.searchProperties.put(searchProperty_name,searchProperty);
    }
}

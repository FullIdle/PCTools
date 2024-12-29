package me.figsq.pctools.pctools.api;

import org.bukkit.entity.Player;

import java.util.List;

public interface ISearchProperty {
    String getName();
    boolean hasProperty(Object poke, String arg);
    List<String> onTabComplete(Player player,String value);

    static void addSearchProperty(String searchProperty_name, ISearchProperty searchProperty){
        Cache.searchProperties.put(searchProperty_name,searchProperty);
    }
}

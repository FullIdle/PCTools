package me.figsq.pctools.pctools.api;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.bukkit.entity.Player;

import java.util.List;

public interface ISearchProperty {
    String getName();
    boolean hasProperty(Pokemon poke, String arg);
    List<String> onTabComplete(Player player,String value);
}

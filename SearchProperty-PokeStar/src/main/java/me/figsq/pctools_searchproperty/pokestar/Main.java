package me.figsq.pctools_searchproperty.pokestar;

import com.mc9y.pokestar.PokeStarAPI;
import com.mc9y.pokestar.data.StarData;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main extends JavaPlugin implements ISearchProperty {
    private static PokeStarAPI pokeStarApi;
    private static int maxStarLevel = 1;

    @Override
    public void onEnable() {
        pokeStarApi = com.mc9y.pokestar.Main.getPokeStarAPI();

        for (StarData datum : StarData.STAR_DATA) {
            maxStarLevel = Math.max(maxStarLevel, datum.getStarLevel());
        }

        SomeMethod.addSearchProperty("pokestar",this);

        getLogger().info("§aSearchProperty-PokeStar ADDED!");
    }

    @Override
    public boolean hasProperty(Pokemon poke, String arg) {
        int anInt = isInt(arg);
        return 0 < anInt && anInt > maxStarLevel && pokeStarApi.getPokemonStar(poke.getSpecies().name) == anInt;
    }

    @Override
    public List<String> onTabComplete(Player player, String value) {
        Stream<String> stream = IntStream.rangeClosed(1, maxStarLevel).boxed().map(String::valueOf);
        if (value.isEmpty()) return stream.collect(Collectors.toList());
        return stream.filter(s->s.startsWith(value)).collect(Collectors.toList());
    }

    public int isInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
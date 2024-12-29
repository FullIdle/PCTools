package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.storage.PCBox;

import java.util.*;

public class PCSortUtil {
    /**
     * 种族排序 宝可梦编号排序
     */
    public static void speciesSort(PCBox box) {
        Map<Species,List<Pokemon>> map = new HashMap<>();
        for (Pokemon pokemon : box.getAll()) {
            if (pokemon == null)continue;
            Species species = pokemon.getSpecies().getDefaultForm().getParentSpecies();
            map.computeIfAbsent(species,k->new ArrayList<>()).add(pokemon);
            box.set(pokemon.getPosition(),null);
        }
        ArrayList<Species> species = Lists.newArrayList(map.keySet());
        species.sort(Species::compareTo);
        for (Species sp : species) {
            for (Pokemon pokemon : map.get(sp)) {
                box.add(pokemon);
            }
        }
    }

    /**
     * 特殊种类排序
     */
    public static void specialSort(PCBox pcBox){
        ArrayList<Pokemon> pokemons = Lists.newArrayList(pcBox.getAll());
        pokemons.removeIf(Objects::isNull);

        for (Pokemon pokemon : pokemons) {
            if (pokemon == null) continue;
            pcBox.set(pokemon.getPosition(), null);
        }

        pokemons.sort(Comparator.comparingInt(PCSortUtil::getPokemonSortNumber));
        for (Pokemon pokemon : pokemons) {
            pcBox.add(pokemon);
        }
    }

    /**
    * 随机排序
    */
    public static void randomSort(PCBox box){
        ArrayList<Pokemon> pokemons = Lists.newArrayList(box.getAll());
        Collections.shuffle(pokemons);
        for (int i = 0; i < pokemons.size(); i++) {
            box.set(i,pokemons.get(i));
        }
    }

    private static int getPokemonSortNumber(Pokemon pokemon){
        return pokemon.isEgg() ?
                0 : pokemon.isLegendary() ?
                2 : pokemon.getSpecies().isUltraBeast() ?
                3 : 1;
    }
}

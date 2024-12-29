package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;

import java.util.*;

public class PCSortUtil {
    /**
     * 种族排序 宝可梦编号排序
     */
    public static void speciesSort(PCBox box) {
        Map<EnumSpecies, List<Pokemon>> map = new EnumMap<>(EnumSpecies.class);
        for (Pokemon pokemon : box.getAll()) {
            if (pokemon == null) continue;
            EnumSpecies species = pokemon.getSpecies().getBaseSpecies();
            map.computeIfAbsent(species, k -> new ArrayList<>()).add(pokemon);
            box.set(pokemon.getPosition(), null);
        }
        for (Map.Entry<EnumSpecies, List<Pokemon>> entry : map.entrySet()) {
            for (Pokemon pokemon : entry.getValue()) {
                box.add(pokemon);
            }
        }
    }

    /**
     * 特殊种类排序
     */
    public static void specialSort(PCBox pcBox) {
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
    public static void randomSort(PCBox box) {
        ArrayList<Pokemon> pokemons = Lists.newArrayList(box.getAll());
        Collections.shuffle(pokemons);
        for (int i = 0; i < pokemons.size(); i++) {
            box.set(i, pokemons.get(i));
        }
    }

    private static int getPokemonSortNumber(Pokemon pokemon){
        return pokemon.isEgg() ?
                0 : pokemon.isLegendary() ?
                2 : pokemon.getSpecies().isUltraBeast() ?
                3 : 1;
    }
}

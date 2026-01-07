package me.figsq.pctools.pctools.api;

import lombok.val;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;

import java.util.*;

public class PCSortUtil {
    /**
     * 种族排序 宝可梦编号排序
     */
    public static void speciesSort(PCBox pcBox) {
        val map = new TreeMap<Integer, List<IPokemonWrapper<?>>>();
        for (IPokemonWrapper<?> poke : pcBox.all()) {
            val species = poke.getSpecies();
            val dex = species.getDex();
            map.computeIfAbsent(dex, k -> new ArrayList<>()).add(poke);
            pcBox.set(poke.getStoragePos(), null);
        }
        for (Map.Entry<Integer, List<IPokemonWrapper<?>>> entry : map.entrySet())
            pcBox.add(entry.getValue(), 0);
    }

    /**
     * 特殊种类排序
     */
    public static void specialSort(PCBox pcBox) {
        List<IPokemonWrapper<?>> pokemons = pcBox.all();

        for (val pokemon : pokemons) pcBox.set(pokemon.getStoragePos(), null);

        pokemons.sort(Comparator.comparingInt(PCSortUtil::getPokemonSortNumber));
        pcBox.add(pokemons, 0);
    }

    /**
     * 随机排序
     */
    public static void randomSort(PCBox pcBox) {
        val all = pcBox.all();
        Collections.shuffle(all);
        val page = pcBox.getPage();
        val size = all.size();
        for (int i = 0; i < 30; i++) pcBox.set(new StoragePos(page, i), i < size ? all.get(i) : null);
    }

    private static int getPokemonSortNumber(IPokemonWrapper<?> pokemon) {
        return pokemon.isEgg() ?
                0 : pokemon.isLegend() ?
                2 : pokemon.isUltra() ?
                3 : 1;
    }
}

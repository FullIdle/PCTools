package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import me.figsq.pctools.pctools.api.enums.SpecialType;

import java.util.*;
import java.util.stream.Collectors;

public class TidyPcUtil{
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
        Map<SpecialType, List<Pokemon>> map = new EnumMap<>(SpecialType.class);

        for (Pokemon pokemon : pcBox.getAll()) {
            if (pokemon == null)continue;
            SpecialType type = SpecialType.getType(pokemon);
            map.computeIfAbsent(type,k->new ArrayList<>()).add(pokemon);
            pcBox.set(pokemon.getPosition(),null);
        }

        for (List<Pokemon> value : map.values()) {
            for (Pokemon pokemon : value) {
                pcBox.add(pokemon);
            }
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
}

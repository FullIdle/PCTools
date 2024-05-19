package me.figsq.pctools.pctools.api.util;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;

import java.util.UUID;

public class StorageHelper {
    /**
     * 查找宝可梦
     */
    public static Pokemon find(UUID uuid, PokemonStorage... pokemonStorages){
        Pokemon pokemon;
        for (PokemonStorage storage : pokemonStorages) {
            if ((pokemon = storage.find(uuid)) != null) {
                return pokemon;
            }
        }
        return null;
    }
}

package me.figsq.pctools.pctools.api;

import lombok.val;
import me.fullidle.ficore.ficore.FICoreAPI;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokeStorageWrapper;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StorageHelper {
    /**
     * 查找宝可梦
     */
    public static IPokemonWrapper<?> find(UUID uuid, IPokeStorageWrapper<?> @NotNull ... pokemonStorages) {
        IPokemonWrapper<?> pokemon;
        for (IPokeStorageWrapper<?> storage : pokemonStorages)
            if ((pokemon = find(uuid, storage)) != null) return pokemon;
        return null;
    }

    public static IPokemonWrapper<?> find(UUID uuid, @NotNull IPokeStorageWrapper<?> storage) {
        for (IPokemonWrapper<?> wrapper : storage.all()) {
            if (wrapper == null) continue;
            if (uuid != null && uuid.equals(wrapper.getUUID())) return wrapper;
        }
        return null;
    }

    public static boolean isParty(@NotNull IPokeStorageWrapper<?> storage) {
        val storageManager = FICoreAPI.getPokeStorageManager();
        return storage.equals(storageManager.getParty(storage.getUUID()));
    }

    public static StoragePos findEmpty(IPokeStorageWrapper<?> storage, int startPage, int endPage, int startSlot, int endSlot) {
        while (startPage < endPage) {
            while (startSlot < endSlot) {
                val pos = new StoragePos(startPage, startSlot);
                if (storage.get(pos) == null) return pos;
                startSlot++;
            }
            startPage++;
        }
        return null;
    }

    public static StoragePos findEmpty(IPokeStorageWrapper<?> storage) {
        if (isParty(storage)) return findEmpty(storage, 0, 1, 0, 6);
        if (storage instanceof PCBox) {
            val page = ((PCBox) storage).getPage();
            return findEmpty(storage, page, page + 1, 0, 30);
        }
        return findEmpty(storage, 0, 30, 0, 30);
    }

    public static StoragePos findPos(IPokeStorageWrapper<?> storage, IPokemonWrapper<?> pokemon) {
        for (IPokemonWrapper<?> wrapper : storage.all()) if (pokemon.equals(wrapper)) return pokemon.getStoragePos();
        return null;
    }

    public static IPokemonWrapper<?> getPokemon(UUID uuid, StoragePos pos) {
        val storageManager = FICoreAPI.getPokeStorageManager();
        final IPokeStorageWrapper<?> storage = pos.getBox() == -1 ? storageManager.getParty(uuid) : storageManager.getPC(uuid);
        return storage.get(pos);
    }
}

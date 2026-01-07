package me.figsq.pctools.pctools.api;

import lombok.Getter;
import lombok.val;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokeStorageWrapper;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ficore并没有pcbox，所以自己整一个
 */
@Getter
public class PCBox extends IPokeStorageWrapper<Object> {
    private final IPokeStorageWrapper<?> pc;
    private final int page;

    /**
     * @param pc 传入最好是pc的存储，这并不会进行检查
     * @param page 页数
     */
    public PCBox(IPokeStorageWrapper<?> pc, int page) {
        super(pc.getOriginal());
        this.pc = pc;
        this.page = page;
    }

    @Override
    public IPokemonWrapper<?> get(StoragePos storagePos) {
        return pc.get(storagePos);
    }

    public IPokemonWrapper<?> get(int slot) {
        return get(new StoragePos(page, slot));
    }

    @Override
    public List<IPokemonWrapper<?>> all() {
        return pc.all().stream().filter(Objects::nonNull).filter(pokemon -> pokemon.getStoragePos().getBox() == page).collect(Collectors.toList());
    }

    @Override
    public UUID getUUID() {
        return pc.getUUID();
    }

    @Override
    public void add(IPokemonWrapper iPokemonWrapper) {
        add(iPokemonWrapper, 0);
    }

    /**
     * @param iPokemonWrapper 宝可梦
     * @param start 开始检测的位置 -1 则传给pc直接add
     * @return 最终添加到的位置(翻页后就是没有)
     */
    public int add(IPokemonWrapper iPokemonWrapper, int start) {
        if (start > 30) throw new IllegalArgumentException("start must be less than 30");
        if (start == -1) {
            pc.add(iPokemonWrapper);
            return -1;
        }
        for (int i = start; i < 30; i++) {
            val pos = new StoragePos(page, i);
            if (pc.get(pos) == null) {
                pc.set(pos, iPokemonWrapper);
                return i;
            }
        }
        pc.add(iPokemonWrapper);
        return -1;
    }

    public void add(Collection<IPokemonWrapper<?>> pokemons, int start) {
        for (val pokemon : pokemons) add(pokemon, start);
    }

    @Override
    public void set(StoragePos storagePos, @Nullable IPokemonWrapper iPokemonWrapper) {
        pc.set(storagePos, iPokemonWrapper);
    }

    @Override
    public Class getType() {
        throw new UnsupportedOperationException("PCBox is not a real storage");
    }
}

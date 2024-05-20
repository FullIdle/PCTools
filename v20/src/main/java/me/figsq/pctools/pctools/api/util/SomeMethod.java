package me.figsq.pctools.pctools.api.util;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.enums.SpecialType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class SomeMethod {
    /**
     * 字符串变量处理及颜色代码处理
     *
     * @param player 可以为null,null则不会进行papi的操作而是颜色代码替换
     */
    public static String papi(OfflinePlayer player, String str) {
        str = str.replace("&", "§");
        if (player == null) return str;
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public static List<String> papi(OfflinePlayer player, Collection<String> ss, Function<String, String> fun) {
        ArrayList<String> list = new ArrayList<>(ss);
        list.replaceAll(s -> papi(player, fun != null?fun.apply(s):s));
        return list;
    }

    public static String[] papi(OfflinePlayer player, String[] strings, Function<String, String> fun) {
        String[] temp = new String[strings.length];
        boolean b = fun != null;
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (b) string = fun.apply(string);
            temp[i] = papi(player, string);
        }
        return temp;
    }

    public static ItemStack getFormatPokePhoto(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack photo = SpriteItemHelper.getPhoto(pokemon);
        NBTTagCompound tag = photo.v() == null ? new NBTTagCompound() : photo.v();
        tag.a("pctoolsUUID", pokemon.getUUID().toString());
        photo.c(tag);
        Player player = pokemon.getOwnerPlayer().getBukkitEntity().getPlayer();
        ItemStack copy = CraftItemStack.asBukkitCopy(photo);
        ItemMeta itemMeta = copy.getItemMeta();
        String name;
        List<String> lore;
        Pair<String, List<String>> pair = Cache.specialNAL.get(pokemon.getSpecies());
        if (pair == null){
            SpecialType type = SpecialType.getType(pokemon);
            switch (type){
                case EGG:{
                    name = Cache.eggName;
                    lore = Cache.eggLore;
                    break;
                }
                case LEGEND:{
                    name = Cache.legendName;
                    lore = Cache.legendLore;
                    break;
                }
                case UBEAST:{
                    name = Cache.uBeastName;
                    lore = Cache.uBeastLore;
                    break;
                }
                default:{
                    name = Cache.normalName;
                    lore = Cache.normalLore;
                    break;
                }
            }
        }else{
            name = pair.getLeft();
            lore = pair.getRight();
        }

        StoragePosition ps = pokemon.getPosition();
        Function<String, String> fun = s -> s.replace("{box}", String.valueOf(((int) (ps.box - Cache.papiIndexOffset))))
                .replace("{order}", String.valueOf(((int) (ps.order - Cache.papiIndexOffset))));
        itemMeta.setDisplayName(papi(player, fun.apply(name)));
        itemMeta.setLore(papi(player, lore, fun));
        copy.setItemMeta(itemMeta);
        return copy;
    }

    public static UUID getFormatItemUUID(ItemStack itemStack) {
        if (itemStack == null) return null;

        net.minecraft.world.item.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbt = copy.v() == null ? new NBTTagCompound() : copy.v();
        if (nbt.b("pctoolsUUID")) {
            return UUID.fromString(nbt.l("pctoolsUUID"));
        }
        return null;
    }

    public static Tuple<PokemonStorage, StoragePosition> computeStorageAndPosition(int clickSlot, PlayerPartyStorage partyStorage, PCBox pcBox) {
        if (Cache.invBackpackSlot.contains(clickSlot))
            return new Tuple<>(partyStorage, new StoragePosition(-1, Cache.invBackpackSlot.indexOf(clickSlot)));
        if (Cache.invPcSlot.contains(clickSlot))
            return new Tuple<>(pcBox, new StoragePosition(pcBox.boxNumber, Cache.invPcSlot.indexOf(clickSlot)));
        return null;
    }

    /**
     * 注册/添加搜索条目
     */
    public static void addSearchProperty(String searchProperty_name, ISearchProperty searchProperty){
        Cache.searchProperties.put(searchProperty_name,searchProperty);
    }
}

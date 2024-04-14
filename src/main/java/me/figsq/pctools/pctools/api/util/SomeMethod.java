package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.clip.placeholderapi.PlaceholderAPI;
import me.figsq.pctools.pctools.api.enums.SpecialType;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.Tuple;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
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
        net.minecraft.server.v1_12_R1.ItemStack photo = (net.minecraft.server.v1_12_R1.ItemStack) ((Object) ItemPixelmonSprite.getPhoto(pokemon));
        NBTTagCompound tag = photo.getTag() == null ? new NBTTagCompound() : photo.getTag();
        tag.setString("pctoolsUUID", pokemon.getUUID().toString());
        photo.setTag(tag);
        Player player = ((EntityPlayer) ((Object) pokemon.getOwnerPlayer()))
                .getBukkitEntity().getPlayer();
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
        net.minecraft.server.v1_12_R1.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbt = copy.getTag() == null ? new NBTTagCompound() : copy.getTag();
        if (nbt.hasKey("pctoolsUUID")) {
            return UUID.fromString(nbt.getString("pctoolsUUID"));
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
     * 获取存储内有多少只精灵
     */
    public static Integer getStoragePokeSlot(PokemonStorage storage){
        ArrayList<Pokemon> list = Lists.newArrayList(storage.getAll());
        list.removeIf(Objects::isNull);
        int i = list.size();
        return i;
    }
}

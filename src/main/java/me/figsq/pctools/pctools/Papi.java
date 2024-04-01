package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.*;
import com.pixelmonmod.pixelmon.items.ItemHeld;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.figsq.pctools.pctools.api.util.Cache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class Papi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return Cache.plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(Cache.plugin.getDescription().getAuthors().toArray());
    }

    @Override
    public @NotNull String getVersion() {
        return Cache.plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] split = params.split("_");
        int box = (int) Double.parseDouble(split[0]);
        int order = (int) Double.parseDouble(split[1]);
        String other = split[2].toLowerCase();
        Pokemon poke = Pixelmon.storageManager.getPokemon(
                (EntityPlayerMP) ((Object) ((CraftEntity) player).getHandle()),
                new StoragePosition(box, order));
        if (poke == null) {
            return "空槽";
        }

        switch (other) {
            case "types": {
                return poke.getSpecies().getBaseStats().types.toString()
                        .replace("Normal", "普")
                        .replace("Fire", "火")
                        .replace("Fighting", "格")
                        .replace("Water", "水")
                        .replace("Flying", "飞")
                        .replace("Grass", "草")
                        .replace("Poison", "毒")
                        .replace("Electric", "电")
                        .replace("Ground", "地")
                        .replace("Psychic", "超")
                        .replace("Rock", "岩")
                        .replace("Ice", "冰")
                        .replace("Bug", "虫")
                        .replace("Dragon", "龙")
                        .replace("Ghost", "幽")
                        .replace("Dark", "暗")
                        .replace("Steel", "钢")
                        .replace("Fairy", "妖")
                        .replace("Stellar", "星")
                        .replace("[", " ")
                        .replace("]", " ")
                        .replace(",", "")
                        .replace(" ", "");
            }
            case "egggroup": {
                return poke.getSpecies().getBaseStats().eggGroups.toString()
                        .replace("Monster", "怪兽")
                        .replace("Humanlike", "人型")
                        .replace("Water1", "水中1")
                        .replace("Water3", "水中3")
                        .replace("Bug", "虫")
                        .replace("Mineral", "矿物")
                        .replace("Flying", "飞行")
                        .replace("Amorphous", "不定形")
                        .replace("Field", "陆上")
                        .replace("Water2", "水中2")
                        .replace("Fairy", "妖精")
                        .replace("Ditto", "百变怪")
                        .replace("Grass", "植物")
                        .replace("Dragon", "龙")
                        .replace("Undiscovered", "未发现")
                        .replace("[", " ")
                        .replace("]", " ")
                        .replace(", ", "、");
            }
            case "position": {
                String o = split[3];
                return o.equalsIgnoreCase("box") ?
                        String.valueOf(poke.getPosition().box) : o.equalsIgnoreCase("order") ?
                        String.valueOf(poke.getPosition().order) : "未知属性";
            }
            case "statstotal": {
                Stats stats = poke.getStats();
                return String.valueOf(addUp(Lists.newArrayList(
                        stats.hp,
                        stats.attack,
                        stats.defence,
                        stats.specialAttack,
                        stats.specialDefence,
                        stats.speed
                )));
            }
            case "stats":
                return stats(split[3], poke.getStats());
            case "basestats":
                return stats(split[3], poke.getBaseStats());
            case "basetotal": {
                return String.valueOf(addUp(poke.getBaseStats().stats.values()));
            }
            case "ivstotal":
                return String.valueOf(poke.getStats().ivs.getTotal());
            case "evstotal":
                return String.valueOf(poke.getStats().evs.getTotal());
            case "eggsteps":
                return String.valueOf(poke.getEggSteps());
            case "eggcycles":
                return String.valueOf(poke.getEggCycles());
            case "growth":
                return poke.getGrowth().getLocalizedName();
            case "helditem":
                String localizedName = poke.getHeldItemAsItemHeld().getLocalizedName();
                return localizedName.equals("item..name") ? "无" : localizedName;
            case "status":
                return poke.getStatus().type.getLocalizedName();
            case "from":
                return poke.getFormEnum().getLocalizedName();
            case "nature":
                return poke.getNature().getLocalizedName();
            case "gender":
                return poke.getGender().getLocalizedName();
            case "hastag":
                return String.valueOf(poke.hasSpecFlag(split[3].toLowerCase()));
            case "uuid":
                return poke.getUUID().toString();
            case "moveset": {
                int i = Integer.parseInt(split[3]);
                Attack attack = poke.getMoveset().attacks[i];
                return attack == null ? "无" : attack.getMove().getLocalizedName();
            }
            case "originalname":
                return poke.getSpecies().name;
            case "localizedname":
                return poke.getLocalizedName();
            case "nickname":
                String nickname = poke.getNickname();
                return nickname == null?poke.getSpecies().name:nickname;
            case "ability":
                return poke.getAbility().getLocalizedName();
            case "islegendary":
                return String.valueOf(poke.isLegendary());
            case "isegg":
                return String.valueOf(poke.isEgg());
            case "isultrabeast":
                return String.valueOf(poke.getSpecies().isUltraBeast());
            case "ivs":
                return stats(split[3], poke.getStats().ivs);
            case "evs":
                return stats(split[3], poke.getStats().evs);
            case "level":
                return String.valueOf(poke.getLevel());
            case "shiny":
                return String.valueOf(poke.isShiny());
            case "inranch":
                return String.valueOf(poke.isInRanch());
            case "caughtball":
                return poke.getCaughtBall().getLocalizedName();
            default:
                return "未知属性";
        }
    }

    public static String stats(String arg, Object stats) {
        String lowerCase = arg.toLowerCase();
        StatsType type = getStatsType(lowerCase);
        if (type != null) {
            return String.valueOf(stats instanceof IStatStore ?
                    ((IStatStore) stats).getStat(type) : stats instanceof Stats ?
                    ((Stats) stats).get(type) :
                    ((BaseStats) stats).getStat(type));
        }
        return null;
    }

    private static StatsType getStatsType(String lowerCase) {
        switch (lowerCase) {
            case "hp":
                return StatsType.HP;
            case "sp":
                return StatsType.Speed;
            case "at":
                return StatsType.Attack;
            case "df":
                return StatsType.Defence;
            case "sd":
                return StatsType.SpecialDefence;
            case "sa":
                return StatsType.SpecialAttack;
            default:
                return null;
        }
    }

    private static Integer addUp(Collection<Integer> integers) {
        int x = 0;
        for (Integer i : integers) {
            x += i;
        }
        return x;
    }
}

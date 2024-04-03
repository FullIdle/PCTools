package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IStatStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import lombok.SneakyThrows;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.figsq.pctools.pctools.api.util.Cache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import static me.figsq.pctools.pctools.api.util.Cache.*;

public class Papi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(plugin.getDescription().getAuthors().toArray());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @SneakyThrows
    @Override /*%pctools_{pokemon}_types%*/
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Pokemon poke = null;
        String[] split = null;
        if (params.charAt(0) == '{'){
            int end = params.lastIndexOf('}') + 1;
            if (end != 0){
                String json = params.substring(0, end);
                NBTTagCompound nbt = JsonToNBT.func_180713_a(json);
                poke = Pixelmon.pokemonFactory.create(nbt);
                String[] ss = params.substring(end+1).split("_");
                split = new String[2 + ss.length];
                System.arraycopy(ss,0,split,2,ss.length);
            }
        }else{
            split = params.split("_");
            int box = (int) (Double.parseDouble(split[0])+papiIndexOffset);
            int order = (int) (Double.parseDouble(split[1])+papiIndexOffset);
            poke = Pixelmon.storageManager.getPokemon(
                    (EntityPlayerMP) ((Object) ((CraftEntity) player).getHandle()),
                    new StoragePosition(box, order));
        }
        if (poke == null) {
            return "空槽";
        }
        if (split == null) {
            return "格式错误";
        }
        String arg = split[2].toLowerCase();
        switch (arg) {
            case "catchrate":{
                return String.valueOf(poke.getBaseStats().getCatchRate());
            }
            case "weight":{
                return String.valueOf(poke.getBaseStats().getWeight());
            }
            case "malepercent":{
                return String.valueOf(((int) poke.getBaseStats().getMalePercent()));
            }
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
                return Arrays.toString(poke.getSpecies().getBaseStats().eggGroups)
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
                StoragePosition position = poke.getPosition();
                return o.equalsIgnoreCase("box") ?
                        String.valueOf(((int) (position.box - papiIndexOffset))) : o.equalsIgnoreCase("order") ?
                        String.valueOf(((int) (position.order - papiIndexOffset))) : "未知属性";
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
                return nickname == null ? poke.getSpecies().name : nickname;
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
                return poke.isShiny() ? "是" : "否";
            case "inranch":
                return String.valueOf(poke.isInRanch());
            case "caughtball":
                return poke.getCaughtBall().getLocalizedName();
            case "form": {
                String s = split[3];
                return s.equalsIgnoreCase("number") ? String.valueOf(poke.getForm()) :
                        s.equalsIgnoreCase("localizedname") ? poke.getFormEnum().getLocalizedName() :
                                "未知参数";
            }
            case "nbt": {
                JsonElement json = gson.fromJson(poke.writeToNBT(new NBTTagCompound()).toString(), JsonObject.class);
                String path = split[3];
                String[] keys = path.split("\\.");
                for (String key : keys) {
                    if (json == null||json.isJsonNull()) return "无数据";
                    if (json.isJsonArray()){
                        json = ((JsonArray) json).get(Integer.parseInt(key));
                        continue;
                    }
                    json = ((JsonObject) json).get(key);
                    if (json == null||json.isJsonNull()) return "无数据";
                }
                return json.getAsString();
            }
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

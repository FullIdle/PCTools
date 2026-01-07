package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.val;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.figsq.pctools.pctools.api.StorageHelper;
import me.fullidle.ficore.ficore.FICoreAPI;
import me.fullidle.ficore.ficore.common.api.pokemon.Stats;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.figsq.pctools.pctools.api.Config.*;

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

    @Override
    public boolean persist() {
        return true;
    }

    @SneakyThrows
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        IPokemonWrapper<?> poke = null;
        ArrayList<String> args = Lists.newArrayList(params.split("_"));
        val pokemonFactory = FICoreAPI.getPokemonWrapperFactory();
        val speciesFactory = FICoreAPI.getSpeciesWrapperFactory();

        String pokeO = args.get(0);
        if (params.contains("{") && params.contains("}")) {
            int start = params.indexOf('{');
            int end = params.lastIndexOf('}') + 1;
            if (pokeO.length() != 2) {
                String substring = params.substring(start, end);
                poke = pokemonFactory.create(substring);
                args = Lists.newArrayList(params.replace(substring, "").split("_"));
                //删一
            }
        } else if (pokeO.startsWith("pokedex:")) {
            poke = pokemonFactory.create(speciesFactory.create(Integer.parseInt(pokeO.substring(8))));
        } else if (pokeO.startsWith("species:")) {
            poke = pokemonFactory.create(speciesFactory.create(pokeO.substring(8)));
        } else {
            int box = (int) (Double.parseDouble(pokeO) + papiIndexOffset);
            int order = (int) (Double.parseDouble(args.get(1)) + papiIndexOffset);
            poke = StorageHelper.getPokemon(player.getUniqueId(), new StoragePos(box, order));
            //删两
            args.remove(0);
        }
        args.remove(0);
        return papiReplace(parsePoke(player, poke, args), args);
    }

    public static String papiReplace(String request, ArrayList<String> args) {
        a:
        for (Map.Entry<String, ConfigurationSection> entry : argsPapiReplace.entrySet()) {
            String old = entry.getKey();
            if (old.contains("_")) {
                String[] ss = old.split("_");
                if (ss.length > args.size()) continue;
                for (int i = 0; i < ss.length; i++) {
                    if (!ss[i].equalsIgnoreCase(args.get(i))) continue a;
                }
                ConfigurationSection value = entry.getValue();
                for (String key : value.getKeys(false)) {
                    request = request.replace(key, value.getString(key));
                }
                continue;
            }
            if (args.get(0).equalsIgnoreCase(old)) {
                ConfigurationSection value = entry.getValue();
                for (String key : value.getKeys(false)) {
                    request = request.replace(key, value.getString(key));
                }
            }
        }

        for (Map.Entry<String, String> entry : globalPapiReplace.entrySet()) {
            if (request.contains(entry.getKey())) {
                request = request.replace(entry.getKey(), entry.getValue());
            }
        }
        return request;
    }

    @SneakyThrows
    public static String parsePoke(OfflinePlayer player, IPokemonWrapper<?> poke, List<String> args) {
        if (args.isEmpty()) {
            return "WRONG FORMAT";
        }
        String arg = args.get(0).toLowerCase();
        val storageManager = FICoreAPI.getPokeStorageManager();
        if (poke == null) {
            //无宝可梦变量
            if (arg.equalsIgnoreCase("pokeslot") ||
                    arg.equalsIgnoreCase("pcslot") ||
                    arg.equalsIgnoreCase("partyslot")
            ) {
                val pc = storageManager.getPC(player.getUniqueId());
                val party = storageManager.getParty(player.getUniqueId());
                switch (arg) {
                    case "pokeslot": {
                        val i = party.all().size() + pc.all().size();
                        return String.valueOf(i);
                    }
                    case "pcslot": {
                        return String.valueOf(pc.all().size());
                    }
                    case "partyslot": {
                        return String.valueOf(party.all().size());
                    }
                }
            }
            //无宝可梦变量
            return "POKE IS EMPTY";
        }

        switch (arg) {
            case "assets":/* {
                String assetsPath = args.get(1).startsWith("/") ? args.get(1) : "/" + args.get(1);
                String jsonPath = args.get(2);
                System.out.println(assetsPath);
                System.out.println(jsonPath);
                try (
                        InputStreamReader reader = new InputStreamReader(Pixelmon.class.getResourceAsStream(assetsPath));
                ) {
                    return GsonParser.parse(gson.fromJson(reader, JsonObject.class), jsonPath);
                } catch (Exception e) {
                    return "UNKNOWN PARAMETERS";
                }
            }*/
            case "description":/* {
                return I18n.func_74838_a("pixelmon." + poke.getSpecies().getPokemonName().toLowerCase() + ".description");
            }*/
            case "hypertrained":/* {
                StatsType type = StatsType.getStatsEffect(getStatsType(args.get(1).toLowerCase()).name());
                if (type == null) return "UNKNOWN PARAMETERS";
                return String.valueOf(poke.getIVs().isHyperTrained(type));
            }*/
            case "mintnature":/* {
                return poke.getMintNature().getLocalizedName();
            }*/
            case "catchrate":/* {
                return String.valueOf(poke.getBaseStats().getCatchRate());
            }*/
            case "weight":/* {
                return String.valueOf(poke.getBaseStats().getWeight());
            }*/
            case "malepercent":/* {
                return String.valueOf(((int) poke.getBaseStats().getMalePercent()));
            }*/
            case "types": {
                return new ArrayList<>(poke.getSpecies().getTypes()).toString();
            }
            case "formtypes": {
                return new ArrayList<>(poke.getTypes()).toString();
            }
            case "egggroup":/* {
                return Arrays.toString(poke.getSpecies().getBaseStats().eggGroups);
            }*/
            case "basestats":/* {
                if (args.get(1).equalsIgnoreCase("json")) {
                    try (Reader reader = new InputStreamReader
                            (
                                    BaseStats.class.getResourceAsStream
                                            (
                                                    "/assets/pixelmon/stats/" + poke.getSpecies().getNationalPokedexNumber() + ".json"
                                            )
                            )
                    ) {
                        String parse;
                        try {
                            parse = GsonParser.parse(gson.fromJson(reader, JsonObject.class), args.get(2));
                        } catch (Exception e) {
                            return "UNKNOWN PARAMETERS";
                        }
                        return parse;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return stats(args.get(1), poke.getBaseStats());
            }*/
            case "formstats":/* {
                return stats(args.get(2), poke.getBaseStats().forms.get(Integer.parseInt(args.get(1))));
            }*/
            case "basetotal":/* {
                return String.valueOf(addUp(poke.getBaseStats().stats.values()));
            }*/
            case "eggsteps":/*
                return String.valueOf(poke.getEggSteps());*/
            case "eggcycles":/*
                return String.valueOf(poke.getEggCycles());*/
            case "growth":/*
                return poke.getGrowth().getLocalizedName();*/
            case "helditem":/*
                ItemHeld held = poke.getHeldItemAsItemHeld();
                if (held == NoItem.noItem) {
                    return "NONE";
                }
                return held.getLocalizedName();*/
            case "status":/*
                return poke.getStatus().type.getLocalizedName();*/
            case "hastag":/*
                return String.valueOf(poke.hasSpecFlag(args.get(1).toLowerCase()));*/
            case "moveset":/* {
                int i = Integer.parseInt(args.get(1));
                Attack attack = poke.getMoveset().attacks[i];

                if (attack == null) return "NOTE";

                if (args.size() <= 2) {
                    return attack.getMove().getLocalizedName();
                }
                //描述
                String sub_arg = args.get(2).toLowerCase();
                switch (sub_arg) {
                    case "id": {
                        return String.valueOf(attack.getMove().getAttackId());
                    }
                    case "type": {
                        return attack.getMove().getAttackType().name();
                    }
                    case "ac": {
                        return attack.getMove().getAttackCategory().name();
                    }
                    case "bp": {
                        return String.valueOf(attack.getMove().getBasePower());
                    }
                    case "ppb": {
                        return String.valueOf(attack.getMove().getPPBase());
                    }
                    case "ppm": {
                        return String.valueOf(attack.getMove().getPPMax());
                    }
                    case "acc": {
                        return String.valueOf(attack.getMove().getAccuracy());
                    }
                    case "mc": {
                        return String.valueOf(attack.getMove().getMakesContact());
                    }
                    case "desc": {
                        String key = "attack." + attack.getMove().getAttackName().toLowerCase().replace(" ", "_") + ".description";
                        return I18n.func_74838_a(key);
                    }
                }
            }*/
            case "localizedname":/*
                return poke.getLocalizedName();*/
            case "nickname":/*
                String nickname = poke.getNickname();
                return nickname == null || nickname.replace(" ", "").isEmpty() ? poke.getLocalizedName() : nickname;
*/
            case "formability":/*
                return Arrays.toString(poke.getBaseStats().forms.get(args.get(1)).getAbilitiesArray());*/
            case "caughtball":/*
                return poke.getCaughtBall().getLocalizedName();*/
            case "form":/* {
                String s = args.get(1);
                return s.equalsIgnoreCase("number") ? String.valueOf(poke.getForm()) :
                        s.equalsIgnoreCase("localizedname") ? poke.getFormEnum().getLocalizedName() :
                                "UNKNOWN PARAMETERS";
            }*/
            case "nbt":/* {
                JsonElement json = gson.fromJson(poke.writeToNBT(new NBTTagCompound()).toString(), JsonObject.class);
                String path = args.get(1);
                String[] keys = path.split("\\.");
                for (String key : keys) {
                    if (json == null || json.isJsonNull()) return "NO DATA";
                    if (json.isJsonArray()) {
                        json = ((JsonArray) json).get(Integer.parseInt(key));
                        continue;
                    }
                    json = ((JsonObject) json).get(key);
                    if (json == null || json.isJsonNull()) return "NO DATA";
                }
                return json.getAsString();
            }*/
            {
                return "暂未实现的变量" + arg;
            }
            case "position": {
                val o = args.get(1);
                val position = poke.getStoragePos();
                return o.equalsIgnoreCase("box") ?
                        String.valueOf(((int) (position.getBox() - papiIndexOffset))) : o.equalsIgnoreCase("order") ?
                        String.valueOf(((int) (position.getSlot() - papiIndexOffset))) : "UNKNOWN PARAMETERS";
            }
            case "statstotal": {
                val stats = poke.getStats();
                return String.valueOf(addUp(Lists.newArrayList(
                        stats.get(Stats.HP),
                        stats.get(Stats.ATTACK),
                        stats.get(Stats.DEFENCE),
                        stats.get(Stats.SPECIAL_ATTACK),
                        stats.get(Stats.SPECIAL_DEFENCE),
                        stats.get(Stats.SPEED)
                )));
            }
            case "stats":
                return stats(args.get(1), poke.getStats());
            case "ivstotal":
                return String.valueOf(total(poke.getIVs()));
            case "evstotal":
                return String.valueOf(total(poke.getEVs()));
            case "nature":
                return poke.getNature().getName();
            case "gender":
                return poke.getGender().name();
            case "uuid":
                return poke.getUUID().toString();
            case "originalname":
                return poke.getSpecies().getName();
            case "ability":
                return poke.getAbility().getName();
            case "islegendary":
                return String.valueOf(poke.isLegend());
            case "isegg":
                return String.valueOf(poke.isEgg());
            case "isultrabeast":
                return String.valueOf(poke.isUltra());
            case "ivs":
                return stats(args.get(1), poke.getIVs());
            case "evs":
                return stats(args.get(1), poke.getEVs());
            case "level":
                return String.valueOf(poke.getLevel());
            case "shiny":
                return String.valueOf(poke.isShiny());
            case "inranch":
                return String.valueOf(poke.inRanch());
            default:
                return "UNKNOWN PARAMETERS";
        }
    }

    public static String stats(String arg, Map<Stats, Integer> map) {
        val lowerCase = arg.toLowerCase();
        val type = getStatsType(lowerCase);
        if (type != null) return String.valueOf(map.get(type));
        return null;
    }

    private static Stats getStatsType(String lowerCase) {
        switch (lowerCase) {
            case "hp":
                return Stats.HP;
            case "sp":
            case "speed":
                return Stats.SPEED;
            case "at":
            case "attack":
                return Stats.ATTACK;
            case "df":
            case "defence":
                return Stats.DEFENCE;
            case "sd":
            case "specialdefence":
                return Stats.SPECIAL_DEFENCE;
            case "sa":
            case "specialattack":
                return Stats.SPECIAL_ATTACK;
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

    private static int total(Map<Stats, Integer> map) {
        int t = 0;
        for (Stats value : Stats.values()) t += map.get(value);
        return t;
    }
}

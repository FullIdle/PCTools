package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.items.ItemHeld;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.ITranslatable;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.enums.SpecialType;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.Tuple;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PokeUtil {
    public static Map<EnumSpecies, Pair<String, List<String>>> specialNAL = new HashMap<>();
    public static Map<String, ISearchProperty> searchProperties = new HashMap<>();

    public static void init(){
        specialNAL.clear();


        FileConfiguration config = Cache.plugin.getConfig();

        //special name lore
        for (String key : config.getConfigurationSection("item.special").getKeys(false)) {
            Optional<EnumSpecies> optional = EnumSpecies.getFromName(key);
            if (!optional.isPresent()) {
                continue;
            }
            EnumSpecies species = optional.get();
            PokeUtil.specialNAL.put(
                    species,
                    Pair.of(config.getString("item.special." + key + ".name"),
                            config.getStringList("item.special." + key + ".lore"))
            );
        }
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
        Pair<String, List<String>> pair = specialNAL.get(pokemon.getSpecies());
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
        itemMeta.setDisplayName(PapiUtil.papi(player, fun.apply(name)));
        itemMeta.setLore(PapiUtil.papi(player, lore, fun));
        copy.setItemMeta(itemMeta);
        return copy;
    }

    public static UUID getFormatItemUUID(ItemStack itemStack) {
        if (itemStack == null) return null;

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

    static {
        PokeUtil.searchProperties.put("name", new ISearchProperty() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.getLocalizedName().equalsIgnoreCase(arg)
                        || poke.getSpecies().name.equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player,String value) {
                UUID uniqueId = player.getUniqueId();
                PlayerPartyStorage party = Pixelmon.storageManager.getParty(uniqueId);
                PCStorage pc = Pixelmon.storageManager.getPCForPlayer(uniqueId);
                ArrayList<Pokemon> pokemons = Lists.newArrayList(pc.getAll());
                Collections.addAll(pokemons,party.getAll());
                pokemons.removeIf(Objects::isNull);
                List<String> collect = pokemons.stream().map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //性别
        PokeUtil.searchProperties.put("gender", new ISearchProperty() {
            @Override
            public String getName() {
                return "gender";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Gender gender = poke.getGender();
                return gender.name().equalsIgnoreCase(arg) ||
                        gender.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Gender.values()).map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //属性
        PokeUtil.searchProperties.put("type1", new ISearchProperty() {
            @Override
            public String getName() {
                return "type1";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumType type1 = poke.getBaseStats().getType1();
                return type1.name().equalsIgnoreCase(arg) ||
                        type1.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = EnumType.getAllTypes().stream().map(EnumType::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        PokeUtil.searchProperties.put("type2", new ISearchProperty() {
            @Override
            public String getName() {
                return "type2";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumType type2 = poke.getBaseStats().getType2();
                return type2.name().equalsIgnoreCase(arg) ||
                        type2.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = EnumType.getAllTypes().stream().map(EnumType::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //特性
        PokeUtil.searchProperties.put("ability", new ISearchProperty() {
            @Override
            public String getName() {
                return "ability";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                AbilityBase ability = poke.getAbility();
                return poke.getAbilityName().equalsIgnoreCase(arg) ||
                        ability.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
        //性格
        PokeUtil.searchProperties.put("nature", new ISearchProperty() {
            @Override
            public String getName() {
                return "nature";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumNature nature = poke.getNature();
                return nature.name().equalsIgnoreCase(arg) ||
                        nature.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(EnumNature.values()).map(EnumNature::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //持有物品
        PokeUtil.searchProperties.put("helditem", new ISearchProperty() {
            @Override
            public String getName() {
                return "helditem";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                if (poke.getHeldItem() == null ||
                        CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) ((Object) poke.getHeldItem()))
                                .getType().equals(Material.AIR)) {
                    return false;
                }
                ItemHeld held = poke.getHeldItemAsItemHeld();
                return held.getLocalizedName().equalsIgnoreCase(arg) ||
                        held.getHeldItemType().name().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(EnumHeldItems.values()).map(EnumHeldItems::name).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s->s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //闪光
        PokeUtil.searchProperties.put("shiny", new ISearchProperty() {
            @Override
            public String getName() {
                return "shiny";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.isShiny() == Boolean.parseBoolean(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Arrays.asList("false","true");
            }
        });
        //自定义名
        PokeUtil.searchProperties.put("nickname", new ISearchProperty() {
            @Override
            public String getName() {
                return "nickname";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.getNickname().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
    }
}

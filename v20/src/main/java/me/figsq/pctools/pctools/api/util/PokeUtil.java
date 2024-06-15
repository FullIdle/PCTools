package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.config.StorageConfig;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.util.ITranslatable;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.items.HeldItem;
import lombok.SneakyThrows;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.enums.SpecialType;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PokeUtil {
    public static Map<Species, Pair<String, List<String>>> specialNAL = new HashMap<>();
    public static Map<String, ISearchProperty> searchProperties = new HashMap<>();
    public static int computerBoxes;

    @SneakyThrows
    public static void init(){
        specialNAL.clear();

        //computerBoxes
        StorageConfig storage = PixelmonConfigProxy.getStorage();
        Field field = StorageConfig.class.getDeclaredField("computerBoxes");
        field.setAccessible(true);
        computerBoxes = (int) field.get(storage);

        FileConfiguration config = Cache.plugin.getConfig();

        //special name lore
        for (String key : config.getConfigurationSection("item.special").getKeys(false)) {
            Optional<Species> optional = PixelmonSpecies.fromName(key).getValue();
            if (!optional.isPresent()) {
                continue;
            }
            Species species = optional.get();
            specialNAL.put(
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
        net.minecraft.world.item.ItemStack photo = SpriteItemHelper.getPhoto(pokemon);
        net.minecraft.nbt.CompoundTag tag = photo.m_41783_() == null ? new net.minecraft.nbt.CompoundTag() : photo.m_41783_();
        tag.m_128359_("pctoolsUUID", pokemon.getUUID().toString());
        photo.m_41751_(tag);
        Player player = pokemon.getOwnerPlayer().getBukkitEntity().getPlayer();
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

        net.minecraft.world.item.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.nbt.CompoundTag nbt = copy.m_41783_() == null ? new net.minecraft.nbt.CompoundTag() : copy.m_41783_();
        if (nbt.m_128403_("pctoolsUUID")) {
            return UUID.fromString(nbt.m_128461_("pctoolsUUID"));
        }
        return null;
    }

    public static net.minecraft.util.Tuple<PokemonStorage, StoragePosition> computeStorageAndPosition(int clickSlot, PlayerPartyStorage partyStorage, PCBox pcBox) {
        if (Cache.invBackpackSlot.contains(clickSlot))
            return new net.minecraft.util.Tuple<>(partyStorage, new StoragePosition(-1, Cache.invBackpackSlot.indexOf(clickSlot)));
        if (Cache.invPcSlot.contains(clickSlot))
            return new net.minecraft.util.Tuple<>(pcBox, new StoragePosition(pcBox.boxNumber, Cache.invPcSlot.indexOf(clickSlot)));
        return null;
    }

    static {
        searchProperties.put("name", new ISearchProperty() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.getLocalizedName().equalsIgnoreCase(arg)
                        || Pokedex.FULL_POKEDEX.get(poke.getSpecies().getDex()).getName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                UUID uniqueId = player.getUniqueId();
                PlayerPartyStorage party = StorageProxy.getPartyNow(uniqueId);
                PCStorage pc = StorageProxy.getPCForPlayerNow(uniqueId);
                ArrayList<Pokemon> pokemons = Lists.newArrayList(pc.getAll());
                Collections.addAll(pokemons, party.getAll());
                pokemons.removeIf(Objects::isNull);
                List<String> collect = pokemons.stream().map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //性别
        searchProperties.put("gender", new ISearchProperty() {
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
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //属性
        searchProperties.put("type1", new ISearchProperty() {
            @Override
            public String getName() {
                return "type1";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Element element = Element.parseOrNull(arg);
                if (element == null) return false;
                return poke.getForm().hasType(element);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Element.getAllTypes().stream().map(Element::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        searchProperties.put("type2", new ISearchProperty() {
            @Override
            public String getName() {
                return "type2";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Element element = Element.parseOrNull(arg);
                if (element == null) return false;
                return poke.getForm().hasType(element);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Element.getAllTypes().stream().map(Element::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //特性
        searchProperties.put("ability", new ISearchProperty() {
            @Override
            public String getName() {
                return "ability";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Ability ability = poke.getAbility();
                return poke.getAbilityName().equalsIgnoreCase(arg) ||
                        ability.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
        //性格
        searchProperties.put("nature", new ISearchProperty() {
            @Override
            public String getName() {
                return "nature";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Nature nature = poke.getNature();
                return nature.name().equalsIgnoreCase(arg) ||
                        nature.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Nature.values()).map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //持有物品
        searchProperties.put("helditem", new ISearchProperty() {
            @Override
            public String getName() {
                return "helditem";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                if (CraftItemStack.asBukkitCopy(poke.getHeldItem())
                        .getType().equals(Material.AIR)) {
                    return false;
                }
                HeldItem held = poke.getHeldItemAsItemHeld();
                return held.getLocalizedName().equalsIgnoreCase(arg) ||
                        held.getHeldItemType().name().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(EnumHeldItems.values()).map(EnumHeldItems::name).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //闪光
        searchProperties.put("shiny", new ISearchProperty() {
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
        searchProperties.put("nickname", new ISearchProperty() {
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

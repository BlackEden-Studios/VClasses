package com.bestudios.classx.classes;

import com.bestudios.classx.Classes;
import com.bestudios.classx.ArmorListener;
import com.bestudios.classx.ClassX;
import com.bestudios.corex.utils.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ClassType extends ConfigLoader {

    ClassType(String dedicatedFile, Classes newClass) {
        super(ClassX.getInstance().getDataFolder(), dedicatedFile, "ClassX");
        thisClass = newClass;
        debug = ClassX.getInstance().isDebugMode();
    }

    /**
     * Class specific identifier
     */
    private final Classes thisClass;

    /**
     * True if the current class configs have been loaded successfully, false otherwise
     */
    private boolean loaded;

    /**
     * True if the current class is disabled, false otherwise
     */
    private boolean disabled = false;

    /**
     * Cache of the specific class armor
     */
    protected Map<String, Classes> classArmorPrivateCache = new HashMap<>();

    /**
     * Cache of the specific class buffs
     */
    public List<PotionEffect> classBuffPrivateCache = new ArrayList<>();

    /**
     * List of strings containing the various equip slots
     */
    String[] equipSlots = new String[]{"head", "chest", "legs", "feet"};

    public Classes getThisClass() { return thisClass; };
    public Map<String, Classes> getClassCache() {
        if (loaded) return classArmorPrivateCache;
        else return new HashMap<>();
    }


    public boolean isLoaded() { return loaded; }
    public boolean isDisabled() { return disabled; }
    public void disableClass() { disabled = true; }
    public void enableClass() { disabled = false; }
    public final boolean debug;

    /**
     * Loads the configurations for the class from the class-bound configuration file,
     * sets up a cache for the class bound equipment,
     * sets up a listener for the (eventual) class ability
     */
    @Override
    public void load() {
        super.load();
        if(!disabled) {
            try {
                //Armor Cache generation
                ClassX.getInstance().toLog("Trying to generate the " + thisClass.name() + " armor private cache", debug);
                for (String slot : equipSlots) {
                    for (String piece : Objects.requireNonNull(this.getConfig().getStringList("class_type.equip." + slot))) {
                        classArmorPrivateCache.put(piece, thisClass);
                    }
                }

                //Buff Cache generation
                ClassX.getInstance().toLog("Trying to generate the " + thisClass.name() + " buff private cache", debug);
                int buffLevel = -1;
                for (String buff : minecraftBuffs) {
                    buffLevel = -1;
                    buffLevel = this.getConfig().getInt("class_type.effect." + buff + ".level");
                    if (buffLevel > -1) classBuffPrivateCache.add( new PotionEffect(potionEffectTypeMap.get(buff), PotionEffect.INFINITE_DURATION, buffLevel));
                }

                ClassX.getInstance().toLog(thisClass.name() + " private cache generation complete", debug);
                loaded = true;

            } catch (Exception ex) {
                ex.printStackTrace();
                ClassX.getInstance().toLog(thisClass.name() + " Class config format exception");
            }

            ArmorListener.getInstance().loadUnifiedCache(classArmorPrivateCache);

            ClassX.getInstance().toLog(thisClass.name() + " private cache successfully loaded", debug);

            if(!classArmorPrivateCache.isEmpty()) ClassX.getInstance().toLog("The Armor private cache for " + this.thisClass + " is populated", debug);
            if(!classBuffPrivateCache.isEmpty()) ClassX.getInstance().toLog("The Buff private cache for " + this.thisClass + " is populated", debug);

            this.classConfiguration();
            this.classAbility();

        }
    };

    /**
     * Loads the configurations asynchronously
     */
    public void asyncLoad() {

        Bukkit.getScheduler().runTaskAsynchronously(ClassX.getInstance(), this::load);
    }

    /**
     *
     */
    protected void classConfiguration() {

    }

    /**
     * Implementation of the specific class ability
     */
    protected void classAbility() {

    }

    /**
     * Implementation of the specific class abilities cooldown procedure
     */
    public void setClassOnCooldown(Player player) {

    }

    /**
     * Implementation of the specific class dismiss procedure
     */
    public void classDismissed(Player player) {

    }

    /**
     * List of strings containing the various potion buffs
     */
    final String[] minecraftBuffs = new String[] {
            "absorption", "bad_omen", "blindness", "conduit_power", "darkness",
            "dolphins_grace", "fire_resistance", "glowing", "haste", "health_boost",
            "hero_of_the_village", "hunger", "instant_damage", "instant_health", "invisibility",
            "jump_boost", "levitation", "luck", "mining_fatigue", "nausea", "night_vision",
            "poison", "regeneration", "resistance", "saturation", "slow_falling", "slowness",
            "speed", "strength", "unluck", "water_breathing", "weakness", "wither"
    };

    private final Map<String, PotionEffectType> potionEffectTypeMap = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("absorption", PotionEffectType.ABSORPTION),
            new AbstractMap.SimpleEntry<>("bad_omen", PotionEffectType.BAD_OMEN),
            new AbstractMap.SimpleEntry<>("blindness", PotionEffectType.BLINDNESS),
            new AbstractMap.SimpleEntry<>("conduit_power", PotionEffectType.CONDUIT_POWER),
            new AbstractMap.SimpleEntry<>("darkness", PotionEffectType.DARKNESS),
            new AbstractMap.SimpleEntry<>("dolphins_grace", PotionEffectType.DOLPHINS_GRACE),
            new AbstractMap.SimpleEntry<>("fire_resistance", PotionEffectType.FIRE_RESISTANCE),
            new AbstractMap.SimpleEntry<>("glowing", PotionEffectType.GLOWING),
            new AbstractMap.SimpleEntry<>("haste", PotionEffectType.HASTE),
            new AbstractMap.SimpleEntry<>("health_boost", PotionEffectType.HEALTH_BOOST),
            new AbstractMap.SimpleEntry<>("hero_of_the_village", PotionEffectType.HERO_OF_THE_VILLAGE),
            new AbstractMap.SimpleEntry<>("hunger", PotionEffectType.HUNGER),
            new AbstractMap.SimpleEntry<>("instant_damage", PotionEffectType.INSTANT_DAMAGE),
            new AbstractMap.SimpleEntry<>("instant_health", PotionEffectType.INSTANT_HEALTH),
            new AbstractMap.SimpleEntry<>("invisibility", PotionEffectType.INVISIBILITY),
            new AbstractMap.SimpleEntry<>("jump_boost", PotionEffectType.JUMP_BOOST),
            new AbstractMap.SimpleEntry<>("levitation", PotionEffectType.LEVITATION),
            new AbstractMap.SimpleEntry<>("luck", PotionEffectType.LUCK),
            new AbstractMap.SimpleEntry<>("mining_fatigue", PotionEffectType.MINING_FATIGUE),
            new AbstractMap.SimpleEntry<>("nausea", PotionEffectType.NAUSEA),
            new AbstractMap.SimpleEntry<>("night_vision", PotionEffectType.NIGHT_VISION),
            new AbstractMap.SimpleEntry<>("poison", PotionEffectType.POISON),
            new AbstractMap.SimpleEntry<>("regeneration", PotionEffectType.REGENERATION),
            new AbstractMap.SimpleEntry<>("resistance", PotionEffectType.RESISTANCE),
            new AbstractMap.SimpleEntry<>("saturation", PotionEffectType.SATURATION),
            new AbstractMap.SimpleEntry<>("slow_falling", PotionEffectType.SLOW_FALLING),
            new AbstractMap.SimpleEntry<>("slowness", PotionEffectType.SLOWNESS),
            new AbstractMap.SimpleEntry<>("speed", PotionEffectType.SPEED),
            new AbstractMap.SimpleEntry<>("strength", PotionEffectType.STRENGTH),
            new AbstractMap.SimpleEntry<>("unluck", PotionEffectType.UNLUCK),
            new AbstractMap.SimpleEntry<>("water_breathing", PotionEffectType.WATER_BREATHING),
            new AbstractMap.SimpleEntry<>("weakness", PotionEffectType.WEAKNESS),
            new AbstractMap.SimpleEntry<>("wither", PotionEffectType.WITHER)

    );

}

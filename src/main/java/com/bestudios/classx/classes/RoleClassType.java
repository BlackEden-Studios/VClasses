package com.bestudios.classx.classes;

import com.bestudios.classx.managers.ArmorListener;
import com.bestudios.classx.ClassX;
import com.bestudios.corex.basics.ConfigLoader;
import com.bestudios.corex.utils.EffectsLibrary;
import com.bestudios.corex.caches.SmartCache;
import com.bestudios.corex.basics.TimerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * RoleClassType is the base class for all role classes in ClassX.
 * It handles the loading of class configurations, armor caches, potion effects,
 * and cooldown management for class abilities.
 */
public class RoleClassType extends ConfigLoader {

    /**
     * Directory where all class configurations are stored
     */
    static File classesDir = new File(com.bestudios.classx.ClassX.getInstance().getDataFolder() + File.separator + "classes");

    RoleClassType(String dedicatedFile, RoleClassEnum newClass) {
        super(classesDir, dedicatedFile, "ClassX", defaultYml);
        thisClass = newClass;

        this.classConfiguration();
        this.classAbility();
    }

    /**
     * Class specific identifier
     */
    private final RoleClassEnum thisClass;
    /**
     * True if the current class configs have been loaded successfully, false otherwise
     */
    private boolean loaded = false;
    /**
     * True if the current class is disabled, false otherwise
     */
    private boolean disabled = false;
    /**
     * Cooldown for the class activation, in seconds
     * This is being loaded from the ClassX config file
     */
    private final int classActivationCooldown = ClassX.getInstance().getConfig().getInt("class_activation_cooldown", 3);
    /**
     * Cache of the specific class armor
     */
    protected Map<String, RoleClassEnum> classArmorPrivateCache = new HashMap<>();
    /**
     * Cache of the specific class buffs
     */
    public final List<PotionEffect> classPotionEffectsPrivateCache = new ArrayList<>();
    /**
     * Smart cache for the class cooldown handling
     */
    protected final SmartCache<TimerInfo> cooldownCache = new SmartCache<>();
    /**
     * List of strings containing the various equipment slots
     */
    String[] equipSlots = new String[]{"head", "chest", "legs", "feet"};

    /**
     * Returns the class identifier
     *
     * @return RoleClassEnum representing the class
     */
    public RoleClassEnum getThisClass() { return thisClass; };
    /**
     * Returns the class-specific armor cache
     *
     * @return Map of String to RoleClassEnum representing the class armor cache
     */
    public Map<String, RoleClassEnum> getClassArmorCache() {
        if (loaded) return classArmorPrivateCache;
        else return new HashMap<>();
    }

    /** Returns true if the class configs have been loaded successfully, false otherwise */
    public boolean isLoaded() { return loaded; }
    /** Returns true if the class is disabled, false otherwise */
    public boolean isDisabled() { return disabled; }

    /**
     * Disables the class, preventing it from being used
     */
    public void disableClass() { disabled = true; }
    /**
     * Enables the class, allowing it to be used again
     */
    public void enableClass() { disabled = false; }

    /**
     * Implementation of the specific class abilities cooldown procedure
     */
    public void setClassOnCooldown(Player player) {
        cooldownCache.put(player.getUniqueId(), new TimerInfo(classActivationCooldown));
    }

    /**
     * Loads the configurations for the class from the class-bound configuration file,
     * sets up a cache for the class bound equipment,
     * sets up a listener for the (eventual) class ability
     */
    @Override
    public void setUpConfigs() {
        super.setUpConfigs();
        if(!disabled) {
            try {
                // Armor Cache generation
                ClassX.getInstance().toLog("Starting to generate the " + thisClass.name() + " armor private cache", ClassX.getInstance().isDebugMode());
                for (String slot : equipSlots) {
                    for (String piece : Objects.requireNonNull(this.getConfig().getStringList("class_type.equip." + slot))) {
                        classArmorPrivateCache.put(piece, thisClass);
                    }
                }
                /*
                 Buff Cache generation
                 The potion effects levels are being defaulted to 0,
                 but the amplifier is being lowered by 1 in the constructor
                 since an amplifier with value 0 will result in a level I effect
                 */
                ClassX.getInstance().toLog("Starting to generate the " + thisClass.name() + " buff private cache", ClassX.getInstance().isDebugMode());
                int defaultValueForBuffs = 0, buffLevel;
                for (String buff : EffectsLibrary.implementedEffects()) {
                    buffLevel = this.getConfig().getInt("class_type.effect." + buff + ".level", defaultValueForBuffs);
                    if (buffLevel > 0) classPotionEffectsPrivateCache.add( new PotionEffect(EffectsLibrary.getEffectByName(buff), PotionEffect.INFINITE_DURATION, buffLevel-1));
                }
                ClassX.getInstance().toLog(thisClass.name() + " private caches generation complete", ClassX.getInstance().isDebugMode());
                loaded = true;

            } catch (Exception ex) {
                ClassX.getInstance().toLog(thisClass.name() + " Class config format exception\n" + ex);
            }
            // Armor cache is being unified with the others
            ArmorListener.getInstance().addToUnifiedCache(classArmorPrivateCache);
        }
    };

    /**
     * Implementation of the specific class configuration, called in the constructor
     */
    protected void classConfiguration() {

    }

    /**
     * Implementation of the specific class ability, called in the constructor
     */
    protected void classAbility() {

    }

    /**
     * Implementation of the specific class dismiss procedure
     */
    public void dismissRoleClass(Player player) {

    }

    /**
     * Loads the configurations asynchronously
     * DO NOT USE
     */
    public void asyncLoad() {

        Bukkit.getScheduler().runTaskAsynchronously(ClassX.getInstance(), this::setUpConfigs);
    }

    /**
     * Default YML configuration for the class type.
     */
    private static final String defaultYml = """
                class_type:
                  equip:
                    head:
                    - minecraft:leather_helmet
                    - minecraft:chainmail_helmet
                    - minecraft:iron_helmet
                    - minecraft:diamond_helmet
                    - minecraft:golden_helmet
                    - minecraft:netherite_helmet
                    - minecraft:turtle_helmet
                    chest:
                    - minecraft:leather_chestplate
                    - minecraft:chainmail_chestplate
                    - minecraft:iron_chestplate
                    - minecraft:diamond_chestplate
                    - minecraft:golden_chestplate
                    - minecraft:netherite_chestplate
                    legs:
                    - minecraft:leather_leggings
                    - minecraft:chainmail_leggings
                    - minecraft:iron_leggings
                    - minecraft:diamond_leggings
                    - minecraft:golden_leggings
                    - minecraft:netherite_leggings
                    feet:
                    - minecraft:leather_boots
                    - minecraft:chainmail_boots
                    - minecraft:iron_boots
                    - minecraft:diamond_boots
                    - minecraft:golden_boots
                    - minecraft:netherite_boots
                  effect:
                    absorption:
                      level: 0
                    bad_omen:
                      level: 0
                    blindness:
                      level: 0
                    conduit_power:
                      level: 0
                    darkness:
                      level: 0
                    dolphins_grace:
                      level: 0
                    fire_resistance:
                      level: 0
                    glowing:
                      level: 0
                    haste:
                      level: 0
                    health_boost:
                      level: 0
                    hero_of_the_village:
                      level: 0
                    hunger:
                      level: 0
                    instant_damage:
                      level: 0
                    instant_health:
                      level: 0
                    invisibility:
                      level: 0
                    jump_boost:
                      level: 0
                    levitation:
                      level: 0
                    luck:
                      level: 0
                    mining_fatigue:
                      level: 0
                    nausea:
                      level: 0
                    night_vision:
                      level: 0
                    poison:
                      level: 0
                    regeneration:
                      level: 0
                    resistance:
                      level: 0
                    saturation:
                      level: 0
                    slow_falling:
                      level: 0
                    slowness:
                      level: 0
                    speed:
                      level: 0
                    strength:
                      level: 0
                    unluck:
                      level: 0
                    water_breathing:
                      level: 0
                    weakness:
                      level: 0
                    wither:
                      level: 0
                """;
}

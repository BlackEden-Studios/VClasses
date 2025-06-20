package com.bestudios.classx.classes;

import com.bestudios.classx.managers.ArmorListener;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.managers.ClassXSettingsManager;
import com.bestudios.corex.basics.ConfigLoader;
import com.bestudios.corex.utils.EffectsLibrary;
import com.bestudios.corex.caches.SmartCache;
import com.bestudios.corex.basics.TimerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class RoleClassType extends ConfigLoader {

    RoleClassType(String dedicatedFile, RoleClassEnum newClass) {
        super(ClassX.getInstance().getDataFolder(), dedicatedFile, "ClassX");
        thisClass = newClass;
        debug = ClassX.getInstance().isDebugMode();

        this.classConfiguration();
        this.classAbility();
    }

    /**
     * Class specific identifier
     */
    private final RoleClassEnum thisClass;

    /**
     * True if the plugin is being loaded in debug mode, false otherwise
     */
    public final boolean debug;

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
     * List of strings containing the various equip slots
     */
    String[] equipSlots = new String[]{"head", "chest", "legs", "feet"};

    public RoleClassEnum getThisClass() { return thisClass; };

    public Map<String, RoleClassEnum> getClassArmorCache() {
        if (loaded) return classArmorPrivateCache;
        else return new HashMap<>();
    }

    public boolean isLoaded() { return loaded; }
    public boolean isDisabled() { return disabled; }
    public void disableClass() { disabled = true; }
    public void enableClass() { disabled = false; }

    /**
     * Implementation of the specific class abilities cooldown procedure
     */
    public void setClassOnCooldown(Player player) {
        cooldownCache.put(player.getUniqueId(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown()));
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
                ClassX.getInstance().toLog("Starting to generate the " + thisClass.name() + " armor private cache", debug);
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
                ClassX.getInstance().toLog("Starting to generate the " + thisClass.name() + " buff private cache", debug);
                int defaultValueForBuffs = 0;
                int buffLevel = defaultValueForBuffs;
                for (String buff : EffectsLibrary.implementedEffects()) {
                    buffLevel = this.getConfig().getInt("class_type.effect." + buff + ".level", defaultValueForBuffs);
                    if (buffLevel > 0) classPotionEffectsPrivateCache.add( new PotionEffect(EffectsLibrary.getEffectByName(buff), PotionEffect.INFINITE_DURATION, buffLevel-1));
                }
                ClassX.getInstance().toLog(thisClass.name() + " private caches generation complete", debug);
                loaded = true;

            } catch (Exception ex) {
                ex.printStackTrace();
                ClassX.getInstance().toLog(thisClass.name() + " Class config format exception");
            }
            // Armor cache is being unified with the others
            ArmorListener.getInstance().loadUnifiedCache(classArmorPrivateCache);
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

}

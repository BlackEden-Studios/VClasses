package com.bestudios.vclasses.classes;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.cache.SmartCache;
import com.bestudios.fulcrum.api.configuration.DefaultConfigurationHolder;
import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import com.bestudios.fulcrum.api.util.EffectsUtils;
import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.data.ClassLoadingException;
import com.bestudios.vclasses.managers.ArmorListener;
import com.bestudios.vclasses.util.DummySaver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RoleClassType is the base class for all role classes.
 * It provides the common functionality for all classes, such as the ability to load the class configuration,
 * the ability to dismiss the class, and the ability to set up the class cooldown timer.
 *
 * @version 0.12.0
 */
public abstract class RoleClassType extends DefaultConfigurationHolder {

  /** Directory where the classes are stored */
  static File classesDir = new File(VClasses.getInstance().getDataFolder(), "classes");
  /** List of strings containing the various equipment slots */
  static String[] equipSlots = new String[]{"head", "chest", "legs", "feet"};
  /** Cooldown timer for the class activation */
  public static int cooldownTimer = VClasses.getInstance().getConfig().getInt("class.activation_cooldown", 10);
  /** CustomItemsService instance */
  static CustomItemsService customItemsService =
    Objects.requireNonNull(VClasses.getInstance().getServer()
                                                 .getServicesManager()
                                                 .getRegistration(CustomItemsService.class))
                                                 .getProvider();

  /** Class specific identifier */
  private final RoleClassEnum classEnum;
  /** Reference to the plugin instance */
  protected final FulcrumPlugin plugin;
  /** True if the current class configs have been enabled successfully, false otherwise */
  protected boolean enabled;

  /** Cache of the specific class buffs */
  protected final List<PotionEffect> classPotionEffectsPrivateCache;
  /** Cache for the class activation cooldown */
  protected final SmartCache<TimerInfo> cooldownCache;

  /**
   * Constructor for the RoleClassType class.
   * @param fileName The name of the configuration file for the class.
   * @param newClass The RoleClassEnum corresponding to the class.
   */
  RoleClassType(String fileName, RoleClassEnum newClass) {
    super(VClasses.getInstance(), classesDir, new File(classesDir, fileName));

    this.classEnum = newClass;
    this.plugin = VClasses.getInstance();

    this.classPotionEffectsPrivateCache = new ArrayList<>();
    this.cooldownCache                  = new SmartCache<>(VClasses.getInstance(), new DummySaver<>());
  }

  /**
   * Returns the RoleClassEnum corresponding to the class.
   * @return The RoleClassEnum corresponding to the class.
   */
  public RoleClassEnum getClassEnum() {
    return classEnum;
  }

  /**
   * Returns true if the class is enabled, false otherwise.
   * @return True if the class is enabled, false otherwise.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Enables or disables the class.
   * @param value True to enable the class, false to disable it.
   */
  public void enableClass(boolean value) {
    this.enabled = value;
  }

  /**
   * Implementation of the specific class abilities cooldown procedure
   *
   * @param player Player to be affected
   */
  public void setCooldownForPlayer(Player player) {
    cooldownCache.put(player.getUniqueId(), new TimerInfo(cooldownTimer));
  }

  /**
  * Initializes the class by loading the configuration
  */
  public void initialize() {
    try {
      this.commonSetup();

      this.additionalSetup();
      this.abilityConfiguration();

      this.enabled = true;
      plugin.getLogger().info("Loaded the class " + classEnum.name());
    } catch (ClassLoadingException e) {
      plugin.getLogger().severe("Could not load the class " + classEnum.name() + " : " + e.getMessage());
      this.enabled = false;
    }
  }

  /**
   * Loads the configurations for the class from the class-bound configuration file,
   * sets up a cache for the class-bound equipment and buffs, and registers the class in the ArmorListener.
   */
  private void commonSetup() throws ClassLoadingException {
    YamlConfiguration config = this.getConfig();
    if (config == null) throw new ClassLoadingException("Failed to load the config for the class " + classEnum.name());

    // Armor Cache generation
    final Map<String, RoleClassEnum> classArmorPrivateCache = new ConcurrentHashMap<>();
    for (String slot : equipSlots)
      for (String piece : this.getConfig().getStringList("class_type.equip." + slot))
        classArmorPrivateCache.put(piece, classEnum);
    // Armor cache is being unified with the others
    ArmorListener.getInstance().loadUnifiedCache(classArmorPrivateCache);
    plugin.getLogger().info("Armor cache for " + classEnum.name() + " has been generated");

    /*
     * Buff Cache generation
     * Configuration levels are intended to be count as normal,
     * since the potion effects configured as 0 are being ignored;
     * The amplifier is being lowered by 1 in the constructor for Bukkit logic,
     * since an amplifier with value 0 will result in a level I effect.
     */
    plugin.getLogger().info("Starting to generate the " + classEnum.name() + " buff private cache");
    for (String buff : EffectsUtils.implementedEffects()) {
        int buffLevel = this.getConfig().getInt("class_type.effect.level." + buff, 0);
        if (buffLevel > 0)
          classPotionEffectsPrivateCache.add(
                new PotionEffect(EffectsUtils.getEffectByName(buff), PotionEffect.INFINITE_DURATION, buffLevel-1)
          );
    }
    plugin.getLogger().info("Buff cache for " + classEnum.name() + " has been generated");
  }

  /** Implementation of the specific class configuration, called in the constructor */
  protected void additionalSetup() throws ClassLoadingException {}

  /** Implementation of the specific class ability, called in the constructor */
  protected void abilityConfiguration() throws ClassLoadingException {}

  /** Implementation of the specific class dismiss procedure */
  public void dismissRoleClass(Player player) {
    for(PotionEffect effect : classPotionEffectsPrivateCache)
      player.removePotionEffect(effect.getType());
  }

}

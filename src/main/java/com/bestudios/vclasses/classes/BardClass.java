package com.bestudios.vclasses.classes;

import com.bestudios.fulcrum.api.cache.SmartCache;
import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.data.ClassLoadingException;
import com.bestudios.vclasses.data.PlayersCache;
import com.bestudios.vclasses.util.BardBuffRunnable;
import com.bestudios.vclasses.util.DummySaver;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Bard Class
 *
 * @version 0.12.0
 */
public class BardClass extends RoleClassType {

  /** Instance variable, intended to be used as a Singleton reference */
  protected static BardClass instance = new BardClass();

  /** Horn Types */
  private record HornType(String key, PotionEffectType type, List<Integer> amplifiers) {}

  /** Cooldown time for the right click ability, expressed in seconds */
  private int rightClickCooldownTime;
  /** Normal ability effect duration, expressed in seconds */
  private int defaultBuffDuration;
  /** Right-click ability effect duration, expressed in seconds */
  private int amplifiedBuffDuration;
  /** Normal and right-click abilities applying range, expressed in blocks */
  private double buffRange;

  /** Horn types holder list */
  private final List<HornType> hornTypes;
  /** Amplifiers for the bard ability potion effects */
  private final List<Integer> speedAmplifiers;
  private final List<Integer> strengthAmplifiers;
  private final List<Integer> hasteAmplifiers;
  private final List<Integer> regenerationAmplifiers;

  /** Smart cache for the abilities' runnable */
  private final SmartCache<BardBuffRunnable> bardBuffsCache;

  /**
   * The only way to retrieve the class instance
   * @return instance of the class
   */
  public static BardClass getInstance() {
    return instance;
  }

  /** Private constructor for the Singleton */
  private BardClass() {
    super("bard.yml", RoleClassEnum.BARD);

    this.rightClickCooldownTime = 20;
    this.defaultBuffDuration = 2;
    this.amplifiedBuffDuration = 10;
    this.buffRange = 20;

    this.speedAmplifiers        = new ArrayList<>(Arrays.asList(0, 1));
    this.strengthAmplifiers     = new ArrayList<>(Arrays.asList(0, 1));
    this.hasteAmplifiers        = new ArrayList<>(Arrays.asList(0, 1));
    this.regenerationAmplifiers = new ArrayList<>(Arrays.asList(0, 1));

    this.hornTypes              = new ArrayList<>();

    this.bardBuffsCache = new SmartCache<>(VClasses.getInstance(), new DummySaver<>());
  }

  private void setRightClickCooldownTime(int value) {
    if (value > 0) rightClickCooldownTime = value;
  }

  private void setDefaultBuffDuration(int value) {
    if (value > 0) defaultBuffDuration = value;
  }

  private void setAmplifiedBuffDuration(int value) {
    if (value > 0) amplifiedBuffDuration = value;
  }

  private void setBuffRange(int value) {
    if (value > 0) buffRange = value;
  }

  private void setAmplifiers(List<Integer> cache, int base, int amplified) {
    cache.set(0, Math.max(0, base - 1));
    cache.set(1, Math.max(0, amplified - 1));
  }

  @Override
  protected void additionalSetup() throws ClassLoadingException {
    // Load the class configuration
    ConfigurationSection bardConfig = this.plugin.getConfig().getConfigurationSection("class.bard");
    if (bardConfig == null) throw new ClassLoadingException("Bard Class configuration not found.");

    setBuffRange(              bardConfig.getInt("effect.range",              20));
    setDefaultBuffDuration(    bardConfig.getInt("effect.default_duration",   2));
    setAmplifiedBuffDuration(  bardConfig.getInt("effect.amplified_duration", 10));
    setRightClickCooldownTime( bardConfig.getInt("horn_cooldown",             20));

    // Load the potion effect amplifiers
    ConfigurationSection effectsConfig = bardConfig.getConfigurationSection("effect");
    if (effectsConfig == null) throw new ClassLoadingException("Bard Class effects configuration not found.");

    setAmplifiers(speedAmplifiers,
                  effectsConfig.getInt("speed.level",            1),
                  effectsConfig.getInt("speed.amplified",        2));
    setAmplifiers(strengthAmplifiers ,
                  effectsConfig.getInt("strength.level",         1),
                  effectsConfig.getInt("strength.amplified",     2));
    setAmplifiers(hasteAmplifiers,
                  effectsConfig.getInt("haste.level",            1),
                  effectsConfig.getInt("haste.amplified",        2));
    setAmplifiers(regenerationAmplifiers,
                  effectsConfig.getInt("regeneration.level",     1),
                  effectsConfig.getInt("regeneration.amplified", 2));

    // Cache the horn types
    hornTypes.add(new HornType("speed",    PotionEffectType.SPEED,        speedAmplifiers));
    hornTypes.add(new HornType("strength", PotionEffectType.STRENGTH,     strengthAmplifiers));
    hornTypes.add(new HornType("haste",    PotionEffectType.HASTE,        hasteAmplifiers));
    hornTypes.add(new HornType("regen",    PotionEffectType.REGENERATION, regenerationAmplifiers));
  }

  /**
   * Implementation of the Bard class ability.
   */
  @Override
  protected void abilityConfiguration() {

    this.plugin.getServer().getPluginManager().registerEvents(new Listener() {

      /*
       * Clean up runnable on death
       */
      @EventHandler (priority = EventPriority.HIGH)
      public void onWeakDeath(EntityDeathEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player deadPlayer) cancelAbilityTask(deadPlayer);
      }

      /*
       * Ability: Horn Held
       */
      @EventHandler(priority = EventPriority.HIGH)
      public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack itemHeld = player.getInventory().getItem(event.getNewSlot());

        cancelAbilityTask(player); // Always cancel a previous task when switching slots

        // Base checks
        if (itemHeld == null || !(itemHeld.getType().equals(Material.GOAT_HORN))) return;
        if (!instance.isEnabled()) return;
        if (event.isCancelled()) return;
        if (PlayersCache.getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;

        // Check class cooldown
        if (!TimerInfo.isValid(cooldownCache.get(player.getUniqueId()))) return;

        selectHorn(player, itemHeld, defaultBuffDuration, EFFECT_TYPE.BASE, true);
      }

      /*
       * Ability: Horn Right Click
       */
      @EventHandler(priority = EventPriority.HIGH)
      public void onHornRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemHeld = player.getInventory().getItemInMainHand();

        cancelAbilityTask(player); // Cancel the previous task

        // Base Checks
        if (!(itemHeld.getType().equals(Material.GOAT_HORN))) return;
        if (!instance.isEnabled()) return;
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (PlayersCache.getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        // Check the cooldown
        if (TimerInfo.isValid(cooldownCache.get(player.getUniqueId()))) {
          player.sendMessage(bardCooldownMessage());
          return;
        }

        // Apply cooldown and effect
        cooldownCache.put(player.getUniqueId(), new TimerInfo(rightClickCooldownTime));
        player.setCooldown(Material.GOAT_HORN, rightClickCooldownTime * 20);
        selectHorn(player, itemHeld, amplifiedBuffDuration, EFFECT_TYPE.AMPLIFIED, false);
      }

      /*
       * Cancel task on inventory change
       */
      @EventHandler(priority = EventPriority.HIGH)
      public void onBardInventoryChange(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();
        if (PlayersCache.getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;
        cancelAbilityTask(player);
      }

    }, this.plugin);
  }

  @Override
  public void dismissRoleClass(Player player) {
    super.dismissRoleClass(player);
    cancelAbilityTask(player);
  }

  /**
   * Internal procedure for correctly dismiss a cached runnable
   * @param player the player to cancel the task for
   */
  private void cancelAbilityTask(Player player) {
    try {
      bardBuffsCache.remove(player.getUniqueId());
    } catch (Exception ignored) {
      plugin.getLogger()
            .warning("Caught an exception when trying to cancel the bard runnable for player " + player.getName());
    }
  }

  // 4. The Optimized selectHorn method
  private void selectHorn(
          Player player,
          ItemStack itemHeld,
          int buffDuration,
          EFFECT_TYPE effectType,
          boolean refreshable
  ) {
    // Finding the matching horn
    HornType match = hornTypes.stream()
                              .filter(horn -> customItemsService.getItemName(itemHeld)
                                                                          .toLowerCase(Locale.ROOT)
                                                                          .contains(horn.key))
                              .findFirst()
                              .orElse(null);

    if (match == null) return;

    int amplifierIndex = (effectType == EFFECT_TYPE.AMPLIFIED) ? 1 : 0;
    // Safety check: ensure the vector has enough slots
    if (match.amplifiers.size() <= amplifierIndex) return;

    // Apply the potion effect
    PotionEffect potion = new PotionEffect(
            match.type,
            20 * buffDuration,
            match.amplifiers.get(amplifierIndex)
    );
    BardBuffRunnable task = new BardBuffRunnable(player, potion, buffRange);
    // If the task is not refreshable, run it once and return
    if (!refreshable) {
      task.runTask(plugin);
      return;
    }
    // Otherwise, run it periodically
    task.runTaskTimer(plugin, 1, 20L * defaultBuffDuration);
    bardBuffsCache.put(player.getUniqueId(), task);
  }

  private @NotNull Component bardCooldownMessage() {
    return Component.text(plugin.getLanguageConfiguration().getString("bard.horn.cooldown_message", ""))
                    .color(TextColor.color(0xff0000))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.text("Bard Class")
                                         .color(TextColor.color(0x008000))));
  }

  enum EFFECT_TYPE {
    BASE,
    AMPLIFIED
  }
}
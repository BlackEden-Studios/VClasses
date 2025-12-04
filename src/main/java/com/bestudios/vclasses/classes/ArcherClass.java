package com.bestudios.vclasses.classes;

import com.bestudios.fulcrum.api.cache.SmartCache;
import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.data.ClassLoadingException;
import com.bestudios.vclasses.data.PlayersCache;
import com.bestudios.vclasses.util.DummySaver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * Archer Class
 *
 * @version 0.12.0
 */
public class ArcherClass extends RoleClassType {

  /** Instance variable, intended to be used as a Singleton reference */
  protected static ArcherClass instance = new ArcherClass();

  /** Archer ability weakening duration in seconds */
  private int taggedTime;
  /** Archer ability bonus damage, to apply as a multiplier on the ordinary damage */
  private double damageMultiplier;
  /** Potion effect type given when hitting a previously weakened target */
  private PotionEffectType buffType;
  /** Amplifier for the potion effect given when hitting a previously weakened target */
  private int buffAmplifier;
  /** Smart cache for the entities that are weakened by the archer ability */
  private final SmartCache<TimerInfo> taggedCache;

  /**
   * The only way to retrieve the class instance
   * @return instance of the class
   */
  public static ArcherClass getInstance() {
    return instance;
  }

  /** Private constructor for the Singleton */
  private ArcherClass() {
    super("archer.yml", RoleClassEnum.ARCHER);

    this.taggedTime = 10;
    this.damageMultiplier = 0.2;
    this.buffType = PotionEffectType.SPEED;
    this.buffAmplifier = 2;

    this.taggedCache = new SmartCache<>(1000, VClasses.getInstance(), new DummySaver<>());
  }

  private void setTaggedTime(int value) {
    if (value > 0) taggedTime = value;
  }

  private void setDamageMultiplier(int value) {
    if (value > 0) damageMultiplier = (double) value / 10 ;
  }

  private void setBuffType(PotionEffectType value) {
    Objects.requireNonNull(value);
    buffType = value;
  }

  private void setBuffAmplifier(int value) {
    if (value > 0) buffAmplifier = value-1;
  }

  @Override
  protected void additionalSetup() throws ClassLoadingException {
    ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("class.archer");
    if (config == null) throw new ClassLoadingException("Archer Class configuration not found.");

    setTaggedTime(       config.getInt("tag_time", 10));
    setDamageMultiplier( config.getInt("damage_multiplier", 2));
    setBuffType(         PotionEffectType.getByName(config.getString("buff.type", "SPEED")));
    setBuffAmplifier(    config.getInt("buff.level", 2));
  }

  /**
   * Implementation of the Archer class ability;
   */
  @Override
  protected void abilityConfiguration() {

    /*
     * Listener implemented with 3 related events:
     * - onArrowHitByArcher: when an archer shoots an arrow, it applies the debuff to the target and buff to self
     * - onWeakBeingHit: when an arrow hits a weak entity, it applies the more damages to it
     * - onWeakDeath: when an entity dies, it removes the debuff from it
     */
    this.plugin.getServer().getPluginManager().registerEvents(new Listener() {

      /*
       * Event fired when an archer shoots an arrow.
       * This event is checked last (EventPriority.HIGHEST)
       */
      @EventHandler (priority = EventPriority.HIGHEST)
      public void onArrowHitByArcher(EntityDamageByEntityEvent event) {
        // Base checks
        if (event.isCancelled()) return;
        if (!instance.isEnabled()) return;
        // The attacker must be an Archer player and damaging a living entity with an arrow
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player attackerPlayer)) return;
        if (!(event.getEntity()  instanceof LivingEntity damagedEntity)) return;
        // Check if the attacker is an Archer
        if (PlayersCache.getPlayerCache(attackerPlayer.getUniqueId()).getCurrentClass() != RoleClassEnum.ARCHER) return;
        // Check if the player's class is not active yet
        if (TimerInfo.isValid(cooldownCache.get(attackerPlayer.getUniqueId()))) return;
        // Check if the target is already tagged (apply the buff in case)
        if (TimerInfo.isValid(taggedCache.get(damagedEntity.getUniqueId())))
          attackerPlayer.addPotionEffect(new PotionEffect(buffType, 20 * taggedTime, buffAmplifier));
        // Tag the entity (it's a refresh if it was already tagged)
        taggedCache.put(damagedEntity.getUniqueId(), new TimerInfo(taggedTime));
        damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * taggedTime, 0));
      }

      /*
       * Event fired when an arrow hits a weak entity.
       * This event is checked before the EntityDamageByEntityEvent (EventPriority.HIGH)
       */
      @EventHandler (priority = EventPriority.HIGH)
      public void onWeakBeingHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        // The damaged entity must be a living entity
        if (!(event.getEntity() instanceof LivingEntity damagedEntity)) return;
        // Multiply the damage for tagged entities
        if (TimerInfo.isValid(taggedCache.get(damagedEntity.getUniqueId()))) {
          double damage = event.getFinalDamage();
          damagedEntity.damage(damageMultiplier * damage);
          plugin.getLogger().info("A weakened entity has been hit for " + damage + " base damages + " +
                                  damage * damageMultiplier + " bonus damages");
        } else
          taggedCache.remove(damagedEntity.getUniqueId());
      }

      /*
       * On entity death, remove the debuff from the entity.
       */
      @EventHandler (priority = EventPriority.HIGH)
      public void onWeakDeath(EntityDeathEvent event) {
        if (event.isCancelled()) return;
        taggedCache.remove(event.getEntity().getUniqueId());
      }

    }, this.plugin);
  }

}

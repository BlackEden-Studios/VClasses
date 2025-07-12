package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.corex.caches.SmartCache;
import com.bestudios.corex.basics.TimerInfo;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 * The archer class is a ranged class that can weaken targets with arrows,
 * applying a glowing effect and a speed buff to the attacker.
 * When hitting a weakened target, the archer deals bonus damage.
 * </p>
 * <p>
 * This class is a Singleton, meaning only one instance of it can exist at any time.
 * </p>
 */
public class ArcherClass extends RoleClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static RoleClassType instance = new ArcherClass();

    /**
     * Private constructor for the Singleton
     */
    private ArcherClass() { super("archer.yml", RoleClassEnum.ARCHER); }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static RoleClassType getInstance() { return instance; }

    /**
     * Archer ability weakening duration in seconds
     */
    private int taggedTime = 10;
    /**
     * Setter for the tagged time, only if the value is not 0
     * @param value the time in seconds to set
     */
    private void setTaggedTime(int value) { if (value != 0) taggedTime = value; }

    /**
     * Archer ability bonus damage, to apply as a multiplier on the ordinary damage
     */
    private double damageMultiplier = 0.2;
    /**
     * Setter for the damage multiplier, only if the value is not 0
     * @param value the multiplier to set, as a percentage (e.g. 20 for 20%)
     */
    private void setDamageMultiplier(int value) { if (value != 0) damageMultiplier = (double) value /10 ; }

    /**
     * Amplifier for the potion effect given when hitting a previously weakened target
     */
    private int buffAmplifier = 2;
    /**
     * Setter for the buff amplifier, only if the value is not 0
     * @param value the amplifier to set, as a percentage (e.g. 2 for level 2)
     */
    private void setBuffAmplifier(int value) { if (value != 0) buffAmplifier = value-1; }
    
    /**
     * Smart cache for the entities that are weakened by the archer ability
     */
    private final SmartCache<TimerInfo> taggedByArcher = new SmartCache<>();

    /**
     * Implementation of the Archer class ability;
     * the event is checked last (EventPriority.HIGH);
     * not firing if canceled;
     * not firing if the damage source is not an arrow;
     * not firing if the attacker is not a player ;
     */
    @Override
    public void classAbility() {

        /* Listener for applying the tag to an entity */
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGHEST)
            public void onArrowHitByArcher(EntityDamageByEntityEvent event) {
                if (!(event.getDamager() instanceof Arrow arrow)) return;
                if (!(arrow.getShooter() instanceof Player attackerPlayer)) return;
                if (PlayersCache.getInstance().getPlayerCache(attackerPlayer.getUniqueId()).getCurrentClass() != RoleClassEnum.ARCHER) return;
                TimerInfo activationTimer = cooldownCache.get(attackerPlayer.getUniqueId());
                if (activationTimer != null) {
                    if (activationTimer.isValid()) return;
                    else cooldownCache.remove(attackerPlayer.getUniqueId());
                } if (event.isCancelled()) return;
                if (ArcherClass.getInstance().isDisabled()) return;
                if (!(event.getEntity() instanceof LivingEntity damagedEntity)) return;
                TimerInfo entry = taggedByArcher.get(damagedEntity.getUniqueId());
                if (entry != null && entry.isValid())
                    attackerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * taggedTime, buffAmplifier));
                taggedByArcher.put(damagedEntity.getUniqueId(), new TimerInfo(taggedTime));
                damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * taggedTime, 0));
            }

        }, ClassX.getInstance());

        /* Listener for dealing bonus damage to weakened entities */
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakBeingHit(EntityDamageByEntityEvent event) {
                if (event.isCancelled()) return;
                if (event.getEntity() instanceof LivingEntity damagedEntity) {
                    TimerInfo entry = taggedByArcher.get(damagedEntity.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        double finalDMG = event.getFinalDamage();
                        damagedEntity.damage(damageMultiplier * finalDMG);
                        ClassX.getInstance().toLog("A weakened entity has been hit for " + finalDMG + " base damages + " + finalDMG * damageMultiplier + " bonus damages", ClassX.getInstance().isDebugMode());
                    } else {
                        taggedByArcher.remove(damagedEntity.getUniqueId());
                    }
                }
            }
        }, ClassX.getInstance());

        /* Listener for removing the dead entities */
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakDeath(EntityDeathEvent event) {
                if (event.isCancelled()) return;
                LivingEntity deadEntity = event.getEntity();
                taggedByArcher.remove(deadEntity.getUniqueId());
            }
        }, ClassX.getInstance());

    }

    @Override
    public void setUpConfigs() {
        super.setUpConfigs();
        YamlConfiguration config = ClassX.getInstance().getSettingsManager().getConfig();

        setTaggedTime( config.getInt("archer.tag_time"));
        setDamageMultiplier( config.getInt("archer.damage_multiplier"));
        setBuffAmplifier( config.getInt("archer.buff_effect_level"));
    }
}

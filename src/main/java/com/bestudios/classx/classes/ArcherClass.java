package com.bestudios.classx.classes;

import com.bestudios.classx.Classes;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.PlayersCache;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.corex.utils.TimerInfo;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArcherClass extends ClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static ClassType instance = new ArcherClass();

    /**
     * Private constructor for the Singleton
     */
    private ArcherClass() { super("archer.yml", Classes.ARCHER); }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static ClassType getInstance() { return instance; }

    private int taggedTime = 10;
    private void setTaggedTime(int value) { if(value != 0) taggedTime = value; }

    private double damageMultiplier = 0.2;
    private void setDamageMultiplier(int value) { if(value != 0) damageMultiplier = (double) value /10 ; }

    private final Map<UUID, TimerInfo> taggedByArcher = new HashMap<>();

    private final Map<UUID, TimerInfo> classInitialCooldownCache = new HashMap<>();

    @Override
    public void setClassOnCooldown(Player player) {
        classInitialCooldownCache.put(player.getUniqueId(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown()));
    }

    /**
     * Implementation of the Archer class ability ;
     * the event is checked last (EventPriority.HIGH) ;
     * not firing if cancelled ;
     * not firing if the damage source is not an arrow ;
     * not firing if attacker is not a player ;
     */
    @Override
    public void classAbility() {

        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGHEST)
            public void onArrowHitByArcher(EntityDamageByEntityEvent event) {
                if (!(event.getDamager() instanceof Arrow arrow)) return;
                ClassX.getInstance().toLog("Damage Source is an Arrow", debug);
                if (!(arrow.getShooter() instanceof Player attackerPlayer)) return;
                ClassX.getInstance().toLog("Damager is a Player", debug);
                if (PlayersCache.getInstance().getCache().get(attackerPlayer.getUniqueId()).getCurrentClass() != Classes.ARCHER) return;
                ClassX.getInstance().toLog("Player is an archer", debug);
                TimerInfo activationTimer = classInitialCooldownCache.get(attackerPlayer.getUniqueId());
                if (activationTimer != null) {
                    if (activationTimer.isValid()) return;
                    else classInitialCooldownCache.remove(attackerPlayer.getUniqueId());
                    ClassX.getInstance().toLog("Archer is not on cooldown", debug);
                } if (event.isCancelled()) return;
                if (ArcherClass.getInstance().isDisabled()) return;
                if (!(event.getEntity() instanceof LivingEntity damagedEntity)) return;
                ClassX.getInstance().toLog("Archer has hit a living entity", debug);
                TimerInfo entry = taggedByArcher.get(damagedEntity.getUniqueId());
                if(entry != null && entry.isValid()) {
                    ClassX.getInstance().toLog("Entity was already weakened, setting the effect for the Player", debug);
                    attackerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * taggedTime, 2));
                }
                taggedByArcher.put(damagedEntity.getUniqueId(), new TimerInfo(taggedTime));
                damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * taggedTime, 0));
            }

        }, ClassX.getInstance());

        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakBeingHit(EntityDamageByEntityEvent event) {
                if (event.isCancelled()) return;
                if(event.getEntity() instanceof LivingEntity damagedEntity) {
                    TimerInfo entry = taggedByArcher.get(damagedEntity.getUniqueId());
                    if ((entry != null) && (entry.isValid())) {
                        double finalDMG = event.getFinalDamage();
                        damagedEntity.damage(damageMultiplier * finalDMG);
                        ClassX.getInstance().toLog("A weakened entity has been hit for " + finalDMG + " base damages + " + finalDMG * damageMultiplier + " bonus damages", debug);
                    } else {
                        taggedByArcher.remove(damagedEntity.getUniqueId());
                    }
                }
            }
        }, ClassX.getInstance());

        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakDeath(EntityDeathEvent event) {
                if (event.isCancelled()) return;
                if (event.getEntity() instanceof LivingEntity deadEntity) {
                    taggedByArcher.remove(deadEntity.getUniqueId());
                }
            }
        }, ClassX.getInstance());

    }

    @Override
    protected void classConfiguration() {
        YamlConfiguration config = ClassXSettingsManager.getInstance().getConfig();

        setTaggedTime( config.getInt("archer_class_tag_time"));
        setDamageMultiplier( config.getInt("archer_class_damage_multiplier"));
    }
}

package com.bestudios.classx.util;

import com.bestudios.corex.CoreX;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

/**
 * BardBuffRunnable is a BukkitRunnable that applies a potion effect to a player and all nearby players
 * who share at least one land with the bard player.
 * The buff is applied within a specified range.
 */
public class BardBuffRunnable extends BukkitRunnable {
    /** Reference to the Lands API integration, used to check land ownership */
    final LandsIntegration api = CoreX.getInstance().getDependencyManager().getLandsAPI();
    /** The potion effect to be applied as a buff */
    final PotionEffect effect;
    /** The range within which players can receive the buff */
    final double range;
    /** The player who dispenses the buff */
    final Player player;

    /**
     * Constructs a BardBuffRunnable with the specified player, potion effect, and buff range.
     * @param bardPlayer reference to the player that dispenses the buff to others
     * @param givenEffect reference to the potion effect to dispense
     * @param buffRange radius of the cube in which other players should be to be eligible to the buff
     */
    public BardBuffRunnable(Player bardPlayer, PotionEffect givenEffect, double buffRange)   {
        this.player = bardPlayer;
        this.effect = givenEffect;
        this.range = buffRange;
    }

    /**
     * The bard buff is eligible only for players who share at least a land with the bard player who applies it.
     * Since the Lands plugin permits to be part of multiple lands, all lands of both players should be compared.
     * <p></p>
     * If the server permits to being part of only one land, the overhead is minimum
     * since the collections are actual singletons.
     */
    @Override
    public void run() {
        // If the player is null, not online, or the effect is null or range is not positive, do nothing
        if (player == null || !player.isOnline() || effect == null || range <= 0) return;
        // Apply the potion effect to the bard player
        player.addPotionEffect(effect);
        // Get the lands of the bard player
        Collection<? extends Land> bardPlayerLandCollection = api.getLandPlayer(player.getUniqueId()).getLands();
        // Iterate through all nearby entities within the specified range
        for (Entity e : player.getNearbyEntities(range,range,range)) {
            if (e instanceof Player found) {
                // If the found entity is not the bard player, check their lands
                Collection<? extends Land> foundPlayerLandCollection = api.getLandPlayer(found.getUniqueId()).getLands();
                for (Land bardLand : bardPlayerLandCollection) {
                    for (Land foundPlayerLand : foundPlayerLandCollection) {
                        // If the bard player and the found player share the same land, apply the potion effect
                        if (bardLand.getOwnerUID() == foundPlayerLand.getOwnerUID())
                            found.addPotionEffect(effect);
                    }
                }
            }
        }
    }
}

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
 * Runnable for applying the bard buff
 */
public class BardBuffRunnable extends BukkitRunnable {

    final LandsIntegration api = CoreX.getInstance().getDependencyManager().getLandsAPI();
    final PotionEffect effect;
    final double range;
    final Player player;

    /**
     * Sole constructor
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
     * The bard buff is eligible only for players who share at least a land with the bard player who applies it
     * -
     * Since the Lands plugin permits to being part of multiple lands, all lands of both players should be compared
     * -
     * If the server restricts to being part of only land, the overhead is minimum
     * since the collections are singleton actually
     */
    @Override
    public void run() {
        player.addPotionEffect(effect);
        Collection<? extends Land> bardPlayerLandCollection = api.getLandPlayer(player.getUniqueId()).getLands();
        for (Entity e : player.getNearbyEntities(range,range,range)) {
            if (e instanceof Player found) {
                Collection<? extends Land> foundPlayerLandCollection = api.getLandPlayer(found.getUniqueId()).getLands();
                for (Land bardLand : bardPlayerLandCollection) {
                    for (Land foundPlayerLand : foundPlayerLandCollection) {
                        if (bardLand.getOwnerUID() == foundPlayerLand.getOwnerUID())
                            found.addPotionEffect(effect);
                    }
                }
            }
        }
    }
}

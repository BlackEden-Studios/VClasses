package com.bestudios.classx.util;

import com.bestudios.corex.CoreX;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BardBuffRunnable extends BukkitRunnable {

    LandsIntegration api = CoreX.getInstance().getDependencyManager().getLandsAPI();
    PotionEffect effect;
    double range;
    Player player;

    public BardBuffRunnable(Player bardPlayer, PotionEffect givenEffect, double buffRange)   {
        this.player = bardPlayer;
        this.effect = givenEffect;
        this.range = buffRange;
    }
    @Override
    public void run() {
        player.addPotionEffect(effect);
        for (Entity e : player.getNearbyEntities(range,range,range)) {
            if (e instanceof Player found ) {
                Collection<? extends Land> bardPlayerLandCollection = api.getLandPlayer(player.getUniqueId()).getLands();
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

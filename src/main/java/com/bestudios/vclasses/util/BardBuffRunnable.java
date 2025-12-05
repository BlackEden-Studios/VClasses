package com.bestudios.vclasses.util;

import com.bestudios.fulcrum.api.service.team.TeamsService;
import com.bestudios.vclasses.VClasses;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Runnable for applying the bard buff
 */
public class BardBuffRunnable extends BukkitRunnable {

  /** Reference to the team service */
  final TeamsService api = Objects.requireNonNull(VClasses.getInstance().getServer()
                                                                        .getServicesManager()
                                                                        .getRegistration(TeamsService.class))
                                                                        .getProvider();
  /** Reference to the potion effect to be applied */
  final PotionEffect effect;
  /** Radius of the cube in which other players should be to be eligible to the buff */
  final double range;
  /** Reference to the player that dispenses the buff to others */
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
    for (Entity e : player.getNearbyEntities(range,range,range))
      if (e instanceof Player found)
        if (api.areAllies(player.getUniqueId(), found.getUniqueId()))
          found.addPotionEffect(effect);


  }
}

package com.bestudios.classx.util;

import com.bestudios.classx.ArmorListener;
import com.bestudios.classx.Classes;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.ClassXSettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class DefaultClassActivationRunnable extends BukkitRunnable {

    Player player;
    Classes playerClass;
    int cooldownTimer;

    public DefaultClassActivationRunnable(Player givenPlayer, Classes recentlyChangedPlayerClass) {
        this.player = givenPlayer;
        playerClass = recentlyChangedPlayerClass;
        cooldownTimer = 0;
    }

    @Override
    public void run() {
        cooldownTimer++;
        if(((cooldownTimer%(ClassXSettingsManager.getInstance().getClassActivationCooldown())) == 0) && (!this.isCancelled())) {
            ClassX.getInstance().toLog("Scheduled timer has expired, executing the method", ClassX.getInstance().isDebugMode());

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (PotionEffect potionEffect : ClassX.implementedClasses.get(playerClass).classBuffPrivateCache) {
                        ClassX.getInstance().toLog("Adding effect " + potionEffect.toString() + " to player " + player.getName(), ClassX.getInstance().isDebugMode());
                        player.addPotionEffect(potionEffect);
                    }

                    player.sendMessage(Component.text("Le caratteristiche di classe sono ora attive!")
                            .color(TextColor.color(0x008000))
                            .decoration(TextDecoration.BOLD, true)
                    );

                    ClassX.getInstance().getPluginManager().callEvent(new TaskCancellationEvent(player, ArmorListener.getInstance()));
                }
            };
            task.runTask(ClassX.getInstance());

        }
    }
}

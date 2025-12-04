package com.bestudios.vclasses.data;

import com.bestudios.vclasses.VClasses;
import com.bestudios.corex.caches.SmartCache;
import com.bestudios.vclasses.classes.RoleClassEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;


public class ClassActivationQueue extends BukkitRunnable{

    private final boolean debug = VClasses.getInstance().isDebugMode();


    /* Singleton implementation */
    private final static ClassActivationQueue instance = new ClassActivationQueue();
    /* Private Constructor */
    private ClassActivationQueue() {}
    public static ClassActivationQueue getInstance() { return instance; }


    private final int max_operations = VClasses.getInstance().getConfig().getInt("max_activation_queue_operations",5);

    private final ArrayDeque<ClassActivationEntry> queue = new ArrayDeque<>(VClasses.MAX_PLAYERS);
    private final SmartCache<Integer> interruptedActivations = new SmartCache<>(VClasses.MAX_PLAYERS);



    @Override
    public void run() {

        int index = 0;
        while (!queue.isEmpty() && !queue.getFirst().isValid() && index < max_operations) {
            ClassActivationEntry entry = queue.pop();
            Player player = entry.getPlayer();
            if (!player.isOnline()) break;
            Integer concurrentActivations = interruptedActivations.get(player.getUniqueId());
            if (concurrentActivations != null && concurrentActivations != 0) {
                interruptedActivations.put(player.getUniqueId(),concurrentActivations-1);
                break;
            }
            RoleClassEnum roleClass = entry.getRoleClass();
            for (PotionEffect potionEffect : VClasses.implementedClasses.get(roleClass).classPotionEffectsPrivateCache) {
                player.addPotionEffect(potionEffect);
            }

            player.sendMessage(Component.text(VClasses.LANGUAGES.getMessage("class_activation_success"))
                                        .color(TextColor.color(0x008000))
                                        .decoration(TextDecoration.BOLD, true)
            );

            interruptedActivations.remove(player.getUniqueId());
            index++;
        }
    }

    public void getInQueue(ClassActivationEntry entry) {
        Integer concurrentActivations = interruptedActivations.get(entry.getPlayer().getUniqueId());
        if (concurrentActivations != null && concurrentActivations != 0)
            concurrentActivations += 1;
        else concurrentActivations = 0;
        interruptedActivations.put(entry.getPlayer().getUniqueId(),concurrentActivations);
        if (entry.getRoleClass() != RoleClassEnum.NONE) queue.push(entry);
    }


}



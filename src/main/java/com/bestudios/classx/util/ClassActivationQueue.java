package com.bestudios.classx.util;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.corex.caches.SmartCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;


/**
 * ClassActivationQueue is a singleton class that manages the queue of class activations in ClassX.
 * It processes the queue periodically to apply class effects to players.
 */
public class ClassActivationQueue extends BukkitRunnable{
    /** Singleton implementation */
    private final static ClassActivationQueue instance = new ClassActivationQueue();
    /** Private Constructor */
    private ClassActivationQueue() {}
    /**
     * Gets the singleton instance of ClassActivationQueue.
     * This method ensures that only one instance of the queue exists throughout the application.
     *
     * @return The singleton instance of ClassActivationQueue
     */
    public static ClassActivationQueue getInstance() { return instance; }
    /**
     * The maximum number of operations to perform in each run of the queue.
     * This value is configurable through the ClassX configuration.
     */
    private final int max_operations = ClassX.getInstance().getConfig().getInt("max_activation_queue_operations",5);
    /**
     * The queue that holds class activation entries.
     * It uses an ArrayDeque for efficient FIFO operations.
     */
    private final ArrayDeque<ClassActivationEntry> queue = new ArrayDeque<>(ClassX.MAX_PLAYERS);
    /**
     * A cache that tracks the number of concurrent activations for each player.
     * This is used to prevent multiple activations from the same player at the same time.
     */
    private final SmartCache<Integer> interruptedActivationsCache = new SmartCache<>(ClassX.MAX_PLAYERS);

    /**
     * Starts the ClassActivationQueue task.
     * This method schedules the queue to run periodically, processing class activations.
     */
    @Override
    public void run() {
        /*
         * This method processes the class activation queue.
         * It iterates through the queue and applies class effects to players.
         * It stops processing when it reaches the maximum number of operations
         * or when it encounters an entry which timer has not expired yet,
         * meaning all the next class activations are equal or further in time.
         */
        int index = 0;
        while (!queue.isEmpty() && !queue.getFirst().isValid() && index < max_operations) {
            // Pop the first entry
            ClassActivationEntry entry = queue.pop();
            Player player = entry.getPlayer();
            // If the player is not online, we skip this activation
            if (!player.isOnline()) break;
            Integer concurrentActivations = interruptedActivationsCache.get(player.getUniqueId());
            // If the player has concurrent activations, we skip this activation
            if (concurrentActivations != null && concurrentActivations != 0) {
                interruptedActivationsCache.put(player.getUniqueId(),concurrentActivations-1);
                break;
            }
            // Apply the class effects to the player
            RoleClassEnum roleClass = entry.getRoleClass();
            for (PotionEffect potionEffect : ClassX.implementedClasses.get(roleClass).classPotionEffectsPrivateCache) {
                player.addPotionEffect(potionEffect);
            }
            // Send a message to the player indicating the class activation was successful
            player.sendMessage(Component.text(ClassX.getInstance().getLanguageManager().getMessage("class_activation_success"))
                                        .color(TextColor.color(0x008000))
                                        .decoration(TextDecoration.BOLD, true)
            );
            // Remove the entry from the queue and the interrupted activations cache
            interruptedActivationsCache.remove(player.getUniqueId());
            index++;
        }
    }

    /**
     * Adds a ClassActivationEntry to the queue.
     * This method is called when a player activates a class.
     * It increments the concurrent activations for the player and adds the entry to the queue.
     *
     * @param entry The ClassActivationEntry to add to the queue
     */
    public void getInQueue(ClassActivationEntry entry) {
        // If the role class is NONE, we do not add the entry to the queue
        if (entry.getRoleClass() == RoleClassEnum.NONE) return;
        // Get the current concurrent activations for the player
        int concurrentActivations = interruptedActivationsCache.containsEntry(entry.getPlayer().getUniqueId()) ?
                interruptedActivationsCache.get(entry.getPlayer().getUniqueId()) : -1;
        // Update the cache incrementing concurrent activations count
        interruptedActivationsCache.put(entry.getPlayer().getUniqueId(), concurrentActivations+1);
        // Add the entry to the queue
        queue.push(entry);
    }


}



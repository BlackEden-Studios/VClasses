package com.bestudios.vclasses.data;

import com.bestudios.fulcrum.api.cache.SmartCache;
import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.vclasses.util.DummySaver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

/**
 * ClassActivationQueue
 * Manages the queue for class activations
 *
 * @version 0.12.0
 */
public class ClassActivationQueue extends BukkitRunnable{

  /** Class Activation Entry */
  public record ClassActivationEntry(
          @NotNull Player player,
          @NotNull RoleClassEnum roleClass,
          @NotNull TimerInfo timer
  ) {}

  /** Plugin instance */
  private final VClasses plugin;
  /** Maximum number of operations to perform in the queue */
  private final int maxOperations;
  /** Queue of Class Activation Entries */
  private final ArrayDeque<ClassActivationEntry> queue;
  /** Cache of interrupted activations */
  private final SmartCache<Integer> interruptedActivations;
  /** Singleton instance */
  private static final ClassActivationQueue instance = new ClassActivationQueue(VClasses.getInstance());

  /** Private Constructor */
  private ClassActivationQueue(VClasses plugin) {
    this.plugin                 = plugin;
    this.maxOperations          = plugin.getConfig().getInt("max_activation_queue_operations",5);
    this.queue                  = new ArrayDeque<>(plugin.getServer().getMaxPlayers()+10);
    this.interruptedActivations = new SmartCache<>(plugin, new DummySaver<>());
  }

  public static ClassActivationQueue getInstance() {
    return instance;
  }

  @Override
  public void run() {
    int processed = 0;

    // Process until the queue is empty or operation limit reached
    while (!queue.isEmpty() && processed < maxOperations) {

      // 1. Check HEAD without removing.
      // If the timer is valid (still running), we must wait. Stop processing entirely.
      if (queue.peek().timer().isValid()) return;

      // 2. Retrieve and remove the entry
      ClassActivationEntry entry = queue.poll();

      // Safety check in case of concurrent modification or nulls
      if (entry == null) continue;

      Player player = entry.player();

      // 3. Check if the player is online
      // We use 'continue' to skip this specific entry but keep processing others
      if (!player.isOnline()) continue;

      // 4. Handle Interrupted Activations
      // We use getOrDefault to avoid null checks
      int concurrentActivations = interruptedActivations.getOrDefault(player.getUniqueId(), 0);

      if (concurrentActivations > 0) {
        // Consume one interruption "credit" and skip activation
        interruptedActivations.put(player.getUniqueId(), concurrentActivations - 1);
        continue;
      }

      // 5. Activate
      RoleClassEnum roleClass = entry.roleClass();
      VClasses.implementedClasses.get(roleClass).activateRoleClass(player);

      player.sendMessage(Component.text(plugin.getLanguageConfiguration().getString("class.activation.success", ""))
                                  .color(TextColor.color(0x008000))
                                  .decoration(TextDecoration.BOLD, true)
      );

      // Clean up cache
      interruptedActivations.remove(player.getUniqueId());
      processed++;
    }
  }

  /**
   * Adds an entry to the queue
   * Logic adjusted to avoid local variable reassignment
   * @param entry Class Activation Entry
   */
  public void getInQueue(ClassActivationEntry entry) {
    if (entry.roleClass == RoleClassEnum.NONE) return;

    // 1. Calculate a new interruption value
    int current = interruptedActivations.getOrDefault(entry.player.getUniqueId(), -1);

    interruptedActivations.put(entry.player.getUniqueId(), current + 1);

    // 2. Add to the END of the queue (FIFO), ensuring fair processing order.
    queue.offer(entry);
  }
}



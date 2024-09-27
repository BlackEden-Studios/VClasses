package com.bestudios.classx;

import com.bestudios.classx.util.ClassChangedException;
import com.bestudios.classx.util.DefaultClassActivationRunnable;
import com.bestudios.classx.util.TaskCancellationEvent;
import com.bestudios.corex.CoreX;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ArmorListener implements Listener {

    private final boolean debug;
    private final static ArmorListener instance = new ArmorListener();
    /* Private Constructor */
    private ArmorListener() {
        debug = ClassX.getInstance().isDebugMode();
    }
    public static ArmorListener getInstance() { return instance; }

    private final Map<String, Classes> classesUnifiedCache = new HashMap<>() {{
        put("minecraft:air", Classes.NONE);
    }};
    public void loadUnifiedCache(Map<String, Classes> append) {
        classesUnifiedCache.putAll(append);
    }

    private final Map<UUID, DefaultClassActivationRunnable> activationTaskMap = new HashMap<>(ClassX.PREDICTED_MAX_PLAYERS);

    private void cancelTask(Player player) {
        try {
            activationTaskMap.get(player.getUniqueId()).cancel();
            activationTaskMap.remove(player.getUniqueId());
        } catch (Exception ignored) { }
    }

    private String itemToString(ItemStack item) {
        if (ItemsAdder.isCustomItem(item))
            return ItemsAdder.getCustomItemName(item);
        else {
            return item.getType().getKey().toString();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        if (CoreX.getInstance().getDependencyManager().isItemsAdderLoaded()) return;
        ClassX.getInstance().toLog("ItemsAdder is being loaded for the first time, registering the Armor Listener", debug);
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onArmorChange(PlayerArmorChangeEvent event) {
                Player player = event.getPlayer();
                if (ItemsAdder.areItemsLoaded()) {
                    try {
                        ClassX.getInstance().toLog("Starting updating the Armor Cache for player " + player.getName(), debug);
                        Classes newItem = classesUnifiedCache.get(itemToString(event.getNewItem()));
                        ClassX.getInstance().toLog("New item is class " + newItem , debug);
                        Classes oldItem = classesUnifiedCache.get(itemToString(event.getOldItem()));
                        ClassX.getInstance().toLog("Old item is class " + oldItem , debug);
                        if (!Objects.equals(newItem.toString(), oldItem.toString()))
                            PlayersCache.getInstance().getCache().get(player.getUniqueId()).updateCache( newItem, oldItem );
                    } catch (ClassChangedException e) {
                        ClassX.getInstance().toLog("Catched a ClassChangedException for player " + player.getName() + " changing to class " + e.getNewClass().toString(), debug);
                        //If the class is changed, clear all potion buffs
                        for(PotionEffect effect : player.getActivePotionEffects()) {
                            ClassX.getInstance().toLog("Removing potion effect " + effect.toString() + " for player " + player.toString(), debug);
                            player.removePotionEffect( effect.getType() );
                        } cancelTask(player);
                        ClassX.implementedClasses.get(e.getPreviousClass()).classDismissed(player);
                        //If the class is defined, add the respective buffs to the player
                        if(e.getNewClass() != Classes.NONE) {
                            ClassX.getInstance().toLog("Detected a change to a specific class", debug);
                            ClassX.implementedClasses.get(e.getNewClass()).setClassOnCooldown(player);
                            ClassX.getInstance().toLog("Class ability has been put on cooldown", debug);
                            DefaultClassActivationRunnable previousTask = activationTaskMap.get(player.getUniqueId());
                            if (previousTask != null) previousTask.cancel();
                            DefaultClassActivationRunnable task = new DefaultClassActivationRunnable(player, e.getNewClass());
                            task.runTaskTimerAsynchronously(ClassX.getInstance(),1,20);
                            activationTaskMap.put(player.getUniqueId(),task);
                            ClassX.getInstance().toLog("The class activation runnable timer has been scheduled", debug);
                        }
                    }
                } // else event.setCancelled(true);
            }

            @EventHandler(priority = EventPriority.HIGH)
            public void onTaskCancellation(TaskCancellationEvent event) {
                if (event.getReceiver() == ArmorListener.getInstance()) {
                    DefaultClassActivationRunnable task = activationTaskMap.get(event.getPlayer().getUniqueId());
                    if (task != null) task.cancel();
                    activationTaskMap.remove(event.getPlayer().getUniqueId());
                }
            }

        }, ClassX.getInstance());
    }

}



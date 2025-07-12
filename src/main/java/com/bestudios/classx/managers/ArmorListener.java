package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.classx.util.ClassActivationEntry;
import com.bestudios.classx.util.ClassActivationQueue;
import com.bestudios.classx.util.ClassChangeMessage;
import com.bestudios.corex.services.HooksManager;
import com.bestudios.corex.basics.BEPlugin;
import com.bestudios.corex.basics.TimerInfo;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * ArmorListener updates the player's role class based on the armor they equip and manages class activation queues.
 */
public class ArmorListener implements Listener {
    /** Singleton instance of ArmorListener */
    private final static ArmorListener instance = new ArmorListener();
    /** Private Constructor */
    private ArmorListener() {
        plugin = ClassX.getInstance();
    }
    /**
     * Gets the singleton instance of ArmorListener.
     * @return the instance of ArmorListener
     */
    public static ArmorListener getInstance() { return instance; }
    /** Reference to the ClassX plugin instance */
    private final BEPlugin plugin;
    /**
     * A cache that maps item names to their corresponding role classes.
     * This cache is used to quickly determine the role class of an item when a player equips it.
     * The key is the item name, and the value is the RoleClassEnum representing the role class.
     */
    private final Map<String, RoleClassEnum> equipmentUnifiedCache = new HashMap<>() {{
        put("minecraft:air", RoleClassEnum.NONE);
    }};

    /**
     * Adds a mapping of item names to their corresponding role classes to the unified cache.
     * @param append a map containing item names as keys and their corresponding RoleClassEnum as values
     */
    public void addToUnifiedCache(Map<String, RoleClassEnum> append) {
        equipmentUnifiedCache.putAll(append);
    }

    /**
     * Handles the ItemsAdderLoadDataEvent to register the Armor Listener.
     * @param event the ItemsAdderLoadDataEvent that is triggered when ItemsAdder is loaded
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        // Check if the event has been fired after an IA reload
        if (event.getCause().equals(ItemsAdderLoadDataEvent.Cause.RELOAD)) return;
        plugin.toLog("ItemsAdder is being loaded for the first time, registering the Armor Listener", plugin.isDebugMode());
        // Register the listener that handles the Player Armor Change
        plugin.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onArmorChange(PlayerArmorChangeEvent event) {
                // References captured for performances and readability
                Player player = event.getPlayer();
                RoleClassEnum newRole = equipmentUnifiedCache.getOrDefault(HooksManager.getItemName(event.getNewItem()), RoleClassEnum.NONE);
                RoleClassEnum oldRole = equipmentUnifiedCache.getOrDefault(HooksManager.getItemName(event.getOldItem()), RoleClassEnum.NONE);
                // If the items are representatives of the same role-class, do nothing
                if (Objects.equals(newRole.toString(), oldRole.toString())) return;
                ClassChangeMessage changeMessage = PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).updateCache(newRole, oldRole);
                // If the cache was not updated, do nothing
                if (changeMessage == null) return;
                // Clear all potion effects
                for(PotionEffect effect : ClassX.implementedClasses.get(changeMessage.getFormerClassType()).classPotionEffectsPrivateCache)
                    player.removePotionEffect(effect.getType());
                // Dismiss the former role-class for the player
                ClassX.implementedClasses.get(changeMessage.getFormerClassType()).dismissRoleClass(player);
                // Set the ability class on cooldown
                ClassX.implementedClasses.get(changeMessage.getNewClassType()).setClassOnCooldown(player);
                // Queue the activation
                ClassActivationQueue.getInstance().getInQueue(new ClassActivationEntry(
                        player, changeMessage.getNewClassType(), new TimerInfo(ClassX.getInstance().getConfig().getInt("class_activation_cooldown", 3))
                ));
                plugin.toLog("A class activation has been queued for " + player.getName(), plugin.isDebugMode());
            }
        }, plugin);
    }

}



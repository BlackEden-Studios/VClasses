package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.classx.util.ClassActivationEntry;
import com.bestudios.classx.util.ClassActivationQueue;
import com.bestudios.classx.util.ClassChangedException;
import com.bestudios.corex.managers.HooksManager;
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

public class ArmorListener implements Listener {
    private final static ArmorListener instance = new ArmorListener();
    /* Private Constructor */
    private ArmorListener() {
        debug = ClassX.getInstance().isDebugMode();
    }
    public static ArmorListener getInstance() { return instance; }

    private final boolean debug;

    private final Map<String, RoleClassEnum> equipmentUnifiedCache = new HashMap<>() {{
        put("minecraft:air", RoleClassEnum.NONE);
    }};
    public void loadUnifiedCache(Map<String, RoleClassEnum> append) {
        equipmentUnifiedCache.putAll(append);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        // Check if the event has been fired after an IA reload
        if (event.getCause().equals(ItemsAdderLoadDataEvent.Cause.RELOAD)) return;
        ClassX.getInstance().toLog("ItemsAdder is being loaded for the first time, registering the Armor Listener", debug);
        // Register the listener that handles the Player Armor Change
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onArmorChange(PlayerArmorChangeEvent event) {
                // References captured for performances and readability
                Player player = event.getPlayer();
                BEPlugin pluginRef = ClassX.getInstance();
                RoleClassEnum newRole = equipmentUnifiedCache.get(HooksManager.getItemName(event.getNewItem()));
                RoleClassEnum oldRole = equipmentUnifiedCache.get(HooksManager.getItemName(event.getOldItem()));
                // If the items are not mapped, the role class is set to None
                if (newRole == null) newRole = RoleClassEnum.NONE;
                if (oldRole == null) oldRole = RoleClassEnum.NONE;
                try { // If the items are representatives of different role-classes, update the cache
                    if (!Objects.equals(newRole.toString(), oldRole.toString())) {
                        pluginRef.toLog("Cache updated for " + player.getName() + " : " + oldRole + " -> " + newRole, debug);
                        PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).updateCache(newRole, oldRole);
                    }
                } catch (ClassChangedException e) {

                    // Logic to execute when a role-class change exception is caught
                    pluginRef.toLog("Catched a ClassChangedException for player " + player.getName() + " changing to role-class " + e.getNewClassType().toString(), debug);
                    // Clear all potion effects
                    for(PotionEffect effect : ClassX.implementedClasses.get(e.getFormerClassType()).classPotionEffectsPrivateCache) player.removePotionEffect(effect.getType());
                    // Dismiss the former role-class for the player
                    ClassX.implementedClasses.get(e.getFormerClassType()).dismissRoleClass(player);
                    // Set the ability class on cooldown
                    ClassX.implementedClasses.get(e.getNewClassType()).setClassOnCooldown(player);
                    // Queue the activation
                    ClassActivationQueue.getInstance().getInQueue(new ClassActivationEntry(
                            player, e.getNewClassType(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown())
                    ));
                    pluginRef.toLog("A class activation has been queued", debug);
                }
            }

        }, ClassX.getInstance());
    }

}



package com.bestudios.vclasses.managers;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.classes.RoleClassType;
import com.bestudios.vclasses.data.PlayersCache;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.vclasses.data.ClassActivationEntry;
import com.bestudios.vclasses.data.ClassActivationQueue;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armor Listener
 */
public class ArmorListener implements Listener {

  /** Singleton instance */
  private final static ArmorListener instance = new ArmorListener();


  /** Equipment cache */
  private final Map<String, RoleClassEnum> equipmentUnifiedCache;
  /** Custom items service */
  private final CustomItemsService customItems;
  /** Reference to the plugin instance */
  private final FulcrumPlugin plugin;

  /* Private Constructor */
  private ArmorListener() {
    this.plugin = VClasses.getInstance();
    this.equipmentUnifiedCache = new ConcurrentHashMap<>() {{
      put("minecraft:air", RoleClassEnum.NONE);
    }};
    this.customItems = Objects.requireNonNull(VClasses.getInstance()
                                                      .getServer()
                                                      .getServicesManager()
                                                      .getRegistration(CustomItemsService.class))
                                                      .getProvider();
  }

  /** Returns the singleton instance */
  public static ArmorListener getInstance() {
    return instance;
  }

  /**
   * Loads the unified cache
   * @param append The map to append to the unified cache
   */
  public void loadUnifiedCache(Map<String, RoleClassEnum> append) {
    equipmentUnifiedCache.putAll(append);
  }

  public RoleClassEnum getRoleFromItem(String itemName) {
    if ( equipmentUnifiedCache.get(itemName) == null) return RoleClassEnum.NONE;
    return equipmentUnifiedCache.get(itemName);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onArmorChange(PlayerArmorChangeEvent event) {
    // References captured for performances and readability
    Player player = event.getPlayer();
    RoleClassEnum newRole = getRoleFromItem(customItems.getItemName(event.getNewItem()));
    RoleClassEnum oldRole = getRoleFromItem(customItems.getItemName(event.getOldItem()));
    // If the items are representatives of the same role-classes, return
    if (Objects.equals(newRole.toString(), oldRole.toString())) return;

    plugin.getLogger().config("Cache updated for " + player.getName() + " : " + oldRole + " -> " + newRole);
    if (!PlayersCache.getPlayerCache(player.getUniqueId()).updateCache(newRole, oldRole)) return;

    // Logic to execute when a role-class change is caught
    plugin.getLogger().config("Class for player " + player.getName() + " is changing to role-class " + newRole);
    // Dismiss the old role-class
    VClasses.implementedClasses.get(oldRole).dismissRoleClass(player);
    // Set the ability class on cooldown
    VClasses.implementedClasses.get(newRole).setCooldownForPlayer(player);
    // Queue activation
    ClassActivationQueue.getInstance().getInQueue(new ClassActivationEntry(
            player, newRole, new TimerInfo(RoleClassType.cooldownTimer))
    );
    plugin.getLogger().config("A class activation has been queued");
  }
}


package com.bestudios.vclasses.data;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.vclasses.classes.RoleClassType;

import java.util.HashMap;
import java.util.Map;

public class EquipmentCache {

  /** Plugin instance */
  private final FulcrumPlugin plugin;
  /** The current role class of the player */
  private RoleClassEnum currentClass;
  /** The cache of the player's equipment */
  private final Map<RoleClassEnum, Short> cache;

  /**
   *  Wrapper class that holds the information about player equipment in relation to the role classes
   */
  public EquipmentCache() {
    this.plugin = VClasses.getInstance();
    this.currentClass = RoleClassEnum.NONE;
    this.cache = createCache();

    plugin.getLogger().config("Created a brand new Equipment Cache");
  }

  /**
   * Cache generator
   * @return a map of role classes and their respective number of items equipped
   */
  private Map<RoleClassEnum, Short> createCache() {
    Map<RoleClassEnum, Short> myMap = new HashMap<>();
    for ( Map.Entry<RoleClassEnum, RoleClassType> classEntry : VClasses.implementedClasses.entrySet()) {
      if ( classEntry.getKey().equals(RoleClassEnum.NONE))
        myMap.put(classEntry.getKey(),(short) 4);
      else myMap.put(classEntry.getKey(),(short) 0);
    }
    return myMap;
  }

    /**
     * Cache updater, called when the changed items' role classes have been already deduced
     * @param newClass the new role class of the items
     * @param oldClass the old role class of the items
     *
     * @return the current role class of the player
     */
    public RoleClassEnum updateCache(RoleClassEnum newClass , RoleClassEnum oldClass) {
      cache.put(newClass, (short) (cache.get(newClass) + 1));
      cache.put(oldClass, (short) (cache.get(oldClass) - 1));
      if(cache.get(newClass) == 4)
        currentClass = newClass;
      else
        currentClass = RoleClassEnum.NONE;
      return currentClass;
    }

    public RoleClassEnum getCurrentClass() {
      return currentClass;
    }
}

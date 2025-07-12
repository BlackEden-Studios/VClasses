package com.bestudios.classx.caches;

import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.classes.RoleClassType;
import com.bestudios.classx.util.ClassChangeMessage;

import java.util.HashMap;
import java.util.Map;


/**
 * EquipmentCache is a class that manages the equipment cache for players in the ClassX plugin.
 * It tracks the current role class of a player
 * and updates the cache when a player changes their role class by equipping new armor.
 */
public class EquipmentCache {

    /** The current role class of the player. */
    private RoleClassEnum currentClass;
    /** A map that holds the count of items for each role class. */
    private final Map<RoleClassEnum, Short> cache;

    /**
     *  Wrapper class that holds the information about player equipment in relation of the role classes
     */
    public EquipmentCache() {
        this.currentClass = RoleClassEnum.NONE;
        this.cache = createCache();
        ClassX.getInstance().toLog("Created a brand new Equipment Cache", ClassX.getInstance().isDebugMode());
    }

    /**
     *  Cache generator
     */
    private Map<RoleClassEnum, Short> createCache() {
        Map<RoleClassEnum, Short> myMap = new HashMap<>();
        for ( Map.Entry<RoleClassEnum, RoleClassType> classEntry : ClassX.implementedClasses.entrySet()) {
            if ( classEntry.getKey().equals(RoleClassEnum.NONE)) myMap.put(classEntry.getKey(),(short) 4);
            else myMap.put(classEntry.getKey(),(short) 0);
        }
        return myMap;
    }

    /**
     * Updates the cache when a player changes their role class by equipping new armor.
     * @param newItem the new role class the player is equipping
     * @param oldItem the old role class the player was equipping
     * @return ClassChangeMessage containing the new and old role classes
     */
    public ClassChangeMessage updateCache(RoleClassEnum newItem , RoleClassEnum oldItem) {
        RoleClassEnum oldClass = currentClass;
        cache.put(newItem, (short) (cache.get(newItem) + 1));
        cache.put(oldItem, (short) (cache.get(oldItem) - 1));
        for (RoleClassEnum c : RoleClassEnum.values()) {
            if(cache.get(c) == 4) {
                currentClass = c;
                return new ClassChangeMessage(currentClass, oldClass);
            }
        }
        if (currentClass != RoleClassEnum.NONE) {
            currentClass = RoleClassEnum.NONE;
            return new ClassChangeMessage(currentClass, oldClass);
        }
        return null;
    }

    /**
     * Gets the current role class of the player.
     * @return the current role class of the player
     */
    public RoleClassEnum getCurrentClass() {
        return currentClass;
    }

}

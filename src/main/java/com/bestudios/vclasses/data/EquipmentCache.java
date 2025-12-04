package com.bestudios.vclasses.data;

import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.vclasses.classes.RoleClassType;

import java.util.HashMap;
import java.util.Map;

public class EquipmentCache {

    private RoleClassEnum currentClass;
    private final Map<RoleClassEnum, Short> cache;
    private final boolean debug;

    /**
     *  Wrapper class that holds the information about a player equipment in relation of the role classes
     */
    public EquipmentCache() {
        this.currentClass = RoleClassEnum.NONE;
        this.cache = createCache();
        debug = VClasses.getInstance().isDebugMode();
        VClasses.getInstance().toLog("Created a brand new Equipment Cache", debug);
    }

    /**
     *  Cache generator
     */
    private Map<RoleClassEnum, Short> createCache() {
        Map<RoleClassEnum, Short> myMap = new HashMap<>();
        for ( Map.Entry<RoleClassEnum, RoleClassType> classEntry : VClasses.implementedClasses.entrySet()) {
            if ( classEntry.getKey().equals(RoleClassEnum.NONE)) myMap.put(classEntry.getKey(),(short) 4);
            else myMap.put(classEntry.getKey(),(short) 0);
        }
        return myMap;
    }

    /**
     *  Cache updater, called when the changed items' role classes have been already deduced
     */
    public boolean updateCache(RoleClassEnum newItem , RoleClassEnum oldItem) {
        RoleClassEnum oldClass = currentClass;
        VClasses.getInstance().toLog("Trying to update the cache", debug);
        cache.put(newItem, (short) (cache.get(newItem) + 1));
        VClasses.getInstance().toLog("Update the cache to add an item of class " + newItem , debug);
        cache.put(oldItem, (short) (cache.get(oldItem) - 1));
        VClasses.getInstance().toLog("Update the cache to remove an item of class " + oldItem, debug);
        for (RoleClassEnum c : RoleClassEnum.values()) {
            if(cache.get(c) == 4) {
                currentClass = c;
                VClasses.getInstance().toLog("Class changed to " + currentClass, debug);
                throw new ClassChangedException(currentClass, oldClass);
            }
        }
        if (currentClass != RoleClassEnum.NONE) {
            currentClass = RoleClassEnum.NONE;
            VClasses.getInstance().toLog("Class changed to " + currentClass, debug);
            throw new ClassChangedException(currentClass, oldClass);
        }
    }

    public RoleClassEnum getCurrentClass() {
        return currentClass;
    }

}

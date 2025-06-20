package com.bestudios.classx.caches;

import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.classes.RoleClassType;
import com.bestudios.classx.util.ClassChangedException;

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
        debug = ClassX.getInstance().isDebugMode();
        ClassX.getInstance().toLog("Created a brand new Equipment Cache", debug);
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
     *  Cache updater, called when the changed items' role classes have been already deduced
     */
    public void updateCache(RoleClassEnum newItem , RoleClassEnum oldItem) throws ClassChangedException {
        RoleClassEnum oldClass = currentClass;
        ClassX.getInstance().toLog("Trying to update the cache", debug);
        cache.put(newItem, (short) (cache.get(newItem) + 1));
        ClassX.getInstance().toLog("Update the cache to add an item of class " + newItem , debug);
        cache.put(oldItem, (short) (cache.get(oldItem) - 1));
        ClassX.getInstance().toLog("Update the cache to remove an item of class " + oldItem, debug);
        for (RoleClassEnum c : RoleClassEnum.values()) {
            if(cache.get(c) == 4) {
                currentClass = c;
                ClassX.getInstance().toLog("Class changed to " + currentClass, debug);
                throw new ClassChangedException(currentClass, oldClass);
            }
        }
        if (currentClass != RoleClassEnum.NONE) {
            currentClass = RoleClassEnum.NONE;
            ClassX.getInstance().toLog("Class changed to " + currentClass, debug);
            throw new ClassChangedException(currentClass, oldClass);
        }
    }

    public RoleClassEnum getCurrentClass() {
        return currentClass;
    }

}

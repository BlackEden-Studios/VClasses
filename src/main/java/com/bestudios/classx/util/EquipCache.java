package com.bestudios.classx.util;

import com.bestudios.classx.Classes;
import com.bestudios.classx.ClassX;

import java.util.HashMap;
import java.util.Map;

public class EquipCache {

    private Classes currentClass;
    private final Map<Classes, Short> cache;
    private final boolean debug;

    public EquipCache() {
        this.currentClass = Classes.NONE;
        this.cache = createCache();
        debug = ClassX.getInstance().isDebugMode();
        ClassX.getInstance().toLog("Created a brand new Cache", debug);
    }

    private Map<Classes, Short> createCache() {
        Map<Classes, Short> myMap = new HashMap<>();
        myMap.put(Classes.NONE, (short) 4);
        myMap.put(Classes.ARCHER, (short) 0);
        myMap.put(Classes.BARD, (short) 0);
        myMap.put(Classes.ROGUE, (short) 0);
        myMap.put(Classes.WARRIOR, (short) 0);
        return myMap;
    }

    public Classes getCurrentClass() {
        return currentClass;
    }

    public void updateCache(Classes newItem , Classes oldItem) throws ClassChangedException {
        Classes oldClass = currentClass;
        ClassX.getInstance().toLog("Trying to update the cache", debug);
        cache.put(newItem, (short) (cache.get(newItem) + 1));
        ClassX.getInstance().toLog("Update the cache to add an item of class " + newItem , debug);
        cache.put(oldItem, (short) (cache.get(oldItem) - 1));
        ClassX.getInstance().toLog("Update the cache to remove an item of class " + oldItem, debug);
        for (Classes c : Classes.values()) {
            if(cache.get(c) == 4) {
                currentClass = c;
                ClassX.getInstance().toLog("Class changed to " + currentClass, debug);
                throw new ClassChangedException(currentClass, oldClass);
            }
        }
        if (currentClass != Classes.NONE) {
            currentClass = Classes.NONE;
            ClassX.getInstance().toLog("Class changed to " + currentClass, debug);
            throw new ClassChangedException(currentClass, oldClass);
        }
    }

}

package com.bestudios.classx;

import com.bestudios.classx.util.EquipmentCache;
import com.bestudios.corex.utils.RoundSmartCache;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayersCache {

    private static final PlayersCache instance = new PlayersCache() ;
    private PlayersCache() {
        ClassX.getInstance().toLog("Initializing the Players Cache", ClassX.getInstance().isDebugMode());
        playerEquipCache = new RoundSmartCache<>(new PlayerCacheFactory());
    }
    public static PlayersCache getInstance() { return instance; }

    protected RoundSmartCache<EquipmentCache> playerEquipCache;

    public EquipmentCache getPlayerCache(UUID playerID) {
        // ClassX.getInstance().toLog("Trying to retrieve the player cache for " + playerID, ClassX.getInstance().isDebugMode());
        return playerEquipCache.get(playerID);
    }

    public static class PlayerCacheFactory implements Supplier<EquipmentCache> {

        @Override
        public EquipmentCache get() {
            ClassX.getInstance().toLog("Creating a new Equipment Cache", ClassX.getInstance().isDebugMode());
            return new EquipmentCache();
        }
    }
}

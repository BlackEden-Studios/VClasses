package com.bestudios.classx.caches;

import com.bestudios.classx.ClassX;
import com.bestudios.corex.caches.RoundSmartCache;

import java.util.UUID;
import java.util.function.Supplier;


/**
 * PlayersCache is a singleton class that manages the cache for player equipment.
 * It uses a RoundSmartCache to store EquipmentCache instances for each player identified by their UUID.
 */
public class PlayersCache {
    /** Singleton instance of PlayersCache */
    private static final PlayersCache instance = new PlayersCache();
    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the playerEquipCache with a PlayerCacheFactory.
     */
    private PlayersCache() {
        ClassX.getInstance().toLog("Initializing the Players Cache", ClassX.getInstance().isDebugMode());
        playerEquipCache = new RoundSmartCache<>(new PlayerCacheFactory());
    }
    /**
     * Returns the singleton instance of PlayersCache.
     *
     * @return the singleton instance of PlayersCache
     */
    public static PlayersCache getInstance() { return instance; }

    /**
     * Cache for player equipment, using UUID as the key and EquipmentCache as the value.
     * The cache is initialized with a PlayerCacheFactory to create new EquipmentCache instances.
     */
    protected RoundSmartCache<EquipmentCache> playerEquipCache;

    /**
     * Retrieves the EquipmentCache for a specific player identified by their UUID.
     * If the cache does not exist, it will create a new one using the PlayerCacheFactory.
     *
     * @param playerID the UUID of the player
     * @return the EquipmentCache for the specified player
     */
    public EquipmentCache getPlayerCache(UUID playerID) {
        return playerEquipCache.get(playerID);
    }

    /**
     * Factory class to create new EquipmentCache instances.
     */
    public static class PlayerCacheFactory implements Supplier<EquipmentCache> {

        /**
         * Creates a new EquipmentCache instance.
         * Logs the creation of the cache if debug mode is enabled.
         *
         * @return a new EquipmentCache instance
         */
        @Override
        public EquipmentCache get() {
            ClassX.getInstance().toLog("Creating a new Equipment Cache", ClassX.getInstance().isDebugMode());
            return new EquipmentCache();
        }
    }
}

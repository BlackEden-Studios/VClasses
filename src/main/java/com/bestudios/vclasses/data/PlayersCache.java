package com.bestudios.vclasses.data;

import com.bestudios.fulcrum.api.cache.PlayerDataLoader;
import com.bestudios.vclasses.VClasses;
import com.bestudios.fulcrum.api.cache.SessionCache;
import com.bestudios.vclasses.util.DummySaver;

import java.util.UUID;

/**
 * Cache for player equipment
 */
public class PlayersCache {

  protected static SessionCache<EquipmentCache> playerCache = new SessionCache<>(
          VClasses.getInstance(),
          new DummySaver<>(),
          new PlayerCacheFactory()
  );

  public static EquipmentCache getPlayerCache(UUID playerID) {
    return playerCache.get(playerID);
  }

  public static class PlayerCacheFactory implements PlayerDataLoader<EquipmentCache> {

    @Override
    public EquipmentCache load(UUID playerID) {
        return new EquipmentCache();
    }
  }
}

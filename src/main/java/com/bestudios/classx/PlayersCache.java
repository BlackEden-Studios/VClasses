package com.bestudios.classx;

import com.bestudios.classx.util.EquipCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayersCache implements Listener {

    private static final PlayersCache instance = new PlayersCache() ;
    private PlayersCache() {}
    public static PlayersCache getInstance() { return instance; }

    protected Map<UUID, EquipCache> playerEquipCache = new HashMap<>(ClassX.PREDICTED_MAX_PLAYERS);

    public Map<UUID, EquipCache> getCache() {
        return playerEquipCache;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        playerEquipCache.put(event.getPlayer().getUniqueId(), new EquipCache());
    }
}

package com.bestudios.classx.util;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TaskCancellationEvent extends Event {

    private final Player boundPlayer;
    private final Object receiver;

    public TaskCancellationEvent(Player player, Object actualReceiver) {
        super();
        boundPlayer = player;
        receiver = actualReceiver;
    }

    public Player getPlayer() {
        return boundPlayer;
    }

    public Object getReceiver() {
        return receiver;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}

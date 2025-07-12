package com.bestudios.classx.util;

import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.corex.basics.TimerInfo;
import org.bukkit.entity.Player;


/**
 * ClassActivationEntry represents an entry for a player's class activation queue.
 * It contains the player, the role class they are activating, and a timer to track the validity of the activation.
 */
public class ClassActivationEntry {

    /** The player who is activating the class */
    private final Player _player;
    /** The timer that tracks the validity of the class activation */
    private final TimerInfo _timer;
    /** The role class that the player is activating */
    private final RoleClassEnum _roleClass;
    /**
     * Constructs a ClassActivationEntry with the specified player, role class, and timer.
     *
     * @param player the player activating the class
     * @param roleClass the role class being activated
     * @param timer the timer that tracks the validity of the activation
     */
    public ClassActivationEntry(Player player, RoleClassEnum roleClass, TimerInfo timer) {
        _player = player;
        _timer = timer;
        _roleClass = roleClass;
    }
    /**
     * Checks if the class activation is still valid based on the timer.
     *
     * @return true if the activation is valid, false otherwise
     */
    public boolean isValid() {
        return _timer.isValid();
    }
    /**
     * Gets the player associated with this class activation entry.
     * @return the player who is activating the class
     */
    public Player getPlayer() {
        return _player;
    }
    /**
     * Gets the class in which the player is changing into.
     * @return the role class being activated by the player
     */
    public RoleClassEnum getRoleClass() {
        return _roleClass;
    }
}

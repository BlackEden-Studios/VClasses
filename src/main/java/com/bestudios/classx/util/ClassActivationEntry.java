package com.bestudios.classx.util;

import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.corex.utils.TimerInfo;
import org.bukkit.entity.Player;

public class ClassActivationEntry {

    private final Player _player;
    private final TimerInfo _timer;
    private final RoleClassEnum _roleClass;

    public ClassActivationEntry(Player player, RoleClassEnum roleClass, TimerInfo timer) {
        _player = player;
        _timer = timer;
        _roleClass = roleClass;
    }

    public boolean isValid() {
        return _timer.isValid();
    }

    public Player getPlayer() {
        return _player;
    }

    public RoleClassEnum getRoleClass() {
        return _roleClass;
    }
}

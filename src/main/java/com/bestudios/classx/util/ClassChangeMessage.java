package com.bestudios.classx.util;

import com.bestudios.classx.classes.RoleClassEnum;


/**
 * ClassChangeMessage is passed when a player changes their role-class
 * by wearing a different set of armor items.
 * It contains information about the new and former role-classes.
 */
public class ClassChangeMessage {
    /** Enum representing the new role-class the player is changing into */
    private final RoleClassEnum _newRoleClass;
    /** Enum representing the player's role-class before the change */
    private final RoleClassEnum _formerRoleClass;

    /*
     * Constructs a ClassChangeMessage with the specified new and former role-classes.
     */
    public ClassChangeMessage(RoleClassEnum newClassType, RoleClassEnum formerClassType) {
        _newRoleClass = newClassType;
        _formerRoleClass = formerClassType;
    }

    /**
     * Returns the new role-class the player is changing into.
     * @return The enum representing the new role-class
     */
    public RoleClassEnum getNewClassType() {
        return _newRoleClass;
    }

    /**
     * Returns the player's role-class before the change.
     * @return The enum referred to player's role-class before the change
     */
    public RoleClassEnum getFormerClassType() {
        return _formerRoleClass;
    }
}

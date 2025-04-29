package com.bestudios.classx.util;

import com.bestudios.classx.classes.RoleClassEnum;

public class ClassChangedException extends Exception{

    private final RoleClassEnum _newRoleClass;

    private final RoleClassEnum _formerRoleClass;

    /**
        Exception thrown when a player changes their role-class
        by wearing a different set of armor items
     */
    protected ClassChangedException(RoleClassEnum newClassType, RoleClassEnum formerClassType) {
        super();
        _newRoleClass = newClassType;
        _formerRoleClass = formerClassType;
    }

    /**
     *
     * @return The enum referred to the role-class the player is changing into
     */
    public RoleClassEnum getNewClassType() {
        return _newRoleClass;
    }

    /**
     *
     * @return The enum referred to player's role-class before the change
     */
    public RoleClassEnum getFormerClassType() {
        return _formerRoleClass;
    }
}

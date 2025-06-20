package com.bestudios.classx.classes;

/**
    Enumeration class that should hold an entry for every implemented role class
 */
public enum RoleClassEnum {
    NONE,
    ARCHER,
    BARD,
    ROGUE,
    WARRIOR;
    /**
     *  Returns the RoleClassEnum value corresponding to the given name.
     *  If the name does not match any enum constant, it returns NONE.
     *
     * @param name The name of the role class.
     * @return The corresponding RoleClassEnum value or NONE if not found.
     */
    public static RoleClassEnum getClassByName(String name) {
        try {
            return RoleClassEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RoleClassEnum.NONE;
        }
    }

}



package com.bestudios.classx.classes;


/**
 * This class represents a role class that does not have any specific attributes or abilities.
 * It is used as a placeholder for cases where no role class is applicable.
 *
 * @version 1.0
 * @since 1.0
 */
public class NoneClass extends RoleClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static RoleClassType instance = new NoneClass();

    /**
     * Private constructor for the Singleton
     */
    private NoneClass(){
        super("none.yml", RoleClassEnum.NONE);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static RoleClassType getInstance() { return instance; }

}

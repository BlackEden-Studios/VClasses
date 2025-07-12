package com.bestudios.classx.classes;


/**
 * Class representing the Warrior class in the game.
 * This class extends RoleClassType and is designed to be a Singleton.
 * It initializes with a specific configuration file and role class type.
 *
 * @version 1.0
 * @since 1.0
 */
public class WarriorClass extends RoleClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static RoleClassType instance = new WarriorClass();

    /**
     * Private constructor for the Singleton
     */
    private WarriorClass(){
        super("warrior.yml", RoleClassEnum.WARRIOR);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static RoleClassType getInstance() { return instance; }

}

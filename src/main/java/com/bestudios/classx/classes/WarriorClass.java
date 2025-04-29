package com.bestudios.classx.classes;

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

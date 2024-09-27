package com.bestudios.classx.classes;

import com.bestudios.classx.Classes;

public class WarriorClass extends ClassType{

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static ClassType instance = new WarriorClass();

    /**
     * Private constructor for the Singleton
     */
    private WarriorClass(){
        super("warrior.yml", Classes.WARRIOR);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static ClassType getInstance() { return instance; }

}

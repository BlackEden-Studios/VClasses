package com.bestudios.classx.classes;

import com.bestudios.classx.Classes;

public class NoneClass extends ClassType{

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static ClassType instance = new NoneClass();

    /**
     * Private constructor for the Singleton
     */
    private NoneClass(){
        super("none.yml", Classes.NONE);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static ClassType getInstance() { return instance; }

}

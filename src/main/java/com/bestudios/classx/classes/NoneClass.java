package com.bestudios.classx.classes;

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

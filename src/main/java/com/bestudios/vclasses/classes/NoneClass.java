package com.bestudios.vclasses.classes;

import com.bestudios.vclasses.data.ClassLoadingException;

/**
 * None Class (Default)
 *
 * @version 0.12.0
 */
public class NoneClass extends RoleClassType {

  /** Instance variable, intended to be used as a Singleton reference */
  protected static NoneClass instance = new NoneClass();

  /**
   * The only way to retrieve the class instance
   * @return instance of the class
   */
  public static NoneClass getInstance() {
    return instance;
  }

  /** Private constructor for the Singleton */
  private NoneClass() {
    super("none.yml", RoleClassEnum.NONE);
  }

  @Override
  protected void additionalSetup() throws ClassLoadingException {
    // No specific configuration for None
  }

  @Override
  protected void abilityConfiguration() {
    // No specific ability listener for None
  }

}
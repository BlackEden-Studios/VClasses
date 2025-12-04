package com.bestudios.vclasses.classes;

import com.bestudios.vclasses.data.ClassLoadingException;

/**
 * Warrior Class
 *
 * @version 0.12.0
 */
public class WarriorClass extends RoleClassType {

  /** Instance variable, intended to be used as a Singleton reference */
  protected static WarriorClass instance = new WarriorClass();

  /**
   * The only way to retrieve the class instance
   * @return instance of the class
   */
  public static WarriorClass getInstance() {
    return instance;
  }

  /** Private constructor for the Singleton */
  private WarriorClass() {
    super("warrior.yml", RoleClassEnum.WARRIOR);
  }

  @Override
  protected void additionalSetup() throws ClassLoadingException {
    // No specific configuration for Warrior currently
  }

  @Override
  protected void abilityConfiguration() {
    // No specific ability listener for Warrior currently
  }

}
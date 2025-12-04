package com.bestudios.vclasses.data;

/**
 * Representation of an error that occurs when loading a class.
 */
public class ClassLoadingException extends Exception {

  public ClassLoadingException(String s) {
    super(s);
  }
}


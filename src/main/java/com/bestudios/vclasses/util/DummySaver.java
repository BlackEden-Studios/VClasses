package com.bestudios.vclasses.util;

import com.bestudios.fulcrum.api.cache.PlayerDataSaver;

import java.util.UUID;

public class DummySaver<T> implements PlayerDataSaver<T> {
  @Override
  public boolean save(UUID playerID) {
    return true;
  }
}

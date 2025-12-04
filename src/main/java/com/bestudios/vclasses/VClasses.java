package com.bestudios.vclasses;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.vclasses.classes.ArcherClass;
import com.bestudios.vclasses.classes.BardClass;
import com.bestudios.vclasses.classes.NoneClass;
import com.bestudios.vclasses.classes.RogueClass;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.vclasses.classes.RoleClassType;
import com.bestudios.vclasses.classes.WarriorClass;
import com.bestudios.vclasses.managers.ArmorListener;
import com.bestudios.vclasses.managers.CommandsManager;
import com.bestudios.vclasses.data.ClassActivationQueue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class VClasses extends FulcrumPlugin {

  private static final int IMPLEMENTED_CLASSES_COUNT = 5;

  public static Map <RoleClassEnum, RoleClassType> implementedClasses;

  @Override
  protected void showPluginTitle() {

  }

  @Override
  protected void additionalInitializationTasks() {

    // Role-classes startup
    implementedClasses = implementedClassInitialization();
    for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet())
      implementedClass.getValue().initialize();

    // Listeners start-up
    this.getServer().getPluginManager().registerEvents(ArmorListener.getInstance(), this);

    // Tasks startup
    ClassActivationQueue.getInstance().runTaskTimer(this,1,1);

    this.getLogger().info("Plugin loaded!");
  }

  @Override
  protected void registerAdditionalCommands() {
    getCommandsRegistry().register("vclasses", Map.ofEntries(
            Map.entry("help",    CommandsManager.getHelpCommand()),
            Map.entry("enable",  CommandsManager.getEnableCommand()),
            Map.entry("disable", CommandsManager.getDisableCommand())
    ));
  }

  @Override
  protected void additionalTerminationTasks() {
    // Role-classes shutdown
    for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet())
      implementedClass.getValue().saveConfig();
  }

  @Override
  public DatabaseGateway getDatabaseGateway() {
    return null;
  }

  public static final List<String> classNames = List.of(
    "Archer",
    "Bard",
    "Rogue",
    "Warrior"
  );

  private @NotNull Map<RoleClassEnum, RoleClassType> implementedClassInitialization() {
    // Try to load every single class
    HashMap<RoleClassEnum, RoleClassType> map = new HashMap<>(Map.ofEntries(
            Map.entry(RoleClassEnum.NONE,    NoneClass.getInstance()),
            Map.entry(RoleClassEnum.ARCHER,  ArcherClass.getInstance()),
            Map.entry(RoleClassEnum.BARD,    BardClass.getInstance()),
            Map.entry(RoleClassEnum.ROGUE,   RogueClass.getInstance()),
            Map.entry(RoleClassEnum.WARRIOR, WarriorClass.getInstance())
    ));

    // Get rid of unloaded classes (null references)
    HashMap<RoleClassEnum, RoleClassType> actualMap = new HashMap<>(IMPLEMENTED_CLASSES_COUNT);
    for (Map.Entry<RoleClassEnum, RoleClassType> entry : map.entrySet())
        if (entry.getValue() != null) actualMap.put(entry.getKey(),entry.getValue());

    return actualMap;
  }

  public static VClasses getInstance() {
    return getPlugin(VClasses.class);
  }

}


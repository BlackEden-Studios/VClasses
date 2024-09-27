package com.bestudios.classx;

import com.bestudios.classx.classes.*;
import com.bestudios.classx.util.CustomFoodControl;
import com.bestudios.corex.utils.BEPlugin;

import java.util.AbstractMap;
import java.util.Map;

public final class ClassX extends BEPlugin {

    public static final int PREDICTED_MAX_PLAYERS = 300;

    public static Map <Classes, ClassType> implementedClasses;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ClassXSettingsManager.getInstance().load();
        ClassX.getInstance().setDebugMode( ClassXSettingsManager.getInstance().getDebugConfig() );
        ClassX.getInstance().toLog("Debug mode detected", ClassX.getInstance().isDebugMode());

        implementedClasses = implementedClassInitialization();

        ClassX.getInstance().getPluginManager().registerEvents(PlayersCache.getInstance(), this);
        ClassX.getInstance().getPluginManager().registerEvents(ArmorListener.getInstance(), this);
        ClassX.getInstance().getPluginManager().registerEvents(CustomFoodControl.getInstance(),this);

        for( Map.Entry<Classes, ClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().load(); }

        ClassX.getInstance().toLog("Load complete", ClassX.getInstance().isDebugMode());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for( Map.Entry<Classes, ClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().save(); }

        ClassX.getInstance().toLog("Save complete", ClassX.getInstance().isDebugMode());
    }

    public static ClassX getInstance() {
        return getPlugin(ClassX.class);
    }

    private Map<Classes, ClassType> implementedClassInitialization() {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>(Classes.NONE, NoneClass.getInstance()),
                new AbstractMap.SimpleEntry<>(Classes.ARCHER, ArcherClass.getInstance()),
                new AbstractMap.SimpleEntry<>(Classes.BARD, BardClass.getInstance()),
                new AbstractMap.SimpleEntry<>(Classes.ROGUE, RogueClass.getInstance()),
                new AbstractMap.SimpleEntry<>(Classes.WARRIOR, WarriorClass.getInstance())
        );
    }

}


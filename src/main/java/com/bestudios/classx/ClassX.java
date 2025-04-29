package com.bestudios.classx;

import com.bestudios.classx.classes.*;
import com.bestudios.classx.util.ClassActivationQueue;
import com.bestudios.corex.CoreX;
import com.bestudios.corex.utils.BEPlugin;

import java.util.HashMap;
import java.util.Map;

public final class ClassX extends BEPlugin {

    private static final int IMPLEMENTED_CLASSES_COUNT = 5;
    public static final int MAX_PLAYERS = CoreX.getMaxPlayers();

    public static Map <RoleClassEnum, RoleClassType> implementedClasses;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Config startup
        ClassXSettingsManager.getInstance().setUpConfigs();
        ClassX.getInstance().setDebugMode( ClassXSettingsManager.getInstance().getDebugConfig() );
        ClassX.getInstance().toLog("Debug mode detected", ClassX.getInstance().isDebugMode());

        // Role-classes startup
        implementedClasses = implementedClassInitialization();
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().setUpConfigs(); }

        // Listeners start-up
        PlayersCache.getInstance();
        ClassX.getInstance().getPluginManager().registerEvents(ArmorListener.getInstance(), this);

        // Tasks startup
        ClassActivationQueue.getInstance().runTaskTimer(ClassX.getInstance(),1,1);

        ClassX.getInstance().toLog("Load complete", ClassX.getInstance().isDebugMode());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Classes
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().save(); }

        ClassX.getInstance().toLog("Save complete", ClassX.getInstance().isDebugMode());
    }

    private Map<RoleClassEnum, RoleClassType> implementedClassInitialization() {

        // Try to load every single class
        HashMap<RoleClassEnum, RoleClassType> map = new HashMap<>(IMPLEMENTED_CLASSES_COUNT);
        try { map.put(RoleClassEnum.NONE, NoneClass.getInstance()); } catch (Exception ignored) {
            toLog("None role-class not loaded"); }
        try { map.put(RoleClassEnum.ARCHER, ArcherClass.getInstance()); } catch (Exception ignored) {
            toLog("Archer role-class not loaded"); }
        try { map.put(RoleClassEnum.BARD, BardClass.getInstance()); } catch (Exception ignored) {
            toLog("Bard role-class not loaded"); }
        try { map.put(RoleClassEnum.ROGUE, RogueClass.getInstance()); } catch (Exception ignored) {
            toLog("Rogue role-class not loaded"); }
        try { map.put(RoleClassEnum.WARRIOR, WarriorClass.getInstance()); } catch (Exception ignored) {
            toLog("Warrior role-class not loaded"); }

        // Get rid of unloaded classes (null references)
        HashMap<RoleClassEnum, RoleClassType> actualMap = new HashMap<>(IMPLEMENTED_CLASSES_COUNT);
        for (Map.Entry<RoleClassEnum, RoleClassType> entry : map.entrySet())
            if (entry.getValue() != null) actualMap.put(entry.getKey(),entry.getValue());

        return actualMap;
    }

    public static ClassX getInstance() {
        return getPlugin(ClassX.class);
    }

}


package com.bestudios.classx;

import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.classx.classes.*;
import com.bestudios.classx.managers.ArmorListener;
import com.bestudios.classx.managers.ClassXSettingsManager;
import com.bestudios.classx.managers.CommandsManager;
import com.bestudios.classx.util.ClassActivationQueue;
import com.bestudios.corex.CoreX;
import com.bestudios.corex.basics.BEPlugin;
import com.bestudios.corex.managers.CoreXSettingsManager;
import com.bestudios.corex.utils.LanguageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ClassX extends BEPlugin {

    private static final int IMPLEMENTED_CLASSES_COUNT = 5;
    public static final int MAX_PLAYERS = CoreX.getMaxPlayers();

    public static Map <RoleClassEnum, RoleClassType> implementedClasses;

    public static LanguageManager LANGUAGES;

    @Override
    public void onEnable() {
        // Plugin title
        String title ="""
                \n
                     ______________   ____      ___           ______________   _____________      _____
                    /             /  /   /     /   \\         /             /  /             \\    /    /
                   /    _________/  /   /     /_____\\       /_____________/  /___________    \\  /    /
                  /    /           /   /     _________      _____________   ____________ \\    \\/    /
                 /    /           /   /     /         \\    /            /  /            / \\        /
                /    /           /   /     /    ___    \\  /______      /  /______      /  /       /
                \\    \\          /   /     /    /   \\    \\       /     /         /     /  /        \\
                 \\    \\_____   /   /_____/    /     \\    \\_____/     /  _______/     /  /    /\\    \\
                  \\        /  /              /       \\              /  /            /  /    /  \\    \\
                   \\______/  /______________/         \\____________/  /____________/  /____/    \\____\\
                  \n
                """;
        getInstance().toLog(title);

        // Plugin startup logic

        // Config startup
        ClassXSettingsManager.getInstance().setUpConfigs();
        ClassX.getInstance().setDebugMode( ClassXSettingsManager.getInstance().getDebugConfig() );
        ClassX.getInstance().toLog("Debug mode detected", ClassX.getInstance().isDebugMode());

        LANGUAGES = new LanguageManager(
                getDataFolder(),
                CoreXSettingsManager.getInstance().getConfig().getString("language" , "en") + ".yml",
                "ClassX"
        );

        // Role-classes startup
        implementedClasses = implementedClassInitialization();
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().setUpConfigs(); }

        // Listeners start-up
        PlayersCache.getInstance();
        ClassX.getInstance().getPluginManager().registerEvents(ArmorListener.getInstance(), this);

        // Tasks startup
        ClassActivationQueue.getInstance().runTaskTimer(ClassX.getInstance(),1,1);

        CommandsManager commandsManager = new CommandsManager();
        // Commands startup
        Objects.requireNonNull(ClassX.getInstance().getCommand("classx")).setExecutor(commandsManager.getWrapper());
        Objects.requireNonNull(ClassX.getInstance().getCommand("classx")).setTabCompleter(commandsManager.getWrapper());

        ClassX.getInstance().toLog("Load complete", ClassX.getInstance().isDebugMode());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Classes
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().save(); }

        ClassX.getInstance().toLog("Save complete", ClassX.getInstance().isDebugMode());
    }

    public static final List<String> classNames = List.of(
            "Archer",
            "Bard",
            "Rogue",
            "Warrior"
    );

    private Map<RoleClassEnum, RoleClassType> implementedClassInitialization() {

        // Try to load every single class
        HashMap<RoleClassEnum, RoleClassType> map = new HashMap<>(Map.ofEntries(
                Map.entry(RoleClassEnum.NONE, NoneClass.getInstance()),
                Map.entry(RoleClassEnum.ARCHER, ArcherClass.getInstance()),
                Map.entry(RoleClassEnum.BARD, BardClass.getInstance()),
                Map.entry(RoleClassEnum.ROGUE, RogueClass.getInstance()),
                Map.entry(RoleClassEnum.WARRIOR, WarriorClass.getInstance())
        ));

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


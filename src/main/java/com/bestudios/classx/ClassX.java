package com.bestudios.classx;

import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.classx.classes.*;
import com.bestudios.classx.managers.ArmorListener;
import com.bestudios.classx.util.ClassActivationQueue;
import com.bestudios.corex.CoreX;
import com.bestudios.corex.basics.BEPlugin;
import com.bestudios.corex.utils.CommandWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * ClassX is a Paper plugin that implements role-based classes for players.
 * It provides various classes with unique abilities and configurations.
 * The plugin manages class activation, cooldowns, and player interactions.
 */
public final class ClassX extends BEPlugin {

    /** The number of implemented classes in the plugin. */
    private static final int IMPLEMENTED_CLASSES_COUNT = 5;
    /** The version of the plugin. */
    public static final Double PLUGIN_VERSION = 0.11;
    /** The maximum number of players supported by the plugin, derived from CoreX. */
    public static final int MAX_PLAYERS = CoreX.getMaxPlayers();
    /** A map containing all implemented classes, indexed by their enum type. */
    public static Map <RoleClassEnum, RoleClassType> implementedClasses;
    /** A list of class names available in the plugin. */
    public static final List<String> classNames = List.of(
            "Archer",
            "Bard",
            "Rogue",
            "Warrior"
    );

    /**
     * Constructor for ClassX.
     * Initializes the plugin with default settings and command mappings.
     */
    public ClassX() {
        super(
            defaultSettingsYML,
            defaultLanguagesYMLs,
            commandActionMap,
            playerCommandActionMap,
            tabCompleterMap
        );
    }
    /**
     * Retrieves the singleton instance of ClassX.
     * This method is used to access the plugin instance from other classes.
     *
     * @return The singleton instance of ClassX.
     */
    public static ClassX getInstance() {
        return getPlugin(ClassX.class);
    }
    /**
     * Called when the plugin is enabled.
     * This method sets up configurations, initializes classes, registers listeners, and starts tasks.
     */
    @Override
    public void onEnable() {
        // Plugin title
        getInstance().toLog(TITLE);

        // Plugin startup logic

        // Role-classes startup
        implementedClasses = implementedClassInitialization();
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().setUpConfigs(); }

        // Listeners start-up
        PlayersCache.getInstance();
        getPluginManager().registerEvents(ArmorListener.getInstance(), this);

        // Tasks startup
        ClassActivationQueue.getInstance().runTaskTimer(ClassX.getInstance(),1,1);

        // Commands startup
        Objects.requireNonNull(getCommand("classx")).setExecutor(getCommandsManager().getCommandExecutor());
        Objects.requireNonNull(getCommand("classx")).setTabCompleter(getCommandsManager().getTabCompleter());

        ClassX.getInstance().toLog("Load complete", ClassX.getInstance().isDebugMode());

    }

    /**
     * Called when the plugin is disabled.
     * This method saves the state of all classes and performs any necessary cleanup.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Classes
        for( Map.Entry<RoleClassEnum, RoleClassType> implementedClass : implementedClasses.entrySet()) { implementedClass.getValue().save(); }

        ClassX.getInstance().toLog("Save complete", ClassX.getInstance().isDebugMode());
    }

    /**
     * Initializes the implemented classes by loading each class type.
     * It creates a map of RoleClassEnum to RoleClassType, filtering out any null references.
     *
     * @return A map containing the initialized classes.
     */
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

    /**
     * Returns the title of the plugin, which is displayed in the console upon startup.
     * The title includes a stylized ASCII art representation of the plugin name.
     */
    private static final String TITLE = """
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
    /**
     * The default settings for the plugin, formatted as a YAML string.
     * This string contains configuration options for various classes and their abilities.
     */
    private static final String defaultSettingsYML = """
                version: %s
                debug: false
                max_activation_queue_operations: 5
                class_activation_cooldown: 10
                ##############################################################
                #                      Class Activators                      #
                ##############################################################
                classes:
                  archer: true
                  bard: true
                  rogue: true
                  warrior: true
                ##############################################################
                #                       Archer Class                         #
                ##############################################################
                archer:
                  tag_time: 10
                  buff_effect_level: 3
                  # Damage multiplier should be an integer, the percentile is handled internally
                  damage_multiplier: 7
                ##############################################################
                #                         Bard Class                         #
                ##############################################################
                bard:
                  effect_range: 20
                  default_effect_duration: 5
                  amplified_effect_duration: 10
                  horn_usage_cooldown: 60
                  # Effects should be indicated as in-game values, real values are handled internally
                  # E.G speed_effect: 1 for Speed Lvl 1
                  speed_effect: 2
                  amplified_speed_effect: 3
                  strength_effect: 2
                  amplified_strength_effect: 4
                  haste_effect: 2
                  amplified_haste_effect: 3
                  regeneration_effect: 2
                  amplified_regeneration_effect: 4
                ##############################################################
                #                        Rogue Class                         #
                ##############################################################
                rogue:
                  knife_throw_cooldown: 3
                ##############################################################
                #                       Warrior Class                        #
                ##############################################################
                # No configurations available for warrior class
               
                """.formatted(PLUGIN_VERSION);

    /**
     * A map containing default language configurations for the plugin.
     * The keys are language codes (e.g., "en", "it"), and the values are YAML strings
     */
    private static final Map<String, String> defaultLanguagesYMLs = Map.of(
            "en", """
                version: %s
                class_activation_success: Class abilities are now active!
                class_action_not_allowed: You don't wear the right armor
                bard:
                  horn_cooldown_message: Horn is in cooldown
                rogue:
                  knife_cooldown_message: Knife is in cooldown
                """.formatted(PLUGIN_VERSION),
            "it", """
                version: %s
                class_activation_success: Le abilità della classe sono ora attive!
                class_action_not_allowed: Non indossi l'armatura giusta
                bard:
                  horn_cooldown_message: Il corno è in cooldown
                rogue:
                    knife_cooldown_message: Il coltello è in cooldown
                """.formatted(PLUGIN_VERSION)
    );

    /**
     * A map containing command actions for the plugin.
     * The keys are command names, and the values are the actions to be executed when the command is invoked.
     */
    private static final Map<String, CommandWrapper.CommandAction> commandActionMap = Map.of(
            "help", ctx -> {
                ctx.sender().sendMessage("ClassX Commands:");
                ctx.sender().sendMessage("/classx enable <class_name> - Enable a class");
                ctx.sender().sendMessage("/classx disable <class_name> - Disable a class");
                return true;
            }, "enable", ctx -> {
                // Retrieve the class name from the command arguments
                RoleClassEnum classEnum = RoleClassEnum.getClassByName(ctx.remainingArgs()[0]);
                // If the class exists, enable it; otherwise, send an error message
                if (classEnum.equals(RoleClassEnum.NONE)) ctx.sender().sendMessage("Invalid class name!");
                else {
                    ClassX.implementedClasses.get(classEnum).enableClass();
                    ctx.sender().sendMessage("You have enabled the " + classEnum + " class!");
                }
                return true;
            }, "disable", ctx -> {
                // Retrieve the class name from the command arguments
                RoleClassEnum classEnum = RoleClassEnum.getClassByName(ctx.remainingArgs()[0]);
                // If the class exists, disable it; otherwise, send an error message
                if (classEnum.equals(RoleClassEnum.NONE)) ctx.sender().sendMessage("Invalid class name!");
                else {
                    ClassX.implementedClasses.get(classEnum).disableClass();
                    ctx.sender().sendMessage("You have disabled the " + classEnum + " class!");
                }
                return true;
            }
    );

    /**
     * A map containing player command actions for the plugin.
     * The keys are command names, and the values are the actions to be executed when the command is invoked by a player.
     */
    private static final Map<String, CommandWrapper.PlayerCommandAction> playerCommandActionMap = new HashMap<>();

    /*
     * A map containing tab completion functions for the plugin.
     * The keys are command names, and the values are functions that provide tab completion suggestions based on the command context.
     */
    private static final Map<String, CommandWrapper.TabCompleteFunction> tabCompleterMap = Map.of(
            "enable", ctx -> {
                if (ctx.remainingArgs().length <= 1) {
                    // If the command has no arguments or only one argument, provide class name completions
                    String partial = ctx.remainingArgs().length > 0 ? ctx.remainingArgs()[0] : "";
                    return CommandWrapper.filterCompletions(ClassX.classNames, partial);
                }
                return List.of();
            },
            "disable", ctx -> {
                if (ctx.remainingArgs().length <= 1) {
                    // If the command has no arguments or only one argument, provide class name completions
                    String partial = ctx.remainingArgs().length > 0 ? ctx.remainingArgs()[0] : "";
                    return CommandWrapper.filterCompletions(ClassX.classNames, partial);
                }
                return List.of();
            }
    );
}


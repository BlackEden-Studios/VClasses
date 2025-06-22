package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;
import com.bestudios.corex.basics.ConfigLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClassXSettingsManager extends ConfigLoader {

    private final static ClassXSettingsManager instance = new ClassXSettingsManager();
    /* Private Constructor */
    private ClassXSettingsManager() {
        super(ClassX.getInstance().getDataFolder(), "config.yml", "ClassX");
    }
    public static ClassXSettingsManager getInstance() { return instance; }

    private boolean debug = false;
    public boolean getDebugConfig() { return debug; }

    private int classActivationCooldown = 0;
    public int getClassActivationCooldown() { return classActivationCooldown; }

    /*
     * The method load() gets the config values from the file "config.yml" and saves them into the config object
     */
    @Override
    public void setUpConfigs() {
        super.setUpConfigs();

        // Default config values
        try {
            Files.createDirectories(Paths.get(String.valueOf(ClassX.getInstance().getDataFolder())));
            File configFile = new File(ClassX.getInstance().getDataFolder(), "config.yml");
            // Create file if it doesn't exist
            if (!configFile.exists()) {
                String defaultYml = """
                debug: false
                max_activation_queue_operations: 5
                class_activation_cooldown: 10
                ##############################################################
                #                       Archer Class                         #
                ##############################################################
                archer_class_tag_time: 10
                archer_class_buff_effect_level: 3
                # Damage multiplier should be an integer, the percentile is handled internally
                archer_class_damage_multiplier: 7
                ##############################################################
                #                         Bard Class                         #
                ##############################################################
                bard_class_effect_range: 20
                bard_class_default_effect_duration: 5
                bard_class_amplified_effect_duration: 10
                bard_class_horn_usage_cooldown: 60
                # Effects should be indicated as in-game values, real values are handled internally
                # E.G speed_effect: 1 for Speed Lvl 1
                bard_class_speed_effect: 2
                bard_class_amplified_speed_effect: 3
                bard_class_strength_effect: 2
                bard_class_amplified_strength_effect: 4
                bard_class_haste_effect: 2
                bard_class_amplified_haste_effect: 3
                bard_class_regeneration_effect: 2
                bard_class_amplified_regeneration_effect: 4
                ##############################################################
                #                        Rogue Class                         #
                ##############################################################
                rogue_class_knife_throw_cooldown: 3
                ##############################################################
                #                       Warrior Class                        #
                ##############################################################
                # No configurations available for warrior class
                ##############################################################
                #                      Class Activators                      #
                ##############################################################
                classes:
                  archer: true
                  bard: true
                  rogue: true
                  warrior: true
                
                """;

                Files.writeString(configFile.toPath(), defaultYml);
                System.out.println("Created default config.yml");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create default config file", e);
        }

        debug = this.getConfig().getBoolean("debug");
        classActivationCooldown = this.getConfig().getInt("class_activation_cooldown");
    }

}

package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;
import com.bestudios.corex.basics.ConfigLoader;

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
        debug = this.getConfig().getBoolean("debug");
        classActivationCooldown = this.getConfig().getInt("class_activation_cooldown");
    }

}

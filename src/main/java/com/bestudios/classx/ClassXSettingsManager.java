package com.bestudios.classx;

import com.bestudios.corex.utils.ConfigLoader;

public class ClassXSettingsManager extends ConfigLoader {
    private final static ClassXSettingsManager instance = new ClassXSettingsManager();
    /* Private Constructor */
    private ClassXSettingsManager() {
        super(ClassX.getInstance().getDataFolder(), "config.yml", "ClassX");
    }
    public static ClassXSettingsManager getInstance() { return instance; }

    private boolean debug = false;
    protected boolean getDebugConfig() { return debug; }

    private int classActivationCooldown = 0;
    public int getClassActivationCooldown() { return classActivationCooldown; }

    /*
     * The method load() gets the config values from the file "config.yml" and saves them into the config object
     */
    @Override
    public void load() {
        super.load();
        debug = this.getConfig().getBoolean("debug");
        classActivationCooldown = this.getConfig().getInt("class_activation_cooldown");
    }

}

package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.corex.services.HooksManager;
import com.bestudios.corex.basics.TimerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;


/**
 * Rogue class implementation for ClassX plugin.
 * This class handles the rogue's unique ability of throwing knives with a cooldown.
 * It is designed as a Singleton to ensure only one instance exists throughout the plugin's lifecycle.
 *
 * @version 1.0
 * @since 1.0
 */
public class RogueClass extends RoleClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static RoleClassType instance = new RogueClass();

    /**
     * Private constructor for the Singleton
     */
    private RogueClass(){
        super("rogue.yml", RoleClassEnum.ROGUE);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static RoleClassType getInstance() { return instance; }

    /** Cooldown interval for the knife throwing ability in seconds. */
    private int cooldownTime = 2;
    /** Set the cooldown time for the knife throwing ability.
     * This method is called during the class setup to ensure the cooldown is configured correctly.
     * @param value The cooldown time in seconds.
     */
    private void setCooldownTime(int value) {
        if (value != 0) cooldownTime = value;
    }

    /**
     * Registers the class ability for the Rogue class.
     */
    @Override
    public void classAbility() {
        /* Listener for the knife throwing ability */
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onKnifeThrown(ProjectileLaunchEvent event) {
                if (event.isCancelled()) return; // Check if the event is canceled
                if (!(event.getEntity() instanceof Snowball knife)) return; // Check if the entity is a Snowball (knife)
                if (!(knife.getShooter() instanceof Player player)) return; // Check if the shooter is a Player
                boolean custom =
                        HooksManager.isCustomItem(player.getInventory().getItemInMainHand()) ||
                        HooksManager.isCustomItem(player.getInventory().getItemInOffHand());
                if (custom) {
                    // Check if the player class is Rogue
                    if ((PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.ROGUE)) {
                        player.sendMessage(Component.text(ClassX.getInstance().getLanguageManager().getMessage("class_action_not_allowed"))
                                                    .color(TextColor.color(0xff0000))
                                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.text("Rogue Class")
                                                                         .color(TextColor.color(0x008000)))));
                        event.setCancelled(true);
                        return;
                    }
                    // Check if the Rogue class is disabled (Needed here since knife logic is ItemsAdder responsibility)
                    if (isDisabled()) {
                        event.setCancelled(true);
                        return;
                    }
                    // Check if there is an active cooldown
                    TimerInfo entry = cooldownCache.get(player.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        // The ability is on cooldown, notify the player
                        player.sendMessage(Component.text(ClassX.getInstance().getLanguageManager().getMessage("rogue.knife_cooldown_message"))
                                                    .color(TextColor.color(0xff0000))
                                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.text("Rogue Class")
                                                                         .color(TextColor.color(0x008000)))));
                        event.setCancelled(true);
                    } else {
                        // The ability use is valid
                        cooldownCache.put(player.getUniqueId(), new TimerInfo(cooldownTime));
                    }
                }
            }
        }, ClassX.getInstance());
    }

    /**
     * Sets up the class configurations, including cooldown time for the knife throwing ability.
     * This method is called during the plugin initialization to ensure all configurations are loaded correctly.
     */
    @Override
    public void setUpConfigs() {
        super.setUpConfigs();

        setCooldownTime(ClassX.getInstance().getConfig().getInt("rogue.knife_throw_cooldown", 2));
    }
}

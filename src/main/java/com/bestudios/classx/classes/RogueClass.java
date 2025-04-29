package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.PlayersCache;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.corex.managers.HooksManager;
import com.bestudios.corex.utils.TimerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

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

    private int cooldownTime = 2;
    private void setCooldownTime(int value) {
        if (value != 0) cooldownTime = value;
    }

    @Override
    public void classAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onKnifeThrown(ProjectileLaunchEvent event) {
                if (event.isCancelled()) return;
                if (!(event.getEntity() instanceof Snowball knife)) return;
                if (!(knife.getShooter() instanceof Player player)) return;
                boolean custom =
                        HooksManager.isCustomItem(player.getInventory().getItemInMainHand()) ||
                        HooksManager.isCustomItem(player.getInventory().getItemInOffHand());
                if (custom) {
                    // Check if the player class is Rogue
                    if ((PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.ROGUE)) {
                        player.sendMessage(Component.text("Non hai l'armatura adatta")
                                .color(TextColor.color(0xff0000))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Rogue Class")
                                                .color(TextColor.color(0x008000)))));
                        event.setCancelled(true);
                        return;
                    }
                    // Check if the Rogue class is disabled (Needed here since knife logic is ItemsAdder responsibility)
                    if (RogueClass.getInstance().isDisabled()) {
                        event.setCancelled(true);
                        return;
                    }
                    // Check if there is an active cooldown
                    TimerInfo entry = cooldownCache.get(player.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        player.sendMessage(Component.text("Il coltello è in cooldown")
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

    @Override
    public void setUpConfigs() {
        super.setUpConfigs();

        YamlConfiguration generalConfig = ClassXSettingsManager.getInstance().getConfig();
        setCooldownTime(generalConfig.getInt("rogue_class_knife_throw_cooldown"));
    }
}

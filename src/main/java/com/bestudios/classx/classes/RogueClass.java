package com.bestudios.classx.classes;

import com.bestudios.classx.Classes;
import com.bestudios.classx.ClassX;
import com.bestudios.classx.PlayersCache;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.corex.utils.TimerInfo;
import dev.lone.itemsadder.api.ItemsAdder;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RogueClass extends ClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static ClassType instance = new RogueClass();

    /**
     * Private constructor for the Singleton
     */
    private RogueClass(){
        super("rogue.yml", Classes.ROGUE);
    }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static ClassType getInstance() { return instance; }

    private int cooldownTime = 2;
    private void setCooldownTime(int value) {
        if (value != 0) cooldownTime = value;
    }
    private final Map<UUID, TimerInfo> cooldownCache = new HashMap<>();

    @Override
    public void setClassOnCooldown(Player player) {
        cooldownCache.put(player.getUniqueId(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown()));
    }

    @Override
    public void classAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onKnifeThrown(ProjectileLaunchEvent event) {
                if (event.isCancelled()) return;
                if (!(event.getEntity() instanceof Snowball knife)) return;
                if (!(knife.getShooter() instanceof Player player)) return;
                boolean custom = ItemsAdder.isCustomItem(player.getInventory().getItemInMainHand());
                if (custom) {
                    if ((PlayersCache.getInstance().getCache().get(player.getUniqueId()).getCurrentClass() != Classes.ROGUE)) {
                        player.sendMessage(Component.text("Non hai l'armatura adatta")
                                .color(TextColor.color(0xff0000))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Rogue Class")
                                                .color(TextColor.color(0x008000)))));
                        event.setCancelled(true);
                        return;
                    }
                    if (RogueClass.getInstance().isDisabled()) event.setCancelled(true);
                    TimerInfo entry = cooldownCache.get(player.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        player.sendMessage(Component.text("Il coltello è in cooldown")
                                .color(TextColor.color(0xff0000))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Rogue Class")
                                                .color(TextColor.color(0x008000)))));
                        event.setCancelled(true);
                    } else {
                        cooldownCache.put(player.getUniqueId(), new TimerInfo(cooldownTime));
                    }
                }
            }
        }, ClassX.getInstance());
    }

    @Override
    protected void classConfiguration() {

        YamlConfiguration config = ClassXSettingsManager.getInstance().getConfig();
        setCooldownTime(config.getInt("rogue_class_knife_throw_cooldown"));
    }
}

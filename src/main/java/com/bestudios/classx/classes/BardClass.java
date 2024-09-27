package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.classx.Classes;
import com.bestudios.classx.PlayersCache;
import com.bestudios.classx.util.BardBuffRunnable;
import com.bestudios.classx.util.TaskCancellationEvent;
import com.bestudios.corex.utils.TimerInfo;
import dev.lone.itemsadder.api.ItemsAdder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BardClass extends ClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static ClassType instance = new BardClass();

    /**
     * Private constructor for the Singleton
     */
    private BardClass() { super("bard.yml", Classes.BARD); }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static ClassType getInstance() { return instance; }

    private double buffRange = 20;
    private int defaultBuffDuration = 2;
    private int amplifiedBuffDuration = 10;
    private final Map <UUID, BardBuffRunnable> bardBuffsCache = new HashMap<>();

    private int cooldownTime = 20;
    private final Map<UUID, TimerInfo> cooldownCache = new HashMap<>();

    private final Map<UUID, TimerInfo> classInitialCooldownCache = new HashMap<>();

    @Override
    public void setClassOnCooldown(Player player) {
        classInitialCooldownCache.put(player.getUniqueId(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown()));
        cooldownCache.put(player.getUniqueId(), new TimerInfo(ClassXSettingsManager.getInstance().getClassActivationCooldown()));
    }

    @Override
    public void classDismissed(Player player) {
        cancelTask(player);
    }

    @Override
    public void classAbility() {

        hornHeldAbility();
        hornRightClickAbility();

        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakDeath(EntityDeathEvent event) {
                if (event.isCancelled()) return;
                if (event.getEntity() instanceof Player deadPlayer) {
                    cancelTask(deadPlayer);
                }
            }
        }, ClassX.getInstance());

    }

    private void hornHeldAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onPlayerItemHeld(PlayerItemHeldEvent event) {
                Player player = event.getPlayer();
                ItemStack itemHeld = player.getInventory().getItem(event.getNewSlot());
                cancelTask(player);
                if (itemHeld == null || !(itemHeld.getType().equals(Material.GOAT_HORN))) return;
                if (BardClass.getInstance().isDisabled()) return;
                if (event.isCancelled()) return;
                if (PlayersCache.getInstance().getCache().get(player.getUniqueId()).getCurrentClass() != Classes.BARD) return;
                if (classInitialCooldownCache.get(player.getUniqueId()).isValid()) return;
                selectHorn(player, itemHeld, defaultBuffDuration,1, true);
            }

            @EventHandler(priority = EventPriority.HIGH)
            public void onTaskCancellation(TaskCancellationEvent event) {
                if (event.getReceiver() == BardClass.getInstance()) {
                    cancelTask(event.getPlayer());
                }
            }

        }, ClassX.getInstance());
    }

    public void hornRightClickAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.HIGH)
            public void onHornRightClick(PlayerInteractEvent event) {
                Player player = event.getPlayer();
                ItemStack itemHeld = player.getInventory().getItemInMainHand();
                cancelTask(player);
                if ( !(itemHeld.getType().equals(Material.GOAT_HORN))) return;
                if (PlayersCache.getInstance().getCache().get(player.getUniqueId()).getCurrentClass() != Classes.BARD) return;
                if (BardClass.getInstance().isDisabled()) return;
                if (event.useItemInHand() == Event.Result.DENY) return;
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    TimerInfo entry = cooldownCache.get(player.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        player.sendMessage(Component.text("Il corno è in cooldown")
                                                    .color(TextColor.color(0xff0000))
                                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.text("Bard Class")
                                                                .color(TextColor.color(0x008000)))));
                        return;
                    }
                    cooldownCache.put(player.getUniqueId(), new TimerInfo(cooldownTime));
                    player.setCooldown(Material.GOAT_HORN,cooldownTime * 20);
                    selectHorn(player, itemHeld, amplifiedBuffDuration,2, false);
                }
            }

        }, ClassX.getInstance());
    }

    private void cancelTask(Player player) {
        try {
            bardBuffsCache.get(player.getUniqueId()).cancel();
            bardBuffsCache.remove(player.getUniqueId());
        } catch (Exception ignored) { }
    }
    private void taskProcedure(Player player, PotionEffect effect, boolean refreshable) {
        BardBuffRunnable task = new BardBuffRunnable(player, effect, buffRange);
        if (refreshable) {
            task.runTaskTimer(ClassX.getInstance(), 1, 20L * defaultBuffDuration);
            bardBuffsCache.put(player.getUniqueId(), task);
        } else task.runTask(ClassX.getInstance());
    }

    private void selectHorn(Player player, ItemStack itemHeld, int buffDuration, int buffAmplifier, boolean refreshable) {
        String meta = itemHeld.getItemMeta().toString();

        if (ItemsAdder.getCustomItemName(itemHeld).contains("speed"))
            taskProcedure(player, new PotionEffect(PotionEffectType.SPEED, 20 * buffDuration, speed_amplifiers[buffAmplifier-1]), refreshable);
        else if (ItemsAdder.getCustomItemName(itemHeld).contains("strength"))
            taskProcedure(player, new PotionEffect(PotionEffectType.STRENGTH, 20 * buffDuration, strength_amplifiers[buffAmplifier-1]), refreshable);
        else if (ItemsAdder.getCustomItemName(itemHeld).contains("haste"))
            taskProcedure(player, new PotionEffect(PotionEffectType.HASTE, 20 * buffDuration, haste_amplifiers[buffAmplifier-1]), refreshable);
        else if (ItemsAdder.getCustomItemName(itemHeld).contains("regen"))
            taskProcedure(player, new PotionEffect(PotionEffectType.REGENERATION, 20 * buffDuration, regeneration_amplifiers[buffAmplifier-1]), refreshable);
    }

    private final int[] speed_amplifiers = { 0, 1};
    private final int[] strength_amplifiers = { 0, 1};
    private final int[] haste_amplifiers = { 0, 1};
    private final int[] regeneration_amplifiers = { 0, 1};

    @Override
    protected void classConfiguration() {
        YamlConfiguration config = ClassXSettingsManager.getInstance().getConfig();

        buffRange = config.getInt("bard_class_effect_range");

        defaultBuffDuration = config.getInt("bard_class_default_effect_duration");
        amplifiedBuffDuration = config.getInt("bard_class_amplified_effect_duration");

        cooldownTime = config.getInt("bard_class_horn_usage_cooldown");

        speed_amplifiers[0] = config.getInt("bard_class_speed_effect") - 1;
        speed_amplifiers[1] = config.getInt("bard_class_amplified_speed_effect") - 1;

        strength_amplifiers[0] = config.getInt("bard_class_strength_effect") - 1;
        strength_amplifiers[1] = config.getInt("bard_class_amplified_strength_effect") - 1;

        haste_amplifiers[0] = config.getInt("bard_class_haste_effect") - 1;
        haste_amplifiers[1] = config.getInt("bard_class_amplified_haste_effect") - 1;

        regeneration_amplifiers[0] = config.getInt("bard_class_regeneration_effect") - 1;
        regeneration_amplifiers[1] = config.getInt("bard_class_amplified_regeneration_effect") - 1;
    }

}



package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.caches.PlayersCache;
import com.bestudios.classx.util.BardBuffRunnable;
import com.bestudios.corex.services.HooksManager;
import com.bestudios.corex.caches.SmartCache;
import com.bestudios.corex.basics.TimerInfo;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
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
import org.checkerframework.checker.units.qual.A;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * This class implements the Bard class functionality.
 * It handles the bard's abilities, cooldowns, and buffs applied to players.
 * The class is designed as a Singleton to ensure only one instance exists.
 */
public class BardClass extends RoleClassType {

    /**
     * Instance variable, intended to be used as Singleton reference
     */
    protected static RoleClassType instance = new BardClass();

    /**
     * Private constructor for the Singleton
     */
    private BardClass() { super("bard.yml", RoleClassEnum.BARD); }

    /**
     * The only way to retrieve the class instance
     * @return instance of the class
     */
    public static RoleClassType getInstance() { return instance; }



    /** Cooldown time for the right click ability, expressed in seconds */
    private int rightClickCooldownTime = 20;
    /** Normal ability effect duration, expressed in seconds */
    private int defaultBuffDuration = 2;
    /** Amplified ability effect duration, expressed in seconds */
    private int amplifiedBuffDuration = 10;
    /** Range for the buffs applied by the bard, expressed in blocks */
    private double buffRange = 20;

    /**
     *  Smart cache for the ability runnables
     */
    private final SmartCache<BardBuffRunnable> bardBuffsCache = new SmartCache<>();
    /** List of implemented buffs */
    private final Map<String, PotionEffectType> buffTypes = Map.of(
            "speed", PotionEffectType.SPEED,
            "strength", PotionEffectType.STRENGTH,
            "haste", PotionEffectType.HASTE,
            "regeneration", PotionEffectType.REGENERATION
    );
    /**
     * Amplifiers for the bard's buffs, indexed by 0 for normal and 1 for amplified
     */
    private final Map<String, Vector<Integer>> amplifiersCache = Map.of(
            "speed", new Vector<>(Arrays.asList(0, 1)),
            "strength", new  Vector<>(Arrays.asList(0, 1)),
            "haste", new  Vector<>(Arrays.asList(0, 1)),
            "regeneration", new Vector<>(Arrays.asList(0, 1))
    );

    /**
     * Dismisses the role class for a player.
     * @param player the player whose role class is being dismissed
     */
    @Override
    public void dismissRoleClass(Player player) {
        cancelAbilityTask(player);
    }

    /**
     * Registers the class abilities for the Bard class.
     */
    @Override
    public void classAbility() {
        // Register the horn held ability
        hornHeldAbility();
        // Register the horn right-click ability
        hornRightClickAbility();
        // Register the listener for player death
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onWeakDeath(EntityDeathEvent event) {
                if (event.isCancelled()) return;
                if (event.getEntity() instanceof Player deadPlayer) {
                    // If the player is a bard, cancel their ability task
                    cancelAbilityTask(deadPlayer);
                }
            }

        }, ClassX.getInstance());

    }

    /**
     * Sets up the configurations for the Bard class.
     * This method retrieves configuration values from the YAML file and initializes class variables.
     */
    @Override
    public void setUpConfigs() {
        super.setUpConfigs();

        YamlConfiguration config = ClassX.getInstance().getSettingsManager().getConfig();

        buffRange = config.getInt("bard.effect_range", 20);

        defaultBuffDuration = config.getInt("bard.default_effect_duration", 5);
        amplifiedBuffDuration = config.getInt("bard.amplified_effect_duration", 10);

        rightClickCooldownTime = config.getInt("bard.horn_usage_cooldown", 60);

        // Initialize the amplifiers for the bard's buffs
        for (Map.Entry<String, Vector<Integer>> entry : amplifiersCache.entrySet()) {
            String key = entry.getKey();
            Vector<Integer> amplifiers = entry.getValue();
            amplifiers.set(0, config.getInt("bard." + key + "_effect", 2) - 1);
            amplifiers.set(1, config.getInt("bard.amplified_" + key + "_effect", 3) - 1);
        }
    }

    /**
     *  Internal procedure for correctly dismiss a cached runnable
     *  @param player the player whose ability task is being canceled
     */
    private void cancelAbilityTask(Player player) {
        try {
            bardBuffsCache.remove(player.getUniqueId());
        } catch (Exception ignored) {
            ClassX.getInstance().toLog(
                    "Caught an exception when trying " +
                    "to cancel the bard buff runnable for player " +
                    player.getName(), ClassX.getInstance().isDebugMode());
        }
    }

    /**
     * Listener for the Bard ability expressed when the horn is being held
     */
    private void hornHeldAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onPlayerItemHeld(PlayerItemHeldEvent event) {
                Player player = event.getPlayer();
                ItemStack itemHeld = player.getInventory().getItem(event.getNewSlot());
                cancelAbilityTask(player);
                if (itemHeld == null || !(itemHeld.getType().equals(Material.GOAT_HORN))) return;
                if (BardClass.getInstance().isDisabled()) return;
                if (event.isCancelled()) return;
                if (PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;
                TimerInfo timer = cooldownCache.get(player.getUniqueId());
                if (timer.getDuration() == ClassX.getInstance().getConfig().getInt("class_activation_cooldown", 1) && timer.isValid()) return;
                selectHorn(player, itemHeld, defaultBuffDuration, EFFECT_TYPE.BASE, true);
            }

            @EventHandler(priority = EventPriority.HIGH)
            public void onBardInventoryChange(PlayerInventorySlotChangeEvent event) {
                Player player = event.getPlayer();
                if (PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;
                cancelAbilityTask(player);
            }

        }, ClassX.getInstance());
    }

    /**
     * Listener for the Bard ability expressed when the horn is being right-clicked
     */
    public void hornRightClickAbility() {
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.HIGH)
            public void onHornRightClick(PlayerInteractEvent event) {
                Player player = event.getPlayer();
                ItemStack itemHeld = player.getInventory().getItemInMainHand();
                cancelAbilityTask(player);
                if ( !(itemHeld.getType().equals(Material.GOAT_HORN))) return;
                if (PlayersCache.getInstance().getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.BARD) return;
                if (BardClass.getInstance().isDisabled()) return;
                if (event.useItemInHand() == Event.Result.DENY) return;
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    // Check the cooldown
                    TimerInfo entry = cooldownCache.get(player.getUniqueId());
                    if (entry != null && entry.isValid()) {
                        player.sendMessage(Component.text(ClassX.getInstance().getLanguageManager().getMessage("bard.horn_cooldown_message"))
                                                    .color(TextColor.color(0xff0000))
                                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                                      Component.text("Bard Class")
                                                                                               .color(TextColor.color(0x008000)))));
                        return;
                    }

                    cooldownCache.put(player.getUniqueId(), new TimerInfo(rightClickCooldownTime));
                    player.setCooldown(Material.GOAT_HORN, rightClickCooldownTime * 20);
                    selectHorn(player, itemHeld, amplifiedBuffDuration, EFFECT_TYPE.AMPLIFIED, false);
                }
            }

        }, ClassX.getInstance());
    }

    /**
     * Internal procedure for the correct effect selection before scheduling the dedicated runnable
     * @param player the player to apply the buff to
     * @param itemHeld the item held by the player, which should be a horn
     * @param buffDuration the duration of the buff to apply, expressed in seconds
     * @param effectType the type of effect to apply, either BASE or AMPLIFIED
     * @param refreshable whether the buff is refreshable or not
     */
    private void selectHorn(Player player, ItemStack itemHeld, int buffDuration, EFFECT_TYPE effectType, boolean refreshable) {

        String name = HooksManager.getItemName(itemHeld);

        int amplifierIndex = effectType.equals(EFFECT_TYPE.AMPLIFIED) ? 1 : 0;
        for (Map.Entry<String, PotionEffectType> entry : buffTypes.entrySet()) {
            String key = entry.getKey();
            if (name.contains(key)) {
                PotionEffect potion = new PotionEffect(
                        entry.getValue(),
                        20 * buffDuration,
                        amplifiersCache.get(key).get(amplifierIndex)
                );
                taskProcedure(player, potion, refreshable);
                break;
            }
        }
    }

    /**
     * Internal procedure for the scheduling of a correct Bard Buff Runnable
     * @param player the player to apply the buff to
     * @param effect the potion effect to apply
     * @param refreshable whether the buff is refreshable or not
     */
    private void taskProcedure(Player player, PotionEffect effect, boolean refreshable) {
        BardBuffRunnable task = new BardBuffRunnable(player, effect, buffRange);
        if (refreshable) {
            task.runTaskTimer(ClassX.getInstance(), 1, 20L * defaultBuffDuration);
            bardBuffsCache.put(player.getUniqueId(), task);
        } else task.runTask(ClassX.getInstance());
    }

    enum EFFECT_TYPE {
        BASE,
        AMPLIFIED
    }

}





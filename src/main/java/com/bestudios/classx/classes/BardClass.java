package com.bestudios.classx.classes;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.classx.PlayersCache;
import com.bestudios.classx.util.BardBuffRunnable;
import com.bestudios.corex.managers.HooksManager;
import com.bestudios.corex.utils.SmartCache;
import com.bestudios.corex.utils.TimerInfo;
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

import java.util.Arrays;
import java.util.Vector;

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



    /**
     *  @rightClickCooldownTime  Cooldown time for the right click ability, expressed in seconds
     *  @defaultBuffDuration     Normal ability effect duration, expressed in seconds
     *  @amplifiedBuffDuration   Right-click ability effect duration, expressed in seconds
     *  @buffRange               Normal and right-click abilities applying range, expressed in blocks
     */
    private int rightClickCooldownTime = 20;
    private int defaultBuffDuration = 2;
    private int amplifiedBuffDuration = 10;
    private double buffRange = 20;

    /**
     *  Smart cache for the ability runnables
     */
    private final SmartCache<BardBuffRunnable> bardBuffsCache = new SmartCache<>();

    /**
     *  Amplifiers for the bard ability potion effects
     */
    private final Vector<Integer> speed_amplifiers = new Vector<Integer>(Arrays.asList(0, 1));
    private final Vector<Integer> strength_amplifiers = new Vector<Integer>(Arrays.asList(0, 1));
    private final Vector<Integer> haste_amplifiers = new Vector<Integer>(Arrays.asList(0, 1));
    private final Vector<Integer> regeneration_amplifiers = new Vector<Integer>(Arrays.asList(0, 1));



    @Override
    public void dismissRoleClass(Player player) {
        cancelAbilityTask(player);
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
                    cancelAbilityTask(deadPlayer);
                }
            }

        }, ClassX.getInstance());

    }

    @Override
    public void setUpConfigs() {
        super.setUpConfigs();

        YamlConfiguration config = ClassXSettingsManager.getInstance().getConfig();

        buffRange = config.getInt("bard_class_effect_range");

        defaultBuffDuration = config.getInt("bard_class_default_effect_duration");
        amplifiedBuffDuration = config.getInt("bard_class_amplified_effect_duration");

        rightClickCooldownTime = config.getInt("bard_class_horn_usage_cooldown");

        speed_amplifiers.set( 0, config.getInt("bard_class_speed_effect") - 1);
        speed_amplifiers.set( 1, config.getInt("bard_class_amplified_speed_effect") - 1);

        strength_amplifiers.set( 0, config.getInt("bard_class_strength_effect") - 1);
        strength_amplifiers.set( 1, config.getInt("bard_class_amplified_strength_effect") - 1);

        haste_amplifiers.set( 0, config.getInt("bard_class_haste_effect") - 1);
        haste_amplifiers.set( 1, config.getInt("bard_class_amplified_haste_effect") - 1);

        regeneration_amplifiers.set( 0, config.getInt("bard_class_regeneration_effect") - 1);
        regeneration_amplifiers.set( 1, config.getInt("bard_class_amplified_regeneration_effect") - 1);
    }

    /**
     *  Internal procedure for correctly dismiss a cached runnable
     *  @param player
     */
    private void cancelAbilityTask(Player player) {
        try {
            bardBuffsCache.remove(player.getUniqueId());
        } catch (Exception ignored) {
            ClassX.getInstance().toLog("Caught an exception when trying " +
                    "to cancel the bard buff runnable for player " +
                    player.getName(), debug);
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
                if (timer.getDuration() == ClassXSettingsManager.getInstance().getClassActivationCooldown() && timer.isValid()) return;
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
                        player.sendMessage(Component.text("Il corno è in cooldown")
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
     * @param player
     * @param itemHeld
     * @param buffDuration
     * @param effectType
     * @param refreshable
     */
    private void selectHorn(Player player, ItemStack itemHeld, int buffDuration, EFFECT_TYPE effectType, boolean refreshable) {

        String name = HooksManager.getItemName(itemHeld);
        PotionEffectType effect = null;
        int amplifierIndex = effectType.equals(EFFECT_TYPE.AMPLIFIED) ? 1 : 0;
        Vector<Integer> amplifierCache = null;

        if (name.contains("speed")) {
            effect = PotionEffectType.SPEED;
            amplifierCache = speed_amplifiers;
        } else if (name.contains("strength")) {
            effect = PotionEffectType.STRENGTH;
            amplifierCache = strength_amplifiers;
        } else if (name.contains("haste")) {
            effect = PotionEffectType.HASTE;
            amplifierCache = haste_amplifiers;
        } else if (name.contains("regen")) {
            effect = PotionEffectType.REGENERATION;
            amplifierCache = regeneration_amplifiers;
        }

        if (effect != null && amplifierCache != null) {
            PotionEffect potion = new PotionEffect(effect, 20 * buffDuration, amplifierCache.get(amplifierIndex));
            taskProcedure(player, potion, refreshable);
        }
    }

    /**
     * Internal procedure for the scheduling of a correct Bard Buff Runnable
     * @param player
     * @param effect
     * @param refreshable
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





package com.bestudios.classx.util;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.ClassXSettingsManager;
import com.bestudios.corex.CoreX;
import com.bestudios.corex.utils.TimerInfo;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomFoodControl implements Listener {

    private final boolean debug;
    private final static CustomFoodControl instance = new CustomFoodControl();
    /* Private Constructor */
    private CustomFoodControl() {
        debug = ClassX.getInstance().isDebugMode();
    }
    public static CustomFoodControl getInstance() { return instance; }

    private final Map<UUID, TimerInfo> cookingItemsCDMap = new HashMap<>(ClassX.PREDICTED_MAX_PLAYERS);
    private final Map<UUID, TimerInfo> potionItemsCDMap = new HashMap<>(ClassX.PREDICTED_MAX_PLAYERS);

    private int foodCD = 300;

    private void setFoodCD(int value) { if(value != 0) foodCD = value; }

    private int potionCD = 300;

    private void setPotionCD(int value) { if(value != 0) potionCD = value; }

    private String itemToString(ItemStack item) {
        if (ItemsAdder.isCustomItem(item))
            return ItemsAdder.getCustomItemName(item);
        else
            return item.getType().getKey().toString();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {

        if (CoreX.getInstance().getDependencyManager().isItemsAdderLoaded()) return;

        ClassX.getInstance().toLog("ItemsAdder is loaded, registering the CustomFoodControl", debug);

        setFoodCD( ClassXSettingsManager.getInstance().getConfig().getInt( "custom_food_cooldown" ));
        ClassX.getInstance().toLog("Custom food cooldown detected and set up", debug);

        setPotionCD( ClassXSettingsManager.getInstance().getConfig().getInt( "custom_potion_cooldown" ));
        ClassX.getInstance().toLog("Custom potion cooldown detected and set up", debug);
        ClassX.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onItemConsumed(PlayerItemConsumeEvent event) {
                Player player = event.getPlayer();
                String itemName = itemToString(event.getItem());
                if ( itemName.startsWith("cooking:") ) {
                    TimerInfo entry = cookingItemsCDMap.get(player.getUniqueId());
                    if ((entry != null) && (entry.isValid())) {
                        player.sendMessage(Component.text("Questo cibo è in cooldown!")
                                .color(TextColor.color(0xFF0000))
                                .decoration(TextDecoration.BOLD, true)
                        );
                        event.setCancelled(true);
                        ClassX.getInstance().toLog("A food cooldown has been detected for " + player.getName(), debug);
                        return;
                    } cookingItemsCDMap.put(player.getUniqueId(),new TimerInfo(foodCD));
                } else if ( itemName.startsWith("potion:")) {
                    TimerInfo entry = potionItemsCDMap.get(player.getUniqueId());
                    if ((entry != null) && (entry.isValid())) {
                        player.sendMessage(Component.text("Questa pozione è in cooldown!")
                                .color(TextColor.color(0xFF0000))
                                .decoration(TextDecoration.BOLD, true)
                        );
                        event.setCancelled(true);
                        ClassX.getInstance().toLog("A potion cooldown has been detected for " + player.getName(), debug);
                        return;
                    } potionItemsCDMap.put(player.getUniqueId(),new TimerInfo(potionCD));
                }
            }
        }, ClassX.getInstance());
    }
}

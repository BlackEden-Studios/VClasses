package com.bestudios.vclasses.classes;

import com.bestudios.fulcrum.api.util.TimerInfo;
import com.bestudios.vclasses.data.ClassLoadingException;
import com.bestudios.vclasses.data.PlayersCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Rogue Class
 *
 * @version 0.12.0
 */
public class RogueClass extends RoleClassType {

  /** Instance variable, intended to be used as a Singleton reference */
  protected static RogueClass instance = new RogueClass();
  /** Class ability cooldown timer */
  private int abilityCooldown;

  /**
   * The only way to retrieve the class instance
   * @return instance of the class
   */
  public static RogueClass getInstance() {
    return instance;
  }

  /** Private constructor for the Singleton */
  private RogueClass(){
    super("rogue.yml", RoleClassEnum.ROGUE);

    this.abilityCooldown = 2;
  }

  /**
   * Sets the value of the class ability cooldown.
   * @param value The new value of the ability cooldown.
   */
  private void setAbilityCooldown(int value) {
    if (value > 0) this.abilityCooldown = value;
  }

  @Override
  protected void additionalSetup() throws ClassLoadingException {
    FileConfiguration config = this.plugin.getConfig();
    // Ability cooldown
    setAbilityCooldown(config.getInt("class.rogue.knife.throw_cooldown", 2));
  }

  @Override
  protected void abilityConfiguration() throws ClassLoadingException{
    // Check if the service is present
    if (customItemsService == null)
      throw new ClassLoadingException("CustomItemsService not found. Rogue Class will not work properly.");

    // Register the listener
    this.plugin.getServer().getPluginManager().registerEvents(new Listener() {

      /*
       * Event that checks the throw of a knife
       */
      @EventHandler(priority = EventPriority.HIGH)
      public void onKnifeThrown(ProjectileLaunchEvent event) {
        // Base checks
        if (event.isCancelled()) return;
        // The thrown entity must be a snowball
        if (!(event.getEntity()  instanceof Snowball knife)) return;
        if (!(knife.getShooter() instanceof Player player)) return;
        // The thrown item must be custom
        if (customItemsService.isCustomItem(player.getInventory().getItemInMainHand()) ||
            customItemsService.isCustomItem(player.getInventory().getItemInOffHand())) return;
        // Check if the player is a rogue
        if (PlayersCache.getPlayerCache(player.getUniqueId()).getCurrentClass() != RoleClassEnum.ROGUE || // Not Rogue
            !RogueClass.getInstance().isEnabled()                                                      || // Not enabled
            TimerInfo.isValid(cooldownCache.get(player.getUniqueId()))                                    // Not valid
        ) {
          player.sendMessage(rogueMessage());
          event.setCancelled(true);
          return;
        }
        // The ability usage is valid
        cooldownCache.put(player.getUniqueId(), new TimerInfo(abilityCooldown));
      }

    }, this.plugin);
  }

  /**
   * Returns a component with the rogue class message.
   *
   * @return A component with the rogue class message.
   */
  private @NotNull Component rogueMessage() {
    return Component.text(plugin.getLanguageConfiguration().getString("class.action.not_allowed", ""))
                    .color(TextColor.color(0xff0000))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                      Component.text("Rogue Class")
                                                               .color(TextColor.color(0x008000))
                                                     )
                    );
  }
}

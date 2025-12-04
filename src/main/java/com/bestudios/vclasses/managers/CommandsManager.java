package com.bestudios.vclasses.managers;

import com.bestudios.fulcrum.api.command.CommandTree;
import com.bestudios.fulcrum.api.command.CommandUtils;
import com.bestudios.vclasses.VClasses;
import com.bestudios.vclasses.classes.RoleClassEnum;
import com.bestudios.fulcrum.api.command.CommandWrapper;

import java.util.List;


/**
 *  CommandsManager is responsible for creating and managing commands related to the VClasses plugin.
 *  It provides implementations for enabling and disabling role classes
 */
public class CommandsManager {

  public static CommandWrapper getHelpCommand() {
    return new CommandWrapper.Builder()
            .path("help")
            .action(ctx -> {
              ctx.sender().sendMessage("""
              §6=== ClassX Commands ===
              §e/classx help §7- Show this help message
              §e/classx enable <class> §7- Enable a role class
              §e/classx disable <class> §7- Disable a role class
              """);
              return true;
            })
            .build();
  }

  public static CommandWrapper getEnableCommand() {
    return new CommandWrapper.Builder()
            .path("enable")
            .action(enableClassAction(true))
            .tabCompleter(classTabCompleter)
            .build();
  }

  public static CommandWrapper getDisableCommand() {
    return new CommandWrapper.Builder()
            .path("disable")
            .action(enableClassAction(false))
            .tabCompleter(classTabCompleter)
            .build();
  }

  private static CommandTree.CommandAction enableClassAction(boolean value) {
    return ctx -> {
      RoleClassEnum classEnum = RoleClassEnum.getClassByName(ctx.remainingArgs().getFirst());
      if (classEnum.equals(RoleClassEnum.NONE)) ctx.sender().sendMessage("Invalid class name");
      else {
        VClasses.implementedClasses.get(classEnum).enableClass(value);
        ctx.sender().sendMessage("You have enabled the " + classEnum + " class!");
      }
      return true;
    };
  }

  private static final CommandTree.TabCompleteFunction classTabCompleter = ctx -> {
    if (ctx.remainingArgs().size() <= 1) {
      String partial = !ctx.remainingArgs().isEmpty() ? ctx.remainingArgs().getFirst() : "";
      return CommandUtils.filterCompletions(VClasses.classNames, partial);
    }
    return List.of();
  };

}
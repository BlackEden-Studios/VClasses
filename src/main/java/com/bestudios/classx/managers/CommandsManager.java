package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;
import com.bestudios.classx.classes.RoleClassEnum;
import com.bestudios.corex.utils.CommandWrapper;

import java.util.List;


/**
 *  CommandsManager is responsible for creating and managing commands related to the ClassX plugin.
 *  It provides implementations for enabling and disabling role classes,
 */
public class CommandsManager {

    private final CommandWrapper wrapper;

    public CommandsManager() {
        wrapper = new CommandWrapper.Builder(
                "classx.use",
                "Usage: /classx <command> [args...]"
                )
                // Simple commands
                .command("help", this::handleHelp)

                .command("enable", ctx -> {
                    RoleClassEnum classEnum = RoleClassEnum.getClassByName(ctx.remainingArgs()[0]);
                    if (classEnum.equals(RoleClassEnum.NONE)) ctx.sender().sendMessage("Invalid class name!");
                    else {
                        ClassX.implementedClasses.get(classEnum).enableClass();
                        ctx.sender().sendMessage("You have enabled the " + classEnum + " class!");
                    }
                    return true;
                })

                .command("disable", ctx -> {
                    RoleClassEnum classEnum = RoleClassEnum.getClassByName(ctx.remainingArgs()[0]);
                    if (classEnum.equals(RoleClassEnum.NONE)) ctx.sender().sendMessage("Invalid class name!");
                    else {
                        ClassX.implementedClasses.get(classEnum).disableClass();
                        ctx.sender().sendMessage("You have disabled the " + classEnum + " class!");
                    }
                    return true;
                })

                // Build the command wrapper
                .build();

        wrapper.registerTabCompleter("enable", ctx -> {
            if (ctx.remainingArgs().length <= 1) {
                String partial = ctx.remainingArgs().length > 0 ? ctx.remainingArgs()[0] : "";
                return CommandWrapper.filterCompletions(ClassX.classNames, partial);
            }
            return List.of();
        });

        wrapper.registerTabCompleter("disable", ctx -> {
            if (ctx.remainingArgs().length <= 1) {
                String partial = ctx.remainingArgs().length > 0 ? ctx.remainingArgs()[0] : "";
                return CommandWrapper.filterCompletions(ClassX.classNames, partial);
            }
            return List.of();
        });
    }

    private boolean handleHelp(CommandWrapper.CommandContext context) {
        context.sender().sendMessage("""
                §6=== ClassX Commands ===
                §e/classx help §7- Show this help message
                §e/classx enable <class> §7- Enable a role class
                §e/classx disable <class> §7- Disable a role class
                """);
        return true;
    }

    public CommandWrapper getWrapper() {
        return wrapper;
    }
}
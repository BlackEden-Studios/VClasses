package com.bestudios.classx.managers;

import com.bestudios.classx.ClassX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultLanguageManager {

    public static void createDefaultConfig() {
        Path languagesDir = Paths.get(String.valueOf(ClassX.getInstance().getDataFolder()), "languages");
        Path langFile = languagesDir.resolve("en.yml");

        try {
            // Ensure directory exists
            Files.createDirectories(languagesDir);

            // Create file if it doesn't exist
            if (!Files.exists(langFile)) {
                String defaultYml = """
                class_activation_success: Class abilities are now active!
                class_action_not_allowed: You don't wear the right armor
                bard:
                  horn_cooldown_message: Horn is in cooldown
                rogue:
                  knife_cooldown_message: Knife is in cooldown
                """;

                Files.writeString(langFile, defaultYml);
                System.out.println("Created default en.yml");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create language file", e);
        }
    }

}

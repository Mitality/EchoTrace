package echotrace.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.tchristofferson.configupdater.ConfigUpdater;
import echotrace.Main;
import echotrace.config.Config;
import echotrace.config.Lang;
import echotrace.core.TraceManager;
import echotrace.core.TraceRenderer;
import echotrace.util.Logger;
import echotrace.util.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ReloadCommand {

    public static int execute(CommandContext<CommandSourceStack> ctx) {

        try {

            // Reload and update config
            Main.getInstance().saveDefaultConfig();
            File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
            ConfigUpdater.update(Main.getInstance(), "config.yml", configFile);
            Main.getInstance().reloadConfig();

            // Load config internally
            Config.load(Main.getInstance().getConfig());

            // Ensure language directory exists
            File languageDir = new File(Main.getInstance().getDataFolder(), "language");
            if (!languageDir.exists()) languageDir.mkdirs();

            // Save and update language files
            for (String langFileName : Main.getLanguages()) {
                if (!new File(languageDir, langFileName).exists()) Main.getInstance().saveResource("language/" + langFileName, false);
                ConfigUpdater.update(Main.getInstance(), "language/" + langFileName, new File(languageDir, langFileName));
            }

            // Get the selected language file
            File languageFile = new File(languageDir, Config.language + ".yml");
            if (!languageFile.exists()) {
                Logger.logErr("Language " + Config.language + " doesn't exist in your language folder! Defaulting to English (en).");
                languageFile = new File(languageDir, "en.yml");
                if (!languageFile.exists()) Main.getInstance().saveResource("language/en.yml", false); // Should not be necessary
            }

            // Load language configuration
            FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);

            // Load language internally
            Lang.load(languageConfig);

            // Reset heartbeat
            TraceManager.startHeartbeat(); // async
            TraceRenderer.startHeartbeat(); // sync

            MessageUtils.notifySender(ctx.getSource().getSender(), Config.prefix + Lang.echotrace_reload_success);
            return SINGLE_SUCCESS;
        } catch (Exception e) {
            MessageUtils.notifySender(ctx.getSource().getSender(), Config.prefix + Lang.echotrace_reload_fail);
            return 0;
        }

    }
}

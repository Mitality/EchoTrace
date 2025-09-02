package echoTrace.commands.subcommands;

import com.tchristofferson.configupdater.ConfigUpdater;
import echoTrace.Main;
import echoTrace.commands.SubCommand;
import echoTrace.config.Config;
import echoTrace.config.Lang;
import echoTrace.util.Logger;
import echoTrace.util.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

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

            MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_reload_success);
        } catch (Exception e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_reload_fail);
            Logger.logErr(e);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "echotrace.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}

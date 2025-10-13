package echotrace;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.tchristofferson.configupdater.ConfigUpdater;
import echotrace.commands.CommandManager;
import echotrace.config.Config;
import echotrace.config.Lang;
import echotrace.core.TraceManager;
import echotrace.core.TraceRenderer;
import echotrace.listeners.UpdateNotifyListener;
import echotrace.util.Logger;
import echotrace.util.UpdateChecker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static List<String> languages;
    private static BukkitAudiences adventure;
    private static TaskScheduler scheduler;

    @Override
    public void onLoad() {
        instance = this;
        languages = new ArrayList<>();
    }

    @Override
    public void onEnable() {

        // Load adventure
        adventure = BukkitAudiences.create(this);

        // Load scheduler
        scheduler = UniversalScheduler.getScheduler(this);

        // Reload and update config
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
        } catch (IOException e) {
            Logger.logErr(e);
        }
        reloadConfig();

        // Load config internally
        Config.load(getConfig());

        // Ensure language directory exists
        File languageDir = new File(getDataFolder(), "language");
        if (!languageDir.exists()) languageDir.mkdirs();

        // Filter out language files from the plugins JarFile
        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("language/") && entry.getName().endsWith(".yml")) {
                    String langFileName = entry.getName().replace("language/", "");
                    languages.add(langFileName);

                    // Save and update language files
                    if (!new File(languageDir, langFileName).exists()) saveResource("language/" + langFileName, false);
                    ConfigUpdater.update(this, "language/" + langFileName, new File(languageDir, langFileName));
                }
            }
        } catch (IOException e) {
            Logger.logErr("Failed to update language files: " + e.getMessage());
            Logger.logErr(e);
        }

        // Get the selected language file
        File languageFile = new File(languageDir, Config.language + ".yml");
        if (!languageFile.exists()) {
            Logger.logErr("Language " + Config.language + " doesn't exist in your language folder! Defaulting to English (en).");
            languageFile = new File(languageDir, "en.yml");
            if (!languageFile.exists()) saveResource("language/en.yml", false); // Should not be necessary
        }

        // Load language configuration
        FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Load language internally
        Lang.load(languageConfig);

        // Register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(CommandManager.echotrace(), List.of("et"));
            event.registrar().register(CommandManager.trace());
        });

        // Start heartbeat
        TraceManager.startHeartbeat(); // async
        TraceRenderer.startHeartbeat(); // sync

        // Check for updates
        UpdateChecker updateChecker = new UpdateChecker("EchoTrace", "echotrace", getDescription().getVersion()).checkNow();
        if (Config.update_check_interval > 0) updateChecker.checkEveryXHours(Config.update_check_interval);
        Bukkit.getPluginManager().registerEvents(new UpdateNotifyListener(updateChecker), this);

        // Metrics - Can be disabled in plugins/bStats/config.yml
        Metrics metrics = new Metrics(this, 27144);
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public static @NotNull BukkitAudiences getAdventure() {
        if (adventure == null) throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        return adventure;
    }

    public static Main getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public static List<String> getLanguages() {
        return languages;
    }

}

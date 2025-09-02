package echotrace.util;

import echotrace.Main;
import echotrace.config.Config;
import io.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    /**
     * Notifies the given player via chat, title, or actionbar
     * @param player The player that should be notified
     * @param message The message to send to the player
     */
    public static void notifyPlayer(Player player, String message) {

        if (message.substring(Config.prefix.length()).trim().isEmpty()) return;
        Audience audience = Main.getAdventure().player(player);

        if (message.trim().toUpperCase().startsWith("ACTIONBAR:")) {
            String actionbarText = message.trim().substring(10).trim();
            Component actionbarMessage = ColorParser.of(actionbarText)
                    .parsePAPIPlaceholders(player).parseLegacy().build();
            audience.sendActionBar(actionbarMessage);
        }

        else if (message.trim().toUpperCase().startsWith("TITLE:")) {
            String[] parts = message.trim().substring(6).split(";", 2);
            Component title = (parts.length > 0) ? ColorParser.of(parts[0].trim()).
                    parsePAPIPlaceholders(player).parseLegacy().build() : Component.empty();
            Component subtitle = (parts.length > 1) ? ColorParser.of(parts[1].trim()).
                    parsePAPIPlaceholders(player).parseLegacy().build() : Component.empty();
            audience.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
        }

        else {
            Component chatMessage = ColorParser.of(message)
                    .parsePAPIPlaceholders(player).parseLegacy().build();
            audience.sendMessage(chatMessage);
        }
    }

    /**
     * Notifies the console, effectively logging something there
     * Applies MiniMessage and legacy text formatting, but PlaceholderAPI
     * placeholders and title/actionbar prefixes are not supported here
     * @param message The message to send to the console
     */
    public static void notifyConsole(String message) {
        if (message.substring(Config.prefix.length()).trim().isEmpty()) return;
        Audience audience = Main.getAdventure().console();
        Component chatMessage = ColorParser.of(message).parseLegacy().build();
        audience.sendMessage(chatMessage);
    }

    /**
     * Notifies the given CommandSender via chat, title, or actionbar
     * Messages to the console always use 'chat' ignoring prefixes
     * @param sender A CommandSender (player or console)
     * @param message The message to send to the player
     */
    public static void notifySender(CommandSender sender, String message) {
        if (sender instanceof Player) {
            notifyPlayer((Player) sender, message);
        } else {
            notifyConsole(message);
        }
    }

}

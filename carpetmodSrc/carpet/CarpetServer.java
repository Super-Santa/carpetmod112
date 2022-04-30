package carpet;

import carpet.commands.CommandCarpet;
import carpet.commands.CommandPing;
import carpet.settings.SettingsManager;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

public class CarpetServer {

    public static final Random rand = new Random();
    public static MinecraftServer minecraftServer;
    public static SettingsManager settingsManager;

    static {
        SettingsManager.parseSettingsClass(CarpetSettings.class);
    }

    public static void onGameStarted() {

    }

    public static void onServerLoaded(MinecraftServer server) {
        settingsManager = new SettingsManager(server);
    }

    public static void onServerLoadedWorlds(MinecraftServer server) {

    }

    public static void tick(MinecraftServer server) {

    }

    public static void registerCarpetCommands(CommandHandler handler) {
        handler.registerCommand(new CommandCarpet());
        handler.registerCommand(new CommandPing());

    }

    public static void onPlayerLoggedIn(EntityPlayerMP player) {

    }

    public static void onPlayerLoggedOut(EntityPlayerMP player) {

    }

    public static void onServerClosed(MinecraftServer server) {

    }

    public static void onServerDoneClosing(MinecraftServer server) {

    }
}
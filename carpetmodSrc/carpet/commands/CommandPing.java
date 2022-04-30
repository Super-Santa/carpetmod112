package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandPing extends CarpetCommandBase {

    @Override
    public String getName()
    {
        return "ping";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/ping";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!CarpetSettings.CommandPing)
            return;

        if (sender instanceof EntityPlayerMP) {
            int ping = ((EntityPlayerMP) sender).ping;
            Messenger.m(sender, "w Your ping is: ", String.format("%s %d",  Messenger.heatmap_color(ping, 250), ping), "w ms");
        }
        else {
            throw new CommandException("Only a player can have a ping!");
        }
    }
}

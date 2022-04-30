package carpet.commands;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.settings.ParsedRule;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CommandCarpet extends CarpetCommandBase {

    @Override
    public String getName() {
        return "carpet";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "carpet <rule> <value>";
    }

    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (CarpetServer.settingsManager.locked)
            return;

        // List Rules
        if (args.length == 0) {
            listAllSettings(sender);
        }

        // List All Rules
        if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
            listSettings(sender, "All CarpetMod Settings", SettingsManager.getRules());
        }

        // List Default settings
        if (args.length == 2 && "list".equalsIgnoreCase(args[0]) && "defaults".equalsIgnoreCase(args[1])) {
            listSettings(sender, "Current CarpetMod Startup Settings from carpet.conf", CarpetServer.settingsManager.findStartupOverrides());
        }

        // List By Tags
        if (args.length == 2 && "list".equalsIgnoreCase(args[0]) && !"defaults".equalsIgnoreCase(args[1])) {
            listSettings(sender, String.format("CarpetMod Settings matching \"%s\"", args[1]), CarpetServer.settingsManager.getRulesMatching(args[1]));
        }

        // Get rule
        if (args.length == 1 && !"list".equalsIgnoreCase(args[0])) {
            displayRuleMenu(sender, getRule(sender, args[0]));
        }

        // Modify rule
        if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"defaults".equalsIgnoreCase(args[1]) && !"setDefault".equalsIgnoreCase(args[0]) && !"removeDefault".equalsIgnoreCase(args[0])) {
            setRule(sender, getRule(sender, args[0]), args[1]);
        }

        // SetDefault
        if (args.length == 3 && "setDefault".equalsIgnoreCase(args[0])) {
            setDefault(sender, getRule(sender, args[1]), args[2]);
        }

        // RemoveDefault
        if (args.length == 2 && "removeDefault".equalsIgnoreCase(args[0])) {
            removeDefault(sender, getRule(sender, args[1]));
        }
    }

    private static ParsedRule<?> getRule(ICommandSender sender, String ctx) {
        ParsedRule<?> rule = SettingsManager.getRule(ctx);

        if (rule == null) {
            Messenger.m(sender, "rb Unkown rule: " + ctx);
            return null;
        }
        return rule;
    }

    private static int displayRuleMenu(ICommandSender source, ParsedRule<?> rule) {

        if (rule == null)
            return 0;

        EntityPlayer player;
        try {
            player = (EntityPlayerMP) source.getCommandSenderEntity();
        } catch (Exception e) {
            Messenger.m(source, "w " + rule.name + " is set to: ", "wb " + rule.getAsString());
            return 1;
        }

        Messenger.m(source, "");
        Messenger.m(source, "wb " + rule.name, "!/carpet " + rule.name, "^g refresh");
        Messenger.m(source, "w " + rule.description);

        rule.extraInfo.forEach(s -> Messenger.m(player, "g " + s));

        List<ITextComponent> tags = new ArrayList<>();
        tags.add(Messenger.c("w Tags: "));
        for (String t : rule.categories) {
            tags.add(Messenger.c("c [" + t + "]", "^g list all " + t + " settings", "!/carpet list " + t));
            tags.add(Messenger.c("w , "));
        }
        tags.remove(tags.size() - 1);
        Messenger.m(player, tags.toArray(new Object[0]));

        Messenger.m(player, "w Current value: ", String.format("%s %s (%s value)", rule.getBoolValue() ? "lb" : "nb", rule.getAsString(), rule.isDefault() ? "default" : "modified"));
        List<ITextComponent> options = new ArrayList<>();
        options.add(Messenger.c("w Options: ", "y [ "));
        for (String o : rule.options) {
            options.add(Messenger.c(makeSetRuleButton(rule, o, false)));
            options.add(Messenger.c("w  "));
        }
        options.remove(options.size() - 1);
        options.add(Messenger.c("y  ]"));
        Messenger.m(player, options.toArray(new Object[0]));

        return 1;
    }

    private static int setRule(ICommandSender source, ParsedRule<?> rule, String newValue) {

        if (rule == null)
            return 0;

        if (rule.set(source, newValue) != null) {
            Messenger.m(source, "w " + rule.toString() + ", ", "c [change permanently?]",
                    "^w Click to keep the settings in carpet.conf to save across restarts",
                    "?/carpet setDefault " + rule.name + " " + rule.getAsString());
        }
        return 1;
    }

    private static int setDefault(ICommandSender source, ParsedRule<?> rule, String defaultValue) {
        if (rule == null)
            return 0;

        if (CarpetServer.settingsManager.setDefaultRule(source, rule.name, defaultValue))
            Messenger.m(source, "gi rule " + rule.name + " will now default to " + defaultValue);
        return 1;
    }

    private static int removeDefault(ICommandSender source, ParsedRule<?> rule) {
        if (rule == null)
            return 0;

        if (CarpetServer.settingsManager.removeDefaultRule(source, rule.name))
            Messenger.m(source, "gi rule " + rule.name + " defaults to Vanilla");
        return 1;
    }

    private static ITextComponent displayInteractiveSetting(ParsedRule<?> e) {
        List<Object> args = new ArrayList<>();
        args.add("w - " + e.name + " ");
        args.add("!/carpet " + e.name);
        args.add("^y " + e.description);
        for (String option : e.options) {
            args.add(makeSetRuleButton(e, option, true));
            args.add("w  ");
        }
        args.remove(args.size() - 1);
        return Messenger.c(args.toArray(new Object[0]));
    }

    private static ITextComponent makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets) {
        String style = rule.isDefault() ? "g" : (option.equalsIgnoreCase(rule.defaultAsString) ? "y" : "e");
        if (option.equalsIgnoreCase(rule.defaultAsString))
            style = style + "b";
        else if (option.equalsIgnoreCase(rule.getAsString()))
            style = style + "u";
        String baseText = style + (brackets ? " [" : " ") + option + (brackets ? "]" : "");
        if (CarpetServer.settingsManager.locked)
            return Messenger.c(baseText, "^g Settings are locked");
        if (option.equalsIgnoreCase(rule.getAsString()))
            return Messenger.c(baseText);
        return Messenger.c(baseText, "^g Switch to " + option, "?/carpet " + rule.name + " " + option);
    }

    private static int listSettings(ICommandSender sender, String title, Collection<ParsedRule<?>> settings_list) {

        if (sender instanceof EntityPlayerMP) {
            Messenger.m(sender, String.format("wb %s", title));
            settings_list.forEach(e -> Messenger.m(sender, displayInteractiveSetting(e)));

        } else {
            Messenger.m(sender, String.format("wb %s", title));
            settings_list.forEach(e -> Messenger.m(sender, displayInteractiveSetting(e)));
        }
        return 1;
    }

    private static int listAllSettings(ICommandSender sender) {
        listSettings(sender, "Current CarpetMod Settings", CarpetServer.settingsManager.getNonDefault());

        Messenger.m(sender, "Carpet Mod version: " + CarpetSettings.carpetVersion);
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        List<Object> tags = new ArrayList<>();
        tags.add("w Browse Categories:\n");
        for (String t : SettingsManager.getCategories()) {
            tags.add("c [" + t + "]");
            tags.add("^g list all " + t + " settings");
            tags.add("!/carpet list " + t);
            tags.add("w  ");
        }
        tags.remove(tags.size() - 1);
        Messenger.m(player, tags.toArray(new Object[0]));
        return 1;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {

        // Options for 1st Arg
        if (args.length == 1) {
            List<String> options = SettingsManager.getRules().stream().map(r -> r.name).collect(Collectors.toList());
            options.add("list");
            options.add("setDefault");
            options.add("removeDefault");
            return getListOfStringsMatchingLastWord(args, options);
        }

        // List Options
        if (args.length == 2 && "list".equalsIgnoreCase(args[0])) {
            List<String> options = new ArrayList<>((Collection) SettingsManager.getCategories());
            options.add("defaults");
            return getListOfStringsMatchingLastWord(args, options);
        }

        // Modify Rule
        if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"defaults".equalsIgnoreCase(args[1]) && !"setDefault".equalsIgnoreCase(args[0]) && !"removeDefault".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, getRule(sender,args[0]).options);
        }

        // SetDefault
        if (args.length == 2 && "setDefault".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, SettingsManager.getRules().stream().map(r -> r.name).collect(Collectors.toList()));
        }

        if (args.length == 3 && "setDefault".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, getRule(sender,args[1]).options);
        }

        // RemoveDefault
        if (args.length == 2 && "removeDefault".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, SettingsManager.getRules().stream().map(r -> r.name).collect(Collectors.toList()));
        }

        return Collections.<String>emptyList();
    }
}
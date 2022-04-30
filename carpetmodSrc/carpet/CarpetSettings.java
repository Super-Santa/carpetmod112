package carpet;

import carpet.CarpetServer;
import carpet.settings.Rule;
import carpet.utils.Messenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.*;

public class CarpetSettings {

    public static final String carpetVersion = "v0.0.1-alpha";
    public static Logger LOG = LogManager.getLogger();

    @Rule(
            desc = "Get the ping of a player",
            category = COMMANDS
    )
    public static boolean CommandPing = true;
}

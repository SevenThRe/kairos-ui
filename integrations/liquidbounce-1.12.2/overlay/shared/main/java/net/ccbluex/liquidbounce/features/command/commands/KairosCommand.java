/*
 * Kairos UI integration for the GPL-3.0 LiquidBounce 1.12.2 port.
 */
package net.ccbluex.liquidbounce.features.command.commands;

import dev.kairos.ui.liquidbounce.KairosScreen;
import java.util.Collections;
import java.util.List;
import net.ccbluex.liquidbounce.features.command.Command;
import net.minecraft.client.Minecraft;

public final class KairosCommand extends Command {
    public KairosCommand() {
        super("kairos", "kgui");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1 || (args.length == 2 && "gui".equalsIgnoreCase(args[1]))) {
            Minecraft.getMinecraft().displayGuiScreen(new KairosScreen());
            return;
        }
        chatSyntax("kairos gui");
    }

    @Override
    public List<String> tabComplete(String[] args) {
        if (args.length == 1 && "gui".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("gui");
        }
        return Collections.emptyList();
    }
}


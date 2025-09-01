package net.petemc.hardcorelite.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands {

    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register(SetCurrentHearts::register);
    }
}

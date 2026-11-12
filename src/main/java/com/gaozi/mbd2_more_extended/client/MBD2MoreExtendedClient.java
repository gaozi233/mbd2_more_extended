package com.gaozi.mbd2_more_extended.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * 客户端入口：注册设置界面的指令。
 */
public final class MBD2MoreExtendedClient {

    private MBD2MoreExtendedClient() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(MBD2MoreExtendedClient::registerCommands);
    }

    private static void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mbd2_more_extended")
                        .then(Commands.literal("config").executes(context -> {
                            Minecraft.getInstance().execute(MBD2MoreExtendedSettingsDialog::open);
                            return 1;
                        }))
        );
    }
}

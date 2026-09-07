package ru.fiw.proxyserver.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fiw.proxyserver.Config;
import ru.fiw.proxyserver.GuiProxy;
import ru.fiw.proxyserver.Proxy;
import ru.fiw.proxyserver.ProxyServer;

@Mixin(JoinMultiplayerScreen.class)
public class MultiplayerScreenOpen {

    @Unique
    private static final int BUTTON_WIDTH = 120;
    @Unique
    private static final int BUTTON_HEIGHT = 20;
    @Unique
    private static final int BUTTON_MARGIN_RIGHT = 5;
    @Unique
    private static final int BUTTON_Y = 5;

    @Unique
    private static final String DEFAULT_ACCOUNT_KEY = "";

    @Inject(method = "init()V", at = @At("TAIL"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        JoinMultiplayerScreen screen = (JoinMultiplayerScreen) (Object) this;

        updateProxyForCurrentPlayer();
        createAndAddProxyButton(screen);
    }

    @Unique
    private void updateProxyForCurrentPlayer() {
        String playerName = Minecraft.getInstance().getUser().getName();
        if (!playerName.equals(Config.lastPlayerName)) {
            Config.lastPlayerName = playerName;

            Proxy userProxy = Config.accounts.getOrDefault(
                    playerName, 
                    Config.accounts.get(DEFAULT_ACCOUNT_KEY)
            );

            if (userProxy != null) {
                ProxyServer.proxy = userProxy;
            }
        }
    }

    @Unique
    private void createAndAddProxyButton(JoinMultiplayerScreen screen) {
        int buttonX = screen.width - BUTTON_WIDTH - BUTTON_MARGIN_RIGHT;

        ProxyServer.proxyMenuButton = Button.builder(
                Component.translatable("gui.proxyserver.button.status", ProxyServer.getLastUsedProxyDisplayComponent()),
                button -> Minecraft.getInstance().setScreenAndShow(new GuiProxy(screen))
        ).bounds(buttonX, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        ((ScreenAccessor) screen).invokeAddRenderableWidget(ProxyServer.proxyMenuButton);
    }
}

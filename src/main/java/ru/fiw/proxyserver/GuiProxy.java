package ru.fiw.proxyserver;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class GuiProxy extends Screen {

    private static final String DEFAULT_TEST_HOST = "mc.hypixel.net";
    private static final int DEFAULT_TEST_PORT = 25565;
    private static final String DEFAULT_NAME_PREFIX = "Proxy_";

    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 20;
    private static final int SMALL_BUTTON_WIDTH = 64;
    private static final int NAME_INPUT_WIDTH = 120;
    private static final int SAVE_BUTTON_WIDTH = 76;
    private static final int PRESET_BUTTON_WIDTH = 175;
    private static final int DELETE_BUTTON_WIDTH = 20;

    private static final int INPUT_MAX_LENGTH_LONG = 512;
    private static final int INPUT_MAX_LENGTH_SHORT = 128;

    private static final int COLOR_TITLE = 0xFFFFFF;
    private static final int COLOR_STATUS = 0xFFE0E0E0;

    private final Screen parent;
    private boolean isSocks4;

    private EditBox ipPort;
    private EditBox username;
    private EditBox password;
    private EditBox nameInput;
    private Checkbox enabledCheck;

    private Component statusMessage = Component.empty();
    private final TestPing testPing = new TestPing();

    private int startY;
    private int centerX;

    private String savedIp = "";
    private String savedUser = "";
    private String savedPass = "";

    public GuiProxy(Screen parent) {
        super(Component.translatable("gui.proxyserver.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.startY = this.height / 2 - 90;

        int x = centerX - (WIDGET_WIDTH / 2);

        this.isSocks4 = ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;

        this.addRenderableWidget(
                Button.builder(
                        getTypeButtonLabel(),
                        b -> {
                            updateSavedFields();
                            isSocks4 = !isSocks4;
                            ProxyServer.proxy.type = isSocks4 ? Proxy.ProxyType.SOCKS4 : Proxy.ProxyType.SOCKS5;
                            this.rebuildWidgets();
                        }
                ).bounds(x, startY, WIDGET_WIDTH, WIDGET_HEIGHT).build()
        );

        this.ipPort = new EditBox(font, x, startY + 24, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty());
        this.ipPort.setMaxLength(INPUT_MAX_LENGTH_LONG);
        this.ipPort.setHint(Component.translatable("gui.proxyserver.hint.ip_port").withStyle(ChatFormatting.DARK_GRAY));
        this.ipPort.setValue(savedIp.isEmpty() ? ProxyServer.proxy.ipPort : savedIp);
        this.addRenderableWidget(ipPort);

        this.username = new EditBox(font, x, startY + 48, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty());
        this.username.setMaxLength(INPUT_MAX_LENGTH_LONG);
        this.username.setHint(
                Component.translatable(isSocks4 ? "gui.proxyserver.hint.userid" : "gui.proxyserver.hint.username")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
        this.username.setValue(savedUser.isEmpty() ? ProxyServer.proxy.username : savedUser);
        this.addRenderableWidget(username);

        if (!isSocks4) {
            this.password = new EditBox(font, x, startY + 72, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty());
            this.password.setMaxLength(INPUT_MAX_LENGTH_LONG);
            this.password.setHint(Component.translatable("gui.proxyserver.hint.password").withStyle(ChatFormatting.DARK_GRAY));
            this.password.setValue(savedPass.isEmpty() ? ProxyServer.proxy.password : savedPass);
            this.addRenderableWidget(password);
        }

        int checkY = startY + (isSocks4 ? 76 : 100);
        this.enabledCheck = Checkbox.builder(Component.translatable("gui.proxyserver.checkbox.enabled"), font)
                .pos(x, checkY)
                .selected(ProxyServer.proxyEnabled)
                .onValueChange((cb, checked) -> ProxyServer.proxyEnabled = checked)
                .build();
        this.addRenderableWidget(enabledCheck);

        int actionButtonsY = startY + (isSocks4 ? 105 : 129);

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("gui.proxyserver.button.apply"),
                        b -> {
                            apply();
                            this.onClose();
                        }
                ).bounds(x, actionButtonsY, SMALL_BUTTON_WIDTH, WIDGET_HEIGHT).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("gui.proxyserver.button.test"),
                        b -> {
                            statusMessage = Component.empty();
                            if (!ipPort.getValue().contains(":")) {
                                statusMessage = Component.translatable("gui.proxyserver.status.invalid_format")
                                        .withStyle(ChatFormatting.RED);
                                return;
                            }

                            testPing.state = ChatFormatting.YELLOW + Component.translatable("gui.proxyserver.status.connecting").getString();
                            testPing.run(
                                    DEFAULT_TEST_HOST,
                                    DEFAULT_TEST_PORT,
                                    new Proxy(
                                            isSocks4,
                                            ipPort.getValue(),
                                            username.getValue(),
                                            password != null ? password.getValue() : ""
                                    )
                            );
                        }
                ).bounds(x + 68, actionButtonsY, SMALL_BUTTON_WIDTH, WIDGET_HEIGHT).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("gui.proxyserver.button.cancel"),
                        b -> this.onClose()
                ).bounds(x + 136, actionButtonsY, SMALL_BUTTON_WIDTH, WIDGET_HEIGHT).build()
        );

        int saveRowY = startY + (isSocks4 ? 132 : 156);

        this.nameInput = new EditBox(font, x, saveRowY, NAME_INPUT_WIDTH, WIDGET_HEIGHT, Component.empty());
        this.nameInput.setMaxLength(INPUT_MAX_LENGTH_SHORT);
        this.nameInput.setHint(Component.translatable("gui.proxyserver.hint.profile_name").withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(nameInput);

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("gui.proxyserver.button.save"),
                        b -> {
                            updateSavedFields();
                            String name = nameInput.getValue().isEmpty()
                                    ? DEFAULT_NAME_PREFIX + System.currentTimeMillis()
                                    : nameInput.getValue();

                            Config.accounts.put(
                                    name,
                                    new Proxy(
                                            isSocks4,
                                            savedIp,
                                            savedUser,
                                            savedPass
                                    )
                            );
                            Config.saveConfig();
                            this.rebuildWidgets();
                        }
                ).bounds(x + 124, saveRowY, SAVE_BUTTON_WIDTH, WIDGET_HEIGHT).build()
        );

        int presetY = startY + (isSocks4 ? 160 : 184);

        for (Map.Entry<String, Proxy> entry : Config.accounts.entrySet()) {
            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(entry.getKey()),
                            b -> {
                                Proxy p = entry.getValue();
                                this.savedIp = p.ipPort;
                                this.savedUser = p.username;
                                this.savedPass = p.password;
                                this.isSocks4 = p.type == Proxy.ProxyType.SOCKS4;
                                this.rebuildWidgets();
                            }
                    ).bounds(x, presetY, PRESET_BUTTON_WIDTH, WIDGET_HEIGHT).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.translatable("gui.proxyserver.button.delete"),
                            b -> {
                                updateSavedFields();
                                Config.accounts.remove(entry.getKey());
                                Config.saveConfig();
                                this.rebuildWidgets();
                            }
                    ).bounds(x + 180, presetY, DELETE_BUTTON_WIDTH, WIDGET_HEIGHT).build()
            );

            presetY += 22;
        }
    }

    private Component getTypeButtonLabel() {
        return Component.translatable(
                "gui.proxyserver.button.type",
                Component.translatable(isSocks4 ? "gui.proxyserver.type.socks4" : "gui.proxyserver.type.socks5")
        );
    }

    private void updateSavedFields() {
        this.savedIp = ipPort.getValue();
        this.savedUser = username.getValue();
        this.savedPass = password != null ? password.getValue() : "";
    }

    private void apply() {
        ProxyServer.proxy = new Proxy(
                isSocks4,
                ipPort.getValue(),
                username.getValue(),
                password != null ? password.getValue() : ""
        );
        ProxyServer.proxyEnabled = enabledCheck.selected();
        Config.saveConfig();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor ctx,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);

        ctx.centeredText(font, this.title, centerX, startY - 20, COLOR_TITLE);

        Component renderedStatus = statusMessage.getString().isEmpty()
                ? (testPing.state != null ? Component.literal(testPing.state) : Component.empty())
                : statusMessage;

        if (!renderedStatus.getString().isEmpty()) {
            ctx.text(font, renderedStatus, centerX + 105, startY + 6, COLOR_STATUS);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}

package de.funboyy.stream.chat.activity;

import de.funboyy.stream.chat.StreamChatAddon;
import de.funboyy.stream.chat.utils.OAuthServer;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.types.SimpleActivity;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.notification.Notification;

@SuppressWarnings("CallToPrintStackTrace")
@Link("token.lss")
@AutoActivity
public class TwitchTokenActivity extends SimpleActivity {

  /**
   * This class was inspired by
   * {@code net.labymod.core.client.gui.screen.activity.activities.account.MicrosoftLoginActivity}
   * **/

  private final StreamChatAddon addon;
  private final String namespace;
  private OAuthServer server;

  public TwitchTokenActivity(final StreamChatAddon addon) {
    this.addon = addon;
    this.namespace = this.addon.addonInfo().getNamespace();

    try {
      this.server = new OAuthServer();
      this.labyAPI.minecraft().chatExecutor().openUrl(OAuthServer.AUTHORIZE_URL);
      this.server.listenForCodeAsync((code) ->
          this.labyAPI.minecraft().executeOnRenderThread(() -> {
            final Notification.Builder builder = Notification.builder()
                .icon(Icon.texture(ResourceLocation.create(this.addon.addonInfo().getNamespace(),
                    "textures/icon.png"))).title(Component.text(this.addon.addonInfo().getDisplayName()));

            if (code == null) {
              this.displayPreviousScreen();

              builder.text(Component.translatable(String.format("%s.notification.authorize.error",
                  addon.addonInfo().getNamespace()), NamedTextColor.RED));
              builder.buildAndPush();
              return;
            }

            this.addon.configuration().tokenConfig().token().set(code);
            this.addon.saveConfiguration();

            this.displayPreviousScreen();

            builder.text(Component.translatable(String.format("%s.notification.authorize.success",
                addon.addonInfo().getNamespace()), NamedTextColor.GREEN));
            builder.buildAndPush();
          }));
    } catch (final Exception exception) {
      LOGGER.warn("Error while listening for the Twitch token");
      exception.printStackTrace();
    }
  }

  public void initialize(final Parent parent) {
    super.initialize(parent);

    final VerticalListWidget<Widget> container = new VerticalListWidget<>();
    container.addId("container");

    final HorizontalListWidget logo = new HorizontalListWidget();
    logo.addId("logo");

    final IconWidget icon = new IconWidget(Icon.texture(ResourceLocation.create(
        this.namespace, "textures/twitch.png")));
    icon.addId("icon");
    logo.addEntry(icon);

    final ComponentWidget name = ComponentWidget.component(Component.text("Twitch Token"));
    name.addId("name");
    logo.addEntry(name);
    container.addChild(logo);

    final ComponentWidget text = ComponentWidget.component(Component.translatable(
        String.format("%s.activity.text", this.namespace), NamedTextColor.GREEN));
    text.addId("text");
    container.addChild(text);

    final DivWidget buttonWrapper = new DivWidget();
    buttonWrapper.addId("button-wrapper");
    container.addChild(buttonWrapper);

    final ButtonWidget button = ButtonWidget.i18n(String.format("%s.activity.button",
        this.namespace), this::displayPreviousScreen);
    button.addId("button");
    buttonWrapper.addChild(button);

    this.document.addChild(container);
  }

  public void onCloseScreen() {
    if (this.server != null) {
      this.server.close();
    }

    super.onCloseScreen();
  }

}

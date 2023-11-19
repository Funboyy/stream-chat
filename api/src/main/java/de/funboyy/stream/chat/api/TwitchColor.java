package de.funboyy.stream.chat.api;

import java.util.Arrays;
import java.util.List;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.ColorUtil;

public class TwitchColor {

  public static TextColor TWITCH = TextColor.color(145, 70, 255);

  public static List<TextColor> DEFAULT_COLORS = Arrays.asList(
      parseHex("#FF0000"),
      parseHex("#0000FF"),
      parseHex("#008000"),
      parseHex("#B22222"),
      parseHex("#FF7F50"),
      parseHex("#9ACD32"),
      parseHex("#FF4500"),
      parseHex("#2E8B57"),
      parseHex("#DAA520"),
      parseHex("#D2691E"),
      parseHex("#5F9EA0"),
      parseHex("#1E90FF"),
      parseHex("#FF69B4"),
      parseHex("#8A2BE2"),
      parseHex("#00FF7F")
  );

  public static TextColor randomColor(final String random) {
    return DEFAULT_COLORS.get(Arrays.stream(random.split("(?!^)"))
        .mapToInt(character -> character.charAt(0)).sum() % 15);
  }

  public static TextColor parseHex(final String hex) {
    return TextColor.color(ColorUtil.toValue(Integer.valueOf(hex.substring(1), 16)));
  }

}

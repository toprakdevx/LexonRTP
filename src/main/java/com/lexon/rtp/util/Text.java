package com.lexon.rtp.util;

import net.md_5.bungee.api.ChatColor;

public final class Text {
    private Text() {
    }

    public static String color(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input.indexOf('&') < 0) {
            return input;
        }
        StringBuilder out = new StringBuilder(input.length() + 8);
        int len = input.length();
        int i = 0;
        while (i < len) {
            char c = input.charAt(i);
            if (c != '&' || i + 1 >= len) {
                out.append(c);
                i++;
                continue;
            }
            char next = input.charAt(i + 1);
            if (next == '#') {
                if (i + 7 <= len && isHex(input, i + 2, i + 8)) {
                    out.append(ChatColor.of('#' + input.substring(i + 2, i + 8)));
                    i += 8;
                    continue;
                }
                out.append('&');
                i++;
                continue;
            }
            ChatColor code = ChatColor.getByChar(Character.toLowerCase(next));
            if (code != null) {
                out.append(code);
                i += 2;
            } else {
                out.append('&');
                i++;
            }
        }
        return out.toString();
    }

    private static boolean isHex(String s, int from, int to) {
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                return false;
            }
        }
        return true;
    }
}

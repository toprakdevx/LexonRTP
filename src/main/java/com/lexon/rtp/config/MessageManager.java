package com.lexon.rtp.config;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MessageManager {
    private final LexonRTP plugin;
    private volatile FileConfiguration messages;
    private volatile String prefix = "";

    public MessageManager(LexonRTP plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String lang = plugin.config().getLanguage();
        String fileName = lang.equals("tr") ? "messages.yml" : "messages_" + lang + ".yml";
        if (plugin.getResource(fileName) == null) {
            plugin.getLogger().warning("Language file '" + fileName
                    + "' not found, falling back to messages_en.yml.");
            fileName = "messages_en.yml";
        }
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = plugin.getResource(fileName)) {
            if (in != null) {
                YamlConfiguration def = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8));
                messages.setDefaults(def);
            }
        } catch (IOException ignored) {
        }
        this.prefix = messages.getString("prefix", "");
    }

    public String raw(String path) {
        String value = messages.getString(path, "&c[missing: " + path + "]");
        return value.replace("%prefix%", prefix);
    }

    public String get(String path, String... replacements) {
        String value = raw(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }
        return Text.color(value);
    }

    public List<String> getList(String path, String... replacements) {
        List<String> out = new ArrayList<>();
        for (String line : messages.getStringList(path)) {
            String value = line.replace("%prefix%", prefix);
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                value = value.replace(replacements[i], replacements[i + 1]);
            }
            out.add(Text.color(value));
        }
        return out;
    }

    public void send(CommandSender target, String path, String... replacements) {
        if (target == null) {
            return;
        }
        String message = get(path, replacements);
        if (!message.isBlank()) {
            target.sendMessage(message);
        }
    }

    public void sendList(CommandSender target, String path, String... replacements) {
        if (target == null) {
            return;
        }
        for (String line : getList(path, replacements)) {
            if (!line.isBlank()) {
                target.sendMessage(line);
            }
        }
    }
}

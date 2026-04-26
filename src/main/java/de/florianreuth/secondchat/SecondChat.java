/*
 * This file is part of SecondChat - https://github.com/florianreuth/SecondChat
 * Copyright (C) 2025-2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianreuth.secondchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.florianreuth.secondchat.filter.ChatConfig;
import de.florianreuth.secondchat.filter.FilterRule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SecondChat implements ClientModInitializer {

    private static SecondChat INSTANCE;

    private final Logger logger = LogManager.getLogger("SecondChat");
    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("secondchat.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<ChatConfig> chatConfigs;

    public static SecondChat instance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        this.chatConfigs = new ArrayList<>();

        if (Files.exists(this.configPath)) {
            try {
                final String json = Files.readString(this.configPath);
                final ChatConfig[] configs = this.gson.fromJson(json, ChatConfig[].class);
                if (configs != null && configs.length > 0) {
                    if (configs[0].name() == null) {
                        // Old format (FilterRule[]): migrate to a single ChatConfig
                        try {
                            final FilterRule[] rules = this.gson.fromJson(json, FilterRule[].class);
                            if (rules != null && rules.length > 0) {
                                final ChatConfig migrated = new ChatConfig("Chat 1", 100, 100);
                                Collections.addAll(migrated.rules(), rules);
                                this.chatConfigs.add(migrated);
                                this.save();
                            }
                        } catch (final Exception ignored) {
                        }
                    } else {
                        this.chatConfigs.addAll(Arrays.asList(configs));
                    }
                }
            } catch (final Exception e) {
                logger.error("Failed to read config file: {}!", this.configPath, e);
            }
        }
    }

    public void save() {
        try {
            Files.write(this.configPath, this.gson.toJson(this.chatConfigs).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final Exception e) {
            logger.error("Failed to write config file: {}!", this.configPath, e);
        }
    }

    public void addChat(final ChatConfig config) {
        this.chatConfigs.add(config);
        this.save();
    }

    public void removeChat(final ChatConfig config) {
        this.chatConfigs.remove(config);
        this.save();
    }

    public List<ChatConfig> chatConfigs() {
        return this.chatConfigs;
    }

}

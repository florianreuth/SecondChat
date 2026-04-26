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

package de.florianreuth.secondchat.filter;

import de.florianreuth.secondchat.SecondChat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.jspecify.annotations.Nullable;

public final class ChatConfig {

    private String name;
    private int x;
    private int y;
    private final List<FilterRule> rules;

    private transient ChatComponent chatComponent;

    public ChatConfig(final String name, final int x, final int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.rules = new ArrayList<>();
    }

    public void ensureInitialized(final Minecraft minecraft) {
        if (this.chatComponent == null) {
            this.chatComponent = new ChatComponent(minecraft);
        }
    }

    public void updateChat(final String name, final int x, final int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        SecondChat.instance().save();
    }

    public void addRule(final FilterRule rule) {
        this.rules.add(rule);
        SecondChat.instance().save();
    }

    public void removeRule(final FilterRule rule) {
        this.rules.remove(rule);
        SecondChat.instance().save();
    }

    public void updateRule(final FilterRule old, final FilterRule updated) {
        final int index = this.rules.indexOf(old);
        if (index >= 0) {
            this.rules.set(index, updated);
        } else {
            this.rules.add(updated);
        }
        SecondChat.instance().save();
    }

    public void clearRules() {
        this.rules.clear();
        SecondChat.instance().save();
    }

    public String name() {
        return this.name;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public ChatComponent chatComponent() {
        return this.chatComponent;
    }

    public List<FilterRule> rules() {
        return this.rules;
    }

    public boolean matches(final String input, @Nullable final String server) {
        if (this.rules.isEmpty()) return false;

        return this.rules.stream()
            .filter(rule -> rule.matchesServer(server))
            .anyMatch(rule -> switch (rule.type()) {
                case EQUALS -> input.equals(rule.value());
                case EQUALS_IGNORE_CASE -> input.equalsIgnoreCase(rule.value());
                case STARTS_WITH -> input.startsWith(rule.value());
                case ENDS_WITH -> input.endsWith(rule.value());
                case CONTAINS -> input.contains(rule.value());
                case REGEX -> input.matches(rule.value());
            });
    }

}

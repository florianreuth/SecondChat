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

package de.florianreuth.secondchat.screen;

import de.florianreuth.secondchat.filter.ChatConfig;
import de.florianreuth.secondchat.filter.FilterRule;
import de.florianreuth.secondchat.filter.FilterType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class EditRuleScreen extends Screen {

    private static final int PADDING = 8;
    private static final int FIELD_WIDTH = 200;
    private static final int TYPE_BUTTON_WIDTH = 160;
    private static final int ACTION_BUTTON_WIDTH = 80;

    private final Screen parent;
    private final ChatConfig chatConfig;
    private final @Nullable FilterRule existing;
    private FilterType filterType;

    private EditBox valueBox;
    private EditBox serverBox;
    private Button saveButton;

    private int valueLabelY;
    private int serverLabelY;
    private int typeLabelY;

    public EditRuleScreen(final Screen parent, final ChatConfig chatConfig, final @Nullable FilterRule existing) {
        super(Component.translatable(existing == null ? "secondchat.config.add.title" : "secondchat.config.edit.title"));
        this.parent = parent;
        this.chatConfig = chatConfig;
        this.existing = existing;
        this.filterType = existing != null ? existing.type() : FilterType.CONTAINS;
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = width / 2;
        int y = height / 3;

        this.valueLabelY = y;
        y += font.lineHeight + 2;
        this.valueBox = addRenderableWidget(new EditBox(font, centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Button.DEFAULT_HEIGHT, Component.empty()));
        this.valueBox.setMaxLength(Integer.MAX_VALUE);
        this.valueBox.setHint(Component.translatable("secondchat.config.filter.hint").withStyle(ChatFormatting.DARK_GRAY));
        if (this.existing != null) this.valueBox.setValue(this.existing.value());
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.serverLabelY = y;
        y += font.lineHeight + 2;
        this.serverBox = addRenderableWidget(new EditBox(font, centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Button.DEFAULT_HEIGHT, Component.empty()));
        this.serverBox.setMaxLength(253);
        this.serverBox.setHint(Component.translatable("secondchat.config.server.hint").withStyle(ChatFormatting.DARK_GRAY));
        if (this.existing != null && this.existing.server() != null) this.serverBox.setValue(this.existing.server());
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.typeLabelY = y;
        y += font.lineHeight + 2;
        addRenderableWidget(Button
            .builder(getFilterTypeText(this.filterType), button -> {
                this.filterType = FilterType.values()[(this.filterType.ordinal() + 1) % FilterType.values().length];
                button.setMessage(getFilterTypeText(this.filterType));
            })
            .pos(centerX - TYPE_BUTTON_WIDTH / 2, y)
            .size(TYPE_BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
            .build());
        y += Button.DEFAULT_HEIGHT + PADDING * 2;

        this.saveButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.save"), _ -> save())
            .pos(centerX - ACTION_BUTTON_WIDTH - PADDING / 2, y)
            .size(ACTION_BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
            .build());
        this.saveButton.active = this.existing != null;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.cancel"), _ -> minecraft.setScreen(new EditChatScreen(this.parent, this.chatConfig)))
            .pos(centerX + PADDING / 2, y)
            .size(ACTION_BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
            .build());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.saveButton != null) {
            this.saveButton.active = !valueBox.getValue().isEmpty();
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new EditChatScreen(this.parent, this.chatConfig));
    }

    private Component getFilterTypeText(final FilterType type) {
        return Component.translatable("secondchat.config.filter." + type.name().toLowerCase()).withStyle(ChatFormatting.GOLD);
    }

    private void save() {
        final String server = this.serverBox.getValue().isBlank() ? null : this.serverBox.getValue();
        final FilterRule updated = new FilterRule(this.valueBox.getValue(), server, this.filterType);
        if (this.existing != null) {
            this.chatConfig.updateRule(this.existing, updated);
        } else {
            this.chatConfig.addRule(updated);
        }
        minecraft.setScreen(new EditChatScreen(this.parent, this.chatConfig));
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        final int centerX = width / 2;
        final int labelX = centerX - FIELD_WIDTH / 2;
        graphics.text(font, title, centerX - font.width(title) / 2, PADDING * 2, -1, true);
        graphics.text(font, Component.translatable("secondchat.config.column.filter"), labelX, this.valueLabelY, -1);
        graphics.text(font, Component.translatable("secondchat.config.column.server"), labelX, this.serverLabelY, -1);
        graphics.text(font, Component.translatable("secondchat.config.column.type"), labelX, this.typeLabelY, -1);
    }

}

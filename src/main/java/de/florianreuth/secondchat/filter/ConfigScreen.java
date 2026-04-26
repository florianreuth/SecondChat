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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

public final class ConfigScreen extends Screen {

    private static final int SELECTED_COLOR = 0x4000AAFF;
    private static final int PADDING = 3;
    private static final int BUTTON_WIDTH = 60;

    private final Screen parent;
    private SlotList slotList;
    private Button editButton;
    private Button deleteButton;

    public ConfigScreen(final Screen parent) {
        super(Component.translatable("secondchat.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        slotList = addRenderableWidget(new SlotList(minecraft, width, height, PADDING + PADDING + (font.lineHeight + 2) * PADDING, Button.DEFAULT_HEIGHT + PADDING * 2));

        final int y = height - Button.DEFAULT_HEIGHT - PADDING;
        final int groupWidth = BUTTON_WIDTH + PADDING + BUTTON_WIDTH + PADDING + BUTTON_WIDTH;
        int x = width / 2 - groupWidth / 2;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.add"), _ -> minecraft.setScreen(new EditRuleScreen(this, null)))
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        x += BUTTON_WIDTH + PADDING;

        editButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.edit"), _ -> {
                final ListEntry selected = slotList.getSelected();
                if (selected != null) {
                    minecraft.setScreen(new EditRuleScreen(this, selected.rule));
                }
            })
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        editButton.active = false;
        x += BUTTON_WIDTH + PADDING;

        deleteButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.delete"), _ -> {
                final ListEntry selected = slotList.getSelected();
                if (selected != null) {
                    SecondChat.instance().remove(selected.rule);
                    minecraft.setScreen(new ConfigScreen(parent));
                }
            })
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        deleteButton.active = false;

        addRenderableWidget(Button
            .builder(Component.literal("←"), _ -> minecraft.setScreen(parent))
            .pos(PADDING, y).size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (slotList == null) return;

        final boolean hasSelection = slotList.getSelected() != null;
        if (editButton != null) editButton.active = hasSelection;
        if (deleteButton != null) deleteButton.active = hasSelection;
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        final Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(2.0F, 2.0F);
        graphics.text(font, title, width / 4 - font.width(title) / 2, PADDING + 1, -1, true);
        pose.popMatrix();
    }

    public class SlotList extends ObjectSelectionList<ListEntry> {

        SlotList(final Minecraft minecraft, final int width, final int height, final int top, final int bottom) {
            super(minecraft, width, height - top - bottom, top,
                minecraft.font.lineHeight * 2 + ListEntry.VERTICAL_PADDING);
            SecondChat.instance().rules().forEach(rule -> addEntry(new ListEntry(rule)));
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 150;
        }
    }

    public class ListEntry extends ObjectSelectionList.Entry<ListEntry> {

        static final int VERTICAL_PADDING = 8;
        private static final int INNER_PADDING = 3;

        final FilterRule rule;

        ListEntry(final FilterRule rule) {
            this.rule = rule;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(rule.value());
        }

        @Override
        public boolean mouseClicked(final @NonNull MouseButtonEvent event, final boolean bl) {
            slotList.setSelected(this);
            return true;
        }

        @Override
        public void extractContent(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
            final Matrix3x2fStack pose = graphics.pose();
            final int width = getContentWidth();
            final int height = getContentHeight();
            final int lineHeight = font.lineHeight;
            final int y1 = (height - lineHeight * 2 - 2) / 2;

            pose.pushMatrix();
            pose.translate(getContentX(), getContentY());

            // Line 1: filter value, truncated to fit
            String valueStr = rule.value();
            final int maxWidth = width - INNER_PADDING * 2;
            if (font.width(valueStr) > maxWidth) {
                while (!valueStr.isEmpty() && font.width(valueStr + "...") > maxWidth) {
                    valueStr = valueStr.substring(0, valueStr.length() - 1);
                }
                valueStr += "...";
            }
            graphics.text(font, Component.literal(valueStr), INNER_PADDING, y1, -1);

            // Line 2: type | server
            final Component typeText = Component.translatable("secondchat.config.filter." + rule.type().name().toLowerCase())
                .withStyle(ChatFormatting.GOLD);
            final Component serverText = rule.server() == null
                ? Component.translatable("secondchat.config.server.all").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(rule.server()).withStyle(ChatFormatting.GRAY);
            final Component subtitle = Component.empty()
                .append(typeText)
                .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                .append(serverText);
            graphics.text(font, subtitle, INNER_PADDING, y1 + lineHeight + 2, -1);

            pose.popMatrix();
        }
    }

}

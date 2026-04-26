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
        this.slotList = addRenderableWidget(new SlotList(minecraft, width, height, PADDING + PADDING + (font.lineHeight + 2) * PADDING, Button.DEFAULT_HEIGHT + PADDING * 2));

        final int y = height - Button.DEFAULT_HEIGHT - PADDING;
        final int groupWidth = BUTTON_WIDTH + PADDING + BUTTON_WIDTH + PADDING + BUTTON_WIDTH;
        int x = width / 2 - groupWidth / 2;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.add"), _ -> minecraft.setScreen(new EditChatScreen(parent, null)))
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        x += BUTTON_WIDTH + PADDING;

        this.editButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.edit"), _ -> {
                final ListEntry selected = this.slotList.getSelected();
                if (selected != null) {
                    openEditChatScreen(selected.config);
                }
            })
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        this.editButton.active = false;
        x += BUTTON_WIDTH + PADDING;

        this.deleteButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.delete"), _ -> {
                final ListEntry selected = slotList.getSelected();
                if (selected != null) {
                    SecondChat.instance().removeChat(selected.config);
                    minecraft.setScreen(new ConfigScreen(parent));
                }
            })
            .pos(x, y).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        this.deleteButton.active = false;

        addRenderableWidget(Button
            .builder(Component.literal("←"), _ -> minecraft.setScreen(parent))
            .pos(PADDING, y).size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (slotList == null) return;

        final boolean hasSelection = this.slotList.getSelected() != null;
        if (this.editButton != null) this.editButton.active = hasSelection;
        if (this.deleteButton != null) this.deleteButton.active = hasSelection;
    }

    private void openEditChatScreen(final ChatConfig config) {
        this.minecraft.setScreen(new EditChatScreen(this.parent, config));
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        final Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(2.0F, 2.0F);
        graphics.text(font, title, width / 4 - font.width(title) / 2, PADDING + 2, -1, true);
        pose.popMatrix();
    }

    public class SlotList extends ObjectSelectionList<ListEntry> {

        SlotList(final Minecraft minecraft, final int width, final int height, final int top, final int bottom) {
            super(minecraft, width, height - top - bottom, top, minecraft.font.lineHeight * 2 + ListEntry.VERTICAL_PADDING);
            SecondChat.instance().chatConfigs().forEach(config -> addEntry(new ListEntry(config)));
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 150;
        }
    }

    public class ListEntry extends ObjectSelectionList.Entry<ListEntry> {

        static final int VERTICAL_PADDING = 8;
        private static final int INNER_PADDING = 3;

        final ChatConfig config;

        ListEntry(final ChatConfig config) {
            this.config = config;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(this.config.name());
        }

        @Override
        public boolean mouseClicked(final @NonNull MouseButtonEvent event, final boolean bl) {
            ConfigScreen.this.slotList.setSelected(this);
            if (bl && event.button() == 0) {
                ConfigScreen.this.openEditChatScreen(this.config);
            }
            return true;
        }

        @Override
        public void extractContent(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
            final Matrix3x2fStack pose = graphics.pose();
            final int height = getContentHeight();
            final int lineHeight = font.lineHeight;
            final int y1 = (height - lineHeight * 2 - 2) / 2;

            pose.pushMatrix();
            pose.translate(getContentX(), getContentY());

            graphics.text(font, Component.literal(this.config.name()), INNER_PADDING, y1, -1);

            final Component posText = Component.literal("x=" + this.config.x() + "%  y=" + this.config.y() + "%").withStyle(ChatFormatting.DARK_GRAY);
            final Component ruleCount = Component.literal(" · " + this.config.rules().size() + " rules").withStyle(ChatFormatting.GOLD);
            graphics.text(font, Component.empty().append(posText).append(ruleCount), INNER_PADDING, y1 + lineHeight + 2, -1);

            pose.popMatrix();
        }
    }

}

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
import de.florianreuth.secondchat.filter.FilterRule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class EditChatScreen extends Screen {

    private static final int PADDING = 8;
    private static final int FIELD_WIDTH = 200;
    private static final int BUTTON_WIDTH = 60;

    private final Screen parent;
    private final @Nullable ChatConfig config;

    private EditBox nameBox;
    private PercentSlider xSlider;
    private PercentSlider ySlider;
    private Button saveButton;

    // Only used when editing an existing config
    private @Nullable RuleList ruleList;
    private @Nullable Button editRuleButton;
    private @Nullable Button deleteRuleButton;

    private int nameLabelY;

    public EditChatScreen(final Screen parent, final @Nullable ChatConfig config) {
        super(Component.translatable(config == null ? "secondchat.chat.add.title" : "secondchat.chat.edit.title"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = width / 2;
        if (this.config == null) {
            initNewChatForm(centerX);
        } else {
            initEditChatForm(centerX);
        }
    }

    private void initNewChatForm(final int centerX) {
        int y = height / 3;

        this.nameLabelY = y;
        y += font.lineHeight + 2;
        this.nameBox = addRenderableWidget(new EditBox(font, centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Button.DEFAULT_HEIGHT, Component.empty()));
        this.nameBox.setMaxLength(64);
        this.nameBox.setHint(Component.translatable("secondchat.chat.name.hint").withStyle(ChatFormatting.DARK_GRAY));
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.xSlider = addRenderableWidget(new PercentSlider(centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Component.translatable("secondchat.chat.x"), 0));
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.ySlider = addRenderableWidget(new PercentSlider(centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Component.translatable("secondchat.chat.y"), 0));
        y += Button.DEFAULT_HEIGHT + PADDING * 2;

        this.saveButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.save"), _ -> saveNewChat())
            .pos(centerX - BUTTON_WIDTH - PADDING / 2, y)
            .size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
            .build());
        this.saveButton.active = false;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.cancel"), _ -> minecraft.setScreen(new ConfigScreen(parent)))
            .pos(centerX + PADDING / 2, y)
            .size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
            .build());
    }

    private void initEditChatForm(final int centerX) {
        int y = PADDING + (font.lineHeight + 2) * 2 + PADDING;

        this.nameLabelY = y;
        y += font.lineHeight + 2;
        this.nameBox = addRenderableWidget(new EditBox(font, centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Button.DEFAULT_HEIGHT, Component.empty()));
        this.nameBox.setMaxLength(64);
        this.nameBox.setValue(config.name());
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.xSlider = addRenderableWidget(new PercentSlider(centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Component.translatable("secondchat.chat.x"), config.x()));
        y += Button.DEFAULT_HEIGHT + PADDING;

        this.ySlider = addRenderableWidget(new PercentSlider(centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, Component.translatable("secondchat.chat.y"), config.y()));
        y += Button.DEFAULT_HEIGHT + PADDING;

        final int listBottom = Button.DEFAULT_HEIGHT + PADDING * 2;
        this.ruleList = addRenderableWidget(new RuleList(minecraft, width, height, y, listBottom));

        final int buttonsY = height - Button.DEFAULT_HEIGHT - PADDING;
        final int groupWidth = BUTTON_WIDTH + PADDING + BUTTON_WIDTH + PADDING + BUTTON_WIDTH + PADDING + BUTTON_WIDTH;
        int bx = width / 2 - groupWidth / 2;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.add.rule"), _ -> minecraft.setScreen(new EditRuleScreen(this.parent, this.config, null)))
            .pos(bx, buttonsY).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        bx += BUTTON_WIDTH + PADDING;

        this.editRuleButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.edit"), _ -> {
                final RuleEntry selected = this.ruleList != null ? this.ruleList.getSelected() : null;
                if (selected != null) openEditRuleScreen(selected.rule);
            })
            .pos(bx, buttonsY).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        this.editRuleButton.active = false;
        bx += BUTTON_WIDTH + PADDING;

        this.deleteRuleButton = addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.delete"), _ -> {
                final RuleEntry selected = this.ruleList != null ? this.ruleList.getSelected() : null;
                if (selected != null) {
                    this.config.removeRule(selected.rule);
                    minecraft.setScreen(new EditChatScreen(this.parent, this.config));
                }
            })
            .pos(bx, buttonsY).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());
        deleteRuleButton.active = false;
        bx += BUTTON_WIDTH + PADDING;

        addRenderableWidget(Button
            .builder(Component.translatable("secondchat.config.button.clear"), _ -> {
                this.config.clearRules();
                minecraft.setScreen(new EditChatScreen(this.parent, this.config));
            })
            .pos(bx, buttonsY).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT).build());

        addRenderableWidget(Button
            .builder(Component.literal("←"), _ -> done())
            .pos(PADDING, buttonsY).size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.config == null && this.saveButton != null) {
            this.saveButton.active = !this.nameBox.getValue().isBlank();
        }

        if (this.ruleList != null) {
            final boolean hasSelection = this.ruleList.getSelected() != null;
            if (this.editRuleButton != null) this.editRuleButton.active = hasSelection;
            if (this.deleteRuleButton != null) this.deleteRuleButton.active = hasSelection;
        }
    }

    @Override
    public void onClose() {
        if (this.config != null) {
            done();
        } else {
            minecraft.setScreen(new ConfigScreen(parent));
        }
    }

    private void saveNewChat() {
        final String name = this.nameBox.getValue().trim();
        if (name.isBlank()) return;

        final ChatConfig newConfig = new ChatConfig(name, this.xSlider.getValue(), this.ySlider.getValue());
        SecondChat.instance().addChat(newConfig);
        minecraft.setScreen(new ConfigScreen(this.parent));
    }

    private void done() {
        if (this.config != null) {
            final String name = this.nameBox.getValue().trim();
            if (!name.isBlank()) {
                this.config.updateChat(name, this.xSlider.getValue(), this.ySlider.getValue());
            }
        }
        minecraft.setScreen(new ConfigScreen(this.parent));
    }

    private void openEditRuleScreen(final FilterRule rule) {
        if (this.config != null) {
            this.minecraft.setScreen(new EditRuleScreen(this.parent, this.config, rule));
        }
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        final int centerX = width / 2;

        if (this.config != null) {
            final Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.scale(2.0F, 2.0F);
            graphics.text(font, title, width / 4 - font.width(title) / 2, PADDING + 1, -1, true);
            pose.popMatrix();
        } else {
            graphics.text(font, title, centerX - font.width(title) / 2, PADDING * 2, -1, true);
        }

        graphics.text(font, Component.translatable("secondchat.chat.name"), centerX - FIELD_WIDTH / 2, nameLabelY, -1);
    }

    private static final class PercentSlider extends AbstractSliderButton {

        private final Component label;

        PercentSlider(final int x, final int y, final int w, final Component label, final int initialPct) {
            super(x, y, w, DEFAULT_HEIGHT, Component.empty(), Mth.clamp(initialPct / 100.0, 0.0, 1.0));
            this.label = label;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(this.label.copy().append(Component.literal(": " + getValue() + "%")));
        }

        @Override
        protected void applyValue() {
        }

        int getValue() {
            return (int) Math.round(this.value * 100);
        }
    }

    public class RuleList extends ObjectSelectionList<RuleEntry> {

        RuleList(final Minecraft minecraft, final int width, final int height, final int top, final int bottom) {
            super(minecraft, width, height - top - bottom, top,
                minecraft.font.lineHeight * 2 + RuleEntry.VERTICAL_PADDING);
            if (EditChatScreen.this.config != null) {
                EditChatScreen.this.config.rules().forEach(rule -> this.addEntry(new RuleEntry(rule)));
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 150;
        }
    }

    public class RuleEntry extends ObjectSelectionList.Entry<RuleEntry> {

        static final int VERTICAL_PADDING = 8;
        private static final int INNER_PADDING = 3;

        final FilterRule rule;

        RuleEntry(final FilterRule rule) {
            this.rule = rule;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(this.rule.value());
        }

        @Override
        public boolean mouseClicked(final @NonNull MouseButtonEvent event, final boolean bl) {
            if (EditChatScreen.this.ruleList != null) EditChatScreen.this.ruleList.setSelected(this);
            if (bl && event.button() == 0) {
                EditChatScreen.this.openEditRuleScreen(this.rule);
            }
            return true;
        }

        @Override
        public void extractContent(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
            final Matrix3x2fStack pose = graphics.pose();
            final int w = getContentWidth();
            final int h = getContentHeight();
            final int y1 = (h - font.lineHeight * 2 - 2) / 2;

            pose.pushMatrix();
            pose.translate(getContentX(), getContentY());

            String valueString = this.rule.value();
            final int maxWidth = w - INNER_PADDING * 2;
            if (font.width(valueString) > maxWidth) {
                while (!valueString.isEmpty() && font.width(valueString + "...") > maxWidth) {
                    valueString = valueString.substring(0, valueString.length() - 1);
                }
                valueString += "...";
            }
            graphics.text(font, Component.literal(valueString), INNER_PADDING, y1, -1);

            final Component typeText = Component.translatable("secondchat.config.filter." + this.rule.type().name().toLowerCase()).withStyle(ChatFormatting.GOLD);
            final Component serverText = this.rule.server() == null ? Component.translatable("secondchat.config.server.all").withStyle(ChatFormatting.DARK_GRAY) : Component.literal(this.rule.server()).withStyle(ChatFormatting.GRAY);
            graphics.text(font, Component.empty().append(typeText).append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(serverText), INNER_PADDING, y1 + font.lineHeight + 2, -1);

            pose.popMatrix();
        }
    }

}

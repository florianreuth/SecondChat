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

package de.florianreuth.secondchat.injection.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.florianreuth.secondchat.SecondChat;
import de.florianreuth.secondchat.filter.ChatConfig;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    @Unique
    private @Nullable ChatConfig secondChat$focusedConfig;

    protected MixinChatScreen(Component title) {
        super(title);
    }

    @Shadow
    protected abstract boolean insertionClickMode();

    @Shadow
    private ChatComponent.DisplayMode displayMode;

    @WrapOperation(method = {"keyPressed", "mouseScrolled"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void scrollAdditionalChats(ChatComponent instance, int dir, Operation<Void> original) {
        if (this.secondChat$focusedConfig == null) {
            original.call(instance, dir);
        } else {
            this.secondChat$focusedConfig.chatComponent().scrollChat(dir);
        }
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "NEW", target = "(Lnet/minecraft/client/gui/Font;II)Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;"))
    private ActiveTextCollector.ClickableStyleFinder clickAdditionalChats(Font font, int mouseX, int testY, Operation<ActiveTextCollector.ClickableStyleFinder> original) {
        if (this.secondChat$focusedConfig != null) {
            mouseX = mouseX - secondChat$translateX(this.secondChat$focusedConfig);
            testY = testY - secondChat$translateY(this.secondChat$focusedConfig);
        }
        return original.call(font, mouseX, testY);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;"))
    private ChatComponent clickAdditionalChats(Gui instance) {
        return this.secondChat$focusedConfig == null ? instance.getChat() : this.secondChat$focusedConfig.chatComponent();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void decideFocusedChat(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        this.secondChat$focusedConfig = null;

        for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
            if (config.chatComponent() == null) continue;

            final int translateX = secondChat$translateX(config);
            final int translateY = secondChat$translateY(config);
            final Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(translateX, translateY);
            config.chatComponent().extractRenderState(graphics, font, minecraft.gui.getGuiTicks(), mouseX, mouseY, displayMode, insertionClickMode());
            pose.popMatrix();

            if (secondChat$isMouseOver(config, mouseX, mouseY)) {
                this.secondChat$focusedConfig = config;
            }
        }
    }

    @Unique
    private boolean secondChat$isMouseOver(final ChatConfig config, final int mouseX, final int mouseY) {
        if (config.chatComponent() == null) return false;

        final int left = secondChat$translateX(config);
        final int right = left + config.chatComponent().getWidth();
        final int anchorY = this.height - 40 + secondChat$translateY(config);
        final int top = anchorY - config.chatComponent().getHeight();
        final int bottom = anchorY + this.font.lineHeight;

        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    @Unique
    private int secondChat$translateX(final ChatConfig config) {
        if (config.chatComponent() == null) return 0;
        final int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        return (int) ((config.x() / 100.0F) * Math.max(0, guiWidth - config.chatComponent().getWidth()));
    }

    @Unique
    private int secondChat$translateY(final ChatConfig config) {
        return (int) ((config.y() - 100) / 100.0F * height);
    }

}

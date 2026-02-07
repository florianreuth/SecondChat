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
import de.florianreuth.secondchat.injection.access.IGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    @Unique
    private boolean secondChat$mainChatFocused;

    protected MixinChatScreen(Component title) {
        super(title);
    }

    @Shadow
    protected abstract boolean insertionClickMode();

    @WrapOperation(method = {"keyPressed", "mouseScrolled"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void scrollSecondChat(ChatComponent instance, int posInc, Operation<Void> original) {
        if (secondChat$mainChatFocused) {
            original.call(instance, posInc);
        } else {
            secondChat$getChatHud().scrollChat(posInc);
        }
    }

    @WrapOperation(method = {"mouseClicked"}, at = @At(value = "NEW", target = "(Lnet/minecraft/client/gui/Font;II)Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;"))
    private ActiveTextCollector.ClickableStyleFinder clickSecondChat(Font font, int mouseX, int mouseY, Operation<ActiveTextCollector.ClickableStyleFinder> original) {
        if (secondChat$mainChatFocused) {
            mouseX = secondChat$fixMouseX(mouseX);
        }

        return original.call(font, mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void decideFocusedChat(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        secondChat$mainChatFocused = mouseX <= width / 2;

        final Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        final ChatComponent secondChat = secondChat$getChatHud();
        pose.translate(guiGraphics.guiWidth() - secondChat.getWidth(), 0);
        secondChat.render(guiGraphics, font, minecraft.gui.getGuiTicks(), mouseX, mouseY, true, insertionClickMode());
        pose.popMatrix();
    }

    @Unique
    private int secondChat$fixMouseX(final int mouseX) {
        return mouseX - minecraft.getWindow().getGuiScaledWidth() + secondChat$getChatHud().getWidth();
    }

    @Unique
    private ChatComponent secondChat$getChatHud() {
        final Gui gui = Minecraft.getInstance().gui;
        return ((IGui) gui).secondChat$getChatComponent();
    }

}

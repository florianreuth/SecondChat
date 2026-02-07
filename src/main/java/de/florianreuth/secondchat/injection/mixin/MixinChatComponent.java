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

import de.florianreuth.secondchat.SecondChat;
import de.florianreuth.secondchat.injection.access.IGui;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    public void proxyMessages(Component chatComponent, MessageSignature headerSignature, GuiMessageTag tag, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            final boolean cancel = SecondChat.instance().matches(chatComponent.getString());
            if (!cancel) {
                return;
            }

            ci.cancel();
            secondChat$getChatHud().addMessage(chatComponent, headerSignature, tag);
        }
    }

    @Inject(method = "clearMessages", at = @At("RETURN"))
    public void clearSecondChat(boolean clearHistory, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            secondChat$getChatHud().clearMessages(clearHistory);
        }
    }

    @Inject(method = "rescaleChat", at = @At("RETURN"))
    public void rescaleSecondChat(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            secondChat$getChatHud().rescaleChat();
        }
    }

    @Inject(method = "resetChatScroll", at = @At("RETURN"))
    public void resetScrollSecondChat(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            secondChat$getChatHud().resetChatScroll();
        }
    }

    @Unique
    private ChatComponent secondChat$getChatHud() {
        final Gui gui = Minecraft.getInstance().gui;
        return ((IGui) gui).secondChat$getChatComponent();
    }

}

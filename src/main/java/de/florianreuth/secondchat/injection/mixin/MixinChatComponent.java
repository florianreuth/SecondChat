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
import de.florianreuth.secondchat.filter.ChatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @Inject(method = "addMessage", at = @At("HEAD"), cancellable = true)
    private void proxyMessages(Component contents, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            final ServerData currentServer = Minecraft.getInstance().getCurrentServer();
            final String server = currentServer != null ? currentServer.ip : null;
            final String text = ChatFormatting.stripFormatting(contents.getString());

            boolean matched = false;
            for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
                if (config.chatComponent() != null && config.matches(text, server)) {
                    config.chatComponent().addMessage(contents, signature, source, tag);
                    matched = true;
                }
            }
            if (matched) ci.cancel();
        }
    }

    @Inject(method = "deleteMessage", at = @At("RETURN"))
    private void proxyDeleteMessage(MessageSignature signature, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
                if (config.chatComponent() != null) config.chatComponent().deleteMessage(signature);
            }
        }
    }

    @Inject(method = "clearMessages", at = @At("RETURN"))
    private void clearAdditionalChats(boolean history, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
                if (config.chatComponent() != null) config.chatComponent().clearMessages(history);
            }
        }
    }

    @Inject(method = "rescaleChat", at = @At("RETURN"))
    private void rescaleAdditionalChats(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
                if (config.chatComponent() != null) config.chatComponent().rescaleChat();
            }
        }
    }

    @Inject(method = "resetChatScroll", at = @At("RETURN"))
    private void resetAdditionalChatsScroll(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().gui.getChat()) {
            for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
                if (config.chatComponent() != null) config.chatComponent().resetChatScroll();
            }
        }
    }

}

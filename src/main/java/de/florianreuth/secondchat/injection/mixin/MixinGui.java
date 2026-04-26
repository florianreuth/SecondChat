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

import de.florianreuth.secondchat.filter.ChatConfig;
import de.florianreuth.secondchat.SecondChat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.joml.Matrix3x2fStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow
    @Final
    private ChatComponent chat;

    @Shadow
    protected abstract void extractChat(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker);

    @Unique
    private ChatComponent secondChat$currentChat;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
            config.ensureInitialized(minecraft);
        }
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void renderSecondChats(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        for (final ChatConfig config : SecondChat.instance().chatConfigs()) {
            config.ensureInitialized(Minecraft.getInstance());

            this.secondChat$currentChat = config.chatComponent();
            final Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            final int chatWidth = this.secondChat$currentChat.getWidth();
            final int tx = (int) ((config.x() / 100.0f) * Math.max(0, graphics.guiWidth() - chatWidth));
            final int ty = (int) ((config.y() - 100) / 100.0f * graphics.guiHeight());
            pose.translate(tx, ty);
            this.extractChat(graphics, deltaTracker);
            pose.popMatrix();
        }
        this.secondChat$currentChat = null;
    }

    @Redirect(method = "extractChat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;chat:Lnet/minecraft/client/gui/components/ChatComponent;", opcode = Opcodes.GETFIELD))
    private ChatComponent replaceChatComponent(Gui instance) {
        return this.secondChat$currentChat != null ? this.secondChat$currentChat : chat;
    }

}

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

import de.florianreuth.secondchat.injection.access.IGui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
public abstract class MixinGui implements IGui {

    @Shadow
    @Final
    private ChatComponent chat;

    @Unique
    private ChatComponent secondChat$chatComponent;

    @Unique
    private boolean secondChat$replacingChatHud;

    @Shadow
    protected abstract void renderChat(final GuiGraphics guiGraphics, final DeltaTracker deltaTracker);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        secondChat$chatComponent = new ChatComponent(minecraft);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderChat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void renderSecondChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        secondChat$replacingChatHud = true;

        final Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(guiGraphics.guiWidth() - secondChat$chatComponent.getWidth(), 0);
        this.renderChat(guiGraphics, deltaTracker);
        pose.popMatrix();

        secondChat$replacingChatHud = false;
    }

    @Redirect(method = "renderChat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;chat:Lnet/minecraft/client/gui/components/ChatComponent;", opcode = Opcodes.GETFIELD))
    private ChatComponent replaceChatComponent(Gui instance) {
        if (secondChat$replacingChatHud) {
            return secondChat$chatComponent;
        } else {
            return chat;
        }
    }

    @Override
    public ChatComponent secondChat$getChatComponent() {
        return secondChat$chatComponent;
    }

}

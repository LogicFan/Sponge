/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.inventory;

import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Collection;
import java.util.Random;

@Mixin(InventoryHelper.class)
public class MixinInventoryHelper {

    @Shadow @Final private static Random RANDOM;

    private static final String
            DROP_INVENTORY_ITEMS_BLOCK_POS =
            "dropInventoryItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/inventory/IInventory;)V";
    private static final String
            DROP_INVENTORY_ITEMS_X_Y_Z =
            "Lnet/minecraft/inventory/InventoryHelper;dropInventoryItems(Lnet/minecraft/world/World;DDDLnet/minecraft/inventory/IInventory;)V";

    @Redirect(method = DROP_INVENTORY_ITEMS_BLOCK_POS, at = @At(value = "INVOKE", target = DROP_INVENTORY_ITEMS_X_Y_Z))
    private static void dropInventoryItems(World world, double x, double y, double z, IInventory inventory) {
        if (world instanceof WorldServer) {
            final IMixinWorldServer mixinWorld = (IMixinWorldServer) world;
            final PhaseData currentPhase = mixinWorld.getCauseTracker().getStack().peek();
            final IPhaseState currentState = currentPhase.getState();
            if (currentState.tracksBlockSpecificDrops()) {
                final PhaseContext context = currentPhase.getContext();
                final Multimap<BlockPos, EntityItem> multimap = context.getBlockItemDropSupplier().get();
                final BlockPos pos = new BlockPos(x, y, z);
                final Collection<EntityItem> itemStacks = multimap.get(pos);
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    final net.minecraft.item.ItemStack itemStack = inventory.getStackInSlot(i);
                    if (itemStack != null) {
                        float f = RANDOM.nextFloat() * 0.8F + 0.1F;
                        float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
                        float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;
                        int stackSize = RANDOM.nextInt(21) + 10;

                        if (stackSize > itemStack.stackSize) {
                            stackSize = itemStack.stackSize;
                        }

                        itemStack.stackSize -= stackSize;
                        final double posX = x + (double) f;
                        final double posY = y + (double) f1;
                        final double posZ = z + (double) f2;
                        EntityItem entityitem = new EntityItem(world, posX, posY, posZ, new ItemStack(itemStack.getItem(), stackSize, itemStack.getMetadata()));

                        if (itemStack.hasTagCompound()) {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entityitem.motionX = RANDOM.nextGaussian() * (double) f3;
                        entityitem.motionY = RANDOM.nextGaussian() * (double) f3 + 0.20000000298023224D;
                        entityitem.motionZ = RANDOM.nextGaussian() * (double) f3;
                        itemStacks.add(entityitem);
                    }
                }
            }
        }
    }

}

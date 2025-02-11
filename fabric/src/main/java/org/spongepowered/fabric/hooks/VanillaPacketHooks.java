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
package org.spongepowered.fabric.hooks;

import org.spongepowered.common.hooks.PacketHooks;

public final class VanillaPacketHooks implements PacketHooks {

    // TODO Minecraft 1.16.4 - I may no longer need to send fake dimension changes, client knows of all levels now...
//    @Override
//    public SRespawnPacket createSRespawnPacket(
//          final ServerPlayerEntity entity,
//          final DimensionType dimensionType,
//          final RegistryKey<World> dimension,
//          final long seed,
//          final GameType playerGameType,
//          final GameType previousPlayerGameType,
//          final boolean isDebug,
//          final boolean isFlat,
//          final boolean keepAllPlayerData
//    ) {
//
//        if (((ServerPlayerEntityBridge) entity).bridge$getClientType() == ClientType.SPONGE_VANILLA) {
//            return PacketHooks.super.createSRespawnPacket(
//                  entity,
//                  dimensionType,
//                  dimension,
//                  seed,
//                  playerGameType,
//                  previousPlayerGameType,
//                  isDebug,
//                  isFlat,
//                  keepAllPlayerData
//            );
//        } else {
//            DimensionType clientType;
//            final SpongeDimensionType logicType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();
//            if (DimensionTypes.OVERWORLD.get() == logicType) {
//                clientType = DimensionType.OVERWORLD;
//            } else if (DimensionTypes.THE_NETHER.get() == logicType) {
//                clientType = DimensionType.THE_NETHER;
//            } else {
//                clientType = DimensionType.THE_END;
//            }
//
//            return new SRespawnPacket(clientType, seed, worldType, gameType);
//        }
//    }
}

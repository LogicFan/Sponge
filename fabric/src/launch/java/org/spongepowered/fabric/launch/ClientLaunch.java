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
package org.spongepowered.fabric.launch;

import com.google.inject.Stage;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.fabric.applaunch.plugin.FabricPluginPlatform;

public final class ClientLaunch extends FabricLaunch {

    protected ClientLaunch(final FabricPluginPlatform pluginPlatform, final Stage injectionStage) {
        super(pluginPlatform, injectionStage);
    }

    public static void launch(final FabricPluginPlatform pluginEngine, final Boolean isDeveloperEnvironment, final String[] args) {
        final ClientLaunch launcher = new ClientLaunch(pluginEngine, isDeveloperEnvironment ? Stage.DEVELOPMENT : Stage.PRODUCTION);
        Launch.setInstance(launcher);
        launcher.launchPlatform(args);
    }

    @Override
    public boolean dedicatedServer() {
        return false;
    }

    public void performBootstrap(final String[] args) {
        SpongeBootstrap.perform("Client", () -> {});
    }
}

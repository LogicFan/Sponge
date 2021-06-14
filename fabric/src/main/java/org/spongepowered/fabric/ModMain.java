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
package org.spongepowered.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.server.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginEngine;
import org.spongepowered.fabric.launch.ClientLaunch;

import java.lang.reflect.InvocationTargetException;

public class ModMain implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer, PreLaunchEntrypoint {
	private static final Logger LOGGER = LogManager.getLogger(ModMain.class);

	@Override
	public void onPreLaunch() {
		String[] args = System.getProperty("sun.java.command").split(" ");
		LOGGER.info("Invoking SpongeFabric installer with args {}", (Object) args);

		invokeMain("org.spongepowered.fabric.installer.InstallerMain", args);
	}

	@Override
	public void onInitialize() {
		System.out.println("Hello Fabric world!");
	}

	@Override
	public void onInitializeClient() {
		invokeMain("org.spongepowered.fabric.applaunch.handler.ClientLaunchHandler", new String[]{});
	}

	@Override
	public void onInitializeServer() {
		invokeMain("org.spongepowered.fabric.applaunch.handler.ServerLaunchHandler", new String[]{});
	}

	private static void invokeMain(final String className, final String[] args) {
		try {
			Class.forName(className)
					.getMethod("main", String[].class)
					.invoke(null, (Object) args);
		} catch (final InvocationTargetException ex) {
			LOGGER.error("Failed to invoke main class {} due to an error", className, ex.getCause());
			System.exit(1);
		} catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
			LOGGER.error("Failed to invoke main class {} due to an error", className, ex);
			System.exit(1);
		}
	}
}

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
package org.spongepowered.fabric.applaunch.plugin;

import org.spongepowered.common.applaunch.plugin.PluginEngine;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.PluginResourceLocatorService;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class FabricPluginEngine implements PluginEngine {

	private final PluginEnvironment pluginEnvironment;
	private final Map<String, PluginResourceLocatorService<PluginResource>> locatorServices;
	private final Map<String, PluginLanguageService<PluginResource>> languageServices;

	private final Map<String, List<PluginResource>> locatorResources;
	private final Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> pluginCandidates;

	public FabricPluginEngine(PluginEnvironment pluginEnvironment) {
		this.pluginEnvironment = pluginEnvironment;
		this.locatorServices = new HashMap<>();
		this.languageServices = new HashMap<>();
		this.locatorResources = new HashMap<>();
		this.pluginCandidates = new IdentityHashMap<>();
	}

	@Override
	public PluginEnvironment getPluginEnvironment() {
		return pluginEnvironment;
	}

	public Map<String, PluginResourceLocatorService<PluginResource>> getLocatorServices() {
		return Collections.unmodifiableMap(this.locatorServices);
	}

	public Map<String, PluginLanguageService<PluginResource>> getLanguageServices() {
		return Collections.unmodifiableMap(this.languageServices);
	}

	public Map<String, List<PluginResource>> getResources() {
		return Collections.unmodifiableMap(this.locatorResources);
	}

	public Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> getCandidates() {
		return this.pluginCandidates;
	}

	public void configure() {
		this.getPluginEnvironment().logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}",
				this.pluginSubsystemVersion(), this.codeSource());

		this.discoverLocatorServices();
		this.getLocatorServices().forEach((k, v) -> this.getPluginEnvironment()
				.logger().info("Plugin resource locator '{}' found.", k));
		this.discoverLanguageServices();
		this.getLanguageServices().forEach((k, v) -> this.getPluginEnvironment()
				.logger().info("Plugin language loader '{}' found.", k));

		for (final Map.Entry<String, PluginLanguageService<PluginResource>> entry : this.languageServices.entrySet()) {
			entry.getValue().initialize(this.pluginEnvironment);
		}
	}

	private String pluginSubsystemVersion() {
		return this.getPluginEnvironment().blackboard().get(PluginKeys.VERSION).orElse("Unknown");
	}

	private String codeSource() {
		try {
			return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		} catch (final Throwable th) {
			return "Unknown";
		}
	}

	public void discoverLocatorServices() {
		@SuppressWarnings("unchecked")
		final ServiceLoader<PluginResourceLocatorService<PluginResource>> serviceLoader = (ServiceLoader<PluginResourceLocatorService<PluginResource>>) (Object) ServiceLoader.load(
				PluginResourceLocatorService.class, null);

		for (final Iterator<PluginResourceLocatorService<PluginResource>> iter = serviceLoader.iterator(); iter.hasNext(); ) {
			final PluginResourceLocatorService<PluginResource> next;

			try {
				next = iter.next();
			} catch (final ServiceConfigurationError e) {
				this.pluginEnvironment.logger().error("Error encountered initializing plugin resource locator!", e);
				continue;
			}

			this.locatorServices.put(next.name(), next);
		}
	}

	public void discoverLanguageServices() {
		@SuppressWarnings("unchecked")
		final ServiceLoader<PluginLanguageService<PluginResource>> serviceLoader = (ServiceLoader<PluginLanguageService<PluginResource>>) (Object) ServiceLoader.load(
				PluginLanguageService.class, FabricPluginEngine.class.getClassLoader());

		for (final Iterator<PluginLanguageService<PluginResource>> it = serviceLoader.iterator(); it.hasNext(); ) {
			final PluginLanguageService<PluginResource> next;

			try {
				next = it.next();
			} catch (final ServiceConfigurationError e) {
				this.pluginEnvironment.logger().error("Error encountered initializing plugin language service!", e);
				continue;
			}

			this.languageServices.put(next.name(), next);
		}
	}
}

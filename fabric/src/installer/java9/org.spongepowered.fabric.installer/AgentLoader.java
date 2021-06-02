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
package org.spongepowered.fabric.installer;

import com.sun.tools.attach.VirtualMachine;
import org.tinylog.Logger;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;

public class AgentLoader {
    public static void load() {
        try {
            File agent = new File(Agent.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            Logger.info("Locate Java Agent at {}", agent.toPath());

            long pid = ProcessHandle.current().pid();
            Logger.info("Current process ID is {}", pid);

            VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
            vm.loadAgent(agent.getAbsolutePath());
            vm.detach();

            Logger.info("Java Agent is successfully loaded.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot locate Agent class file location", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Do not have sufficient permission to load Java Agent.", e);
        } catch (Exception e) {
            throw new RuntimeException("Load Java Agent fail.", e);
        }
    }
}

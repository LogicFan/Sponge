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

import org.tinylog.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarFile;

/**
 * Agent, used to add downloaded jars to the system classpath and open modules
 * for deep reflection.
 *
 * <p>See the JDK9+ counterpart in src/installer/java9</p>
 */
public class KnotClassLoaderUtils {
    static void addJarToClasspath(final Path jar) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            // TODO: this is non-public fabric loader api.
            Method addURL = loader.getClass().getMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(loader, jar.toUri().toURL());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("ClassLoader " + loader.getClass().getName() + " is not KnotClassLoaderInterface.", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Incorrect jar file path " + jar + ".", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Cannot invoke method", e);
        }
    }
}

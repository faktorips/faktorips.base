/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Abstractions;

/**
 * This class simply creates a JarFile for a previously specified path. After creating a
 * {@link JarFileFactory} with a specified {@link IPath} you could create as many {@link JarFile jar
 * files} as you want by calling {@link #createJarFile()}. You have to close the jar file using the
 * {@link #closeJarFile()} method yourself.
 * <p>
 * The path in this {@link JarFileFactory} may either be absolute in the workspace or it is absolute
 * in the file system. The factory first checks if there is a file with the specified path in the
 * workspace. If not it will take the path as being absolute in the file system.
 * <p>
 * This class holds a reference to an jar file and delays the close method in an thread. If the same
 * jar file should be opened before the delay is up, the timer is reset and the close method will be
 * delayed once again.
 */
public class JarFileFactory {

    private static final boolean TRACE_JAR_FILES = Boolean
            .parseBoolean(Platform.getDebugOption("org.faktorips.devtools.model/trace/jarfiles")); //$NON-NLS-1$

    private static final Map<Path, OpenedJar> CACHE = new ConcurrentHashMap<>();

    private static final int DEFAULT_DELAY_TIME = 5000;

    private static int closeDelay = DEFAULT_DELAY_TIME;

    private final Path jarPath;

    /**
     * Create the {@link JarFileFactory}, the jarPath is the absolute path to the jar file this
     * factory will construct. It is either absolute in workspace or absolute in file system.
     * 
     * @param jarPath The absolute path to a jar file
     */
    public JarFileFactory(Path jarPath) {
        this.jarPath = jarPath;
    }

    protected void setCloseDelay(int ms) {
        closeDelay = ms;
    }

    /**
     * Returns the path of the jar file this factory could construct.
     * 
     * @return The absolute jar file path
     */
    public Path getJarPath() {
        return jarPath;
    }

    /**
     * This method either creates a new jar file or returns a still open instance of the same jar
     * file.
     * 
     * <p>
     * You have to ensure that the jar file is closed correctly after you do not need it any longer,
     * using the {@link #closeJarFile()} method.
     * 
     * @return A new jar file of the path specified in this factory
     * 
     * @throws IOException in case of any IO exception while creating the {@link JarFileFactory}
     * @see JarFile#JarFile(java.io.File)
     */
    public synchronized JarFile createJarFile() throws IOException {
        OpenedJar openedJar = CACHE.get(getJarPath());
        if (openedJar == null) {
            JarFile jarFile = new JarFile(getAbsolutePath(getJarPath()).toFile());
            openedJar = new OpenedJar(jarFile);
            CACHE.put(getJarPath(), openedJar);
            trace(MessageFormat.format("Open {0}.", jarFile.getName())); //$NON-NLS-1$
        }
        return openedJar.getJarFile();
    }

    private void trace(String message) {
        if (TRACE_JAR_FILES) {
            System.out.println("JarFileFactory: " + message); //$NON-NLS-1$
        }
    }

    /* private */Path getAbsolutePath(Path bundlePath) {
        if (isWorkspaceRelativePath(bundlePath)) {
            return getWorkspaceRelativePath(bundlePath);
        } else {
            return bundlePath;
        }
    }

    private boolean isWorkspaceRelativePath(Path bundlePath) {
        return Abstractions.getWorkspace().getRoot().getFile(bundlePath).exists();
    }

    private Path getWorkspaceRelativePath(Path bundlePath) {
        AFile file = Abstractions.getWorkspace().getRoot().getFile(bundlePath);
        return file.getLocation();
    }

    /**
     * Closes the jar file after a delay.
     */
    public synchronized void closeJarFile() {
        OpenedJar openedJar = CACHE.get(getJarPath());
        if (openedJar != null) {
            openedJar.close();
        }
    }

    /**
     * Inner class holding the reference to the jar file and the thread for delaying the close
     * method.
     */
    private class OpenedJar {

        private final JarFile jarFile;
        private volatile long lastCallToClose;
        private volatile Closer closer;

        public OpenedJar(JarFile jarFile) {
            this.jarFile = jarFile;
        }

        public synchronized JarFile getJarFile() {
            if (closer != null) {
                trace(MessageFormat.format("Received a reopen request for {0}. Delaying close.", jarFile.getName())); //$NON-NLS-1$
                closer.dontCloseYet();
            }
            return jarFile;
        }

        public synchronized void close() {
            trace(MessageFormat.format("Received close for {0}. Wait {1} ms before doing so.", //$NON-NLS-1$
                    jarFile.getName(),
                    closeDelay));
            lastCallToClose = System.currentTimeMillis();
            if (closer == null) {
                closer = new Closer();
                new Thread(closer, "Closer for " + jarFile.getName()).start(); //$NON-NLS-1$
            } else {
                closer.closeIsOkForMe();
            }
        }

        private class Closer implements Runnable {

            private AtomicInteger opened = new AtomicInteger(0);

            @Override
            public void run() {
                while (true) {
                    if (opened.get() <= 0 && lastCallToClose + closeDelay <= System.currentTimeMillis()) {
                        try {
                            jarFile.close();
                            CACHE.remove(jarPath);
                            trace(MessageFormat.format("Finally closing {0}.", jarFile.getName())); //$NON-NLS-1$
                            break;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Thread.sleep(closeDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            }

            protected void dontCloseYet() {
                opened.incrementAndGet();
            }

            protected void closeIsOkForMe() {
                opened.decrementAndGet();
            }

        }

    }
}

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
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * This class simply creates a JarFile for a previously specified path. After creating a
 * {@link JarFileFactory} with a specified {@link IPath} you could create as many {@link JarFile jar
 * files} as you want by calling {@link #createJarFile()}. You have to verify for yourself that the
 * jar file is closed correctly after use.
 * <p>
 * The path in this {@link JarFileFactory} may either be absolute in the workspace or it is absolute
 * in the file system. The factory first checks if there is a file with the specified path in the
 * workspace. If not it will take the path as being absolute in the file system.
 */
public class JarFileFactory {

    private static final boolean TRACE_JAR_FILES = Boolean
            .valueOf(Platform.getDebugOption("org.faktorips.devtools.model/trace/jarfiles")).booleanValue(); //$NON-NLS-1$

    private static final Map<IPath, OpenedJar> CACHE = new ConcurrentHashMap<>();

    private static final int DEFAULT_DELAY_TIME = 5000;

    private static int closeDelay = DEFAULT_DELAY_TIME;

    private final IPath jarPath;

    /**
     * Create the {@link JarFileFactory}, the jarPath is the absolute path to the jar file this
     * factory will construct. It is either absolute in workspace or absolute in file system.
     * 
     * @param jarPath The absolute path to a jar file
     */
    public JarFileFactory(IPath jarPath) {
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
    public IPath getJarPath() {
        return jarPath;
    }

    /**
     * Creates a new jar file every time you call this method. You have to ensure that the jar file
     * is closed correctly after you do not need it any longer.
     * 
     * @return A new jar file of the path specified in this factory
     * 
     * @throws IOException in case of any IO exception while creating the {@link JarFileFactory}
     * @see JarFile#JarFile(java.io.File)
     */
    public synchronized JarFile createJarFile() throws IOException {
        OpenedJar openedJar = CACHE.get(getJarPath());
        if (openedJar != null) {
            openedJar.resetCreatedAt();
        } else {
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

    /* private */IPath getAbsolutePath(IPath bundlePath) {
        if (isWorkspaceRelativePath(bundlePath)) {
            return getWorkspaceRelativePath(bundlePath);
        } else {
            return bundlePath;
        }
    }

    private boolean isWorkspaceRelativePath(IPath bundlePath) {
        return ResourcesPlugin.getWorkspace().getRoot().exists(bundlePath);
    }

    private IPath getWorkspaceRelativePath(IPath bundlePath) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(bundlePath);
        return file.getLocation();
    }

    public synchronized void closeJarFile() {
        OpenedJar openedJar = CACHE.get(getJarPath());
        if (openedJar != null) {
            openedJar.close();
        }
    }

    private class OpenedJar {

        private final JarFile jarFile;
        private volatile long createdAt;
        private final Thread closer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (getCreatedAt() + closeDelay <= System.currentTimeMillis()) {
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
        });

        public OpenedJar(JarFile jarFile) {
            this.jarFile = jarFile;
            createdAt = System.currentTimeMillis();
        }

        public JarFile getJarFile() {
            return jarFile;
        }

        public synchronized long getCreatedAt() {
            return createdAt;
        }

        public synchronized void resetCreatedAt() {
            trace(MessageFormat.format("Received a reopen request for {0}. Delaying close.", jarFile.getName())); //$NON-NLS-1$
            createdAt = System.currentTimeMillis();
        }

        public void close() {
            if (!closer.isAlive()) {
                trace(MessageFormat.format("Received close for {0}. Wait {1} ms before doing so.", jarFile.getName(), //$NON-NLS-1$
                        closeDelay));

                closer.start();
            }
        }
    }
}

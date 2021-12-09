/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsStatus;

class PlainJavaUtil {

    private PlainJavaUtil() {
        // util
    }

    static File directory(File potentialDirectory) {
        if (!potentialDirectory.exists() || potentialDirectory.isDirectory()) {
            return potentialDirectory;
        }
        throw new IllegalArgumentException(potentialDirectory + " is not a directory"); //$NON-NLS-1$
    }

    static void walk(File file, IProgressMonitor monitor, String taskname, PathHandler pathHandler) {
        try {
            if (monitor != null) {
                AtomicInteger count = new AtomicInteger(0);
                Files.walkFileTree(file.toPath(), new SimpleFileVisitor<java.nio.file.Path>() {
                    @Override
                    public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                            throws IOException {
                        count.incrementAndGet();
                        return super.visitFile(file, attrs);
                    }
                });
                monitor.beginTask(taskname + " " + file, count.get()); //$NON-NLS-1$
            }
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<java.nio.file.Path>() {
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                        throws IOException {
                    pathHandler.handle(file);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    return super.visitFile(file, attrs);
                }
            });
            if (monitor != null) {
                monitor.done();
            }
        } catch (IOException e) {
            throw new CoreRuntimeException(new IpsStatus(taskname + " " + file + " failed", e)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    static void withMonitor(File file, IProgressMonitor monitor, String taskname, PathHandler pathHandler) {
        if (monitor != null) {
            monitor.beginTask(taskname + " " + file, 1); //$NON-NLS-1$
        }
        try {
            pathHandler.handle(file.toPath());
        } catch (IOException e) {
            throw new CoreRuntimeException(new IpsStatus(taskname + " " + file + " failed", e)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (monitor != null) {
            monitor.done();
        }
    }

    @FunctionalInterface
    static interface PathHandler {
        public void handle(Path path) throws IOException;
    }

}

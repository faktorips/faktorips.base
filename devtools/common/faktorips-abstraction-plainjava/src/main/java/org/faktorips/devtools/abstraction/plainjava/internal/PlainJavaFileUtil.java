/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;

public class PlainJavaFileUtil {

    private PlainJavaFileUtil() {
        // util
    }

    public static File copy(File file, Path destination, IProgressMonitor monitor) {
        try {
            if (monitor != null) {
                initializeAndStartMonitor(file, monitor, "Copying"); //$NON-NLS-1$
            }
            File copy = makeAbsolute(file, destination).toFile();
            if (file.isDirectory()) {
                FileUtils.copyDirectory(file, copy);
            } else {
                FileUtils.copyFile(file, copy);
            }
            if (monitor != null) {
                monitor.worked(1);
            }
            return copy;
        } catch (IOException | IllegalArgumentException e) {
            throw new IpsException("Copying " + file + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private static Path makeAbsolute(File file, Path destination) {
        PlainJavaWorkspaceRoot workspaceRoot = PlainJavaImplementation.get().getWorkspace().getRoot();
        if (workspaceRoot.getProjects().stream().anyMatch(p -> destination.startsWith(p.getWorkspaceRelativePath()))) {
            Path workspacePath = workspaceRoot.getLocation();
            return workspacePath.resolve(destination);
        }
        Path parentDir = file.toPath().toAbsolutePath().getParent();
        return parentDir == null ? destination : parentDir.resolve(destination);
    }

    public static void move(File file, Path destination, IProgressMonitor monitor) {
        try {
            if (monitor != null) {
                initializeAndStartMonitor(file, monitor, "Moving"); //$NON-NLS-1$
            }
            if (file.isDirectory()) {
                FileUtils.moveDirectory(file, makeAbsolute(file, destination).toFile());
            } else {
                FileUtils.moveFile(file, makeAbsolute(file, destination).toFile());
            }
            if (monitor != null) {
                monitor.worked(1);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IpsException("Moving " + file + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    public static File directory(File potentialDirectory) {
        if (!potentialDirectory.exists() || potentialDirectory.isDirectory()) {
            return potentialDirectory;
        }
        throw new IllegalArgumentException(potentialDirectory + " is not a directory"); //$NON-NLS-1$
    }

    public static File internalResource(File potentialInternal, PlainJavaProject project) {
        if (potentialInternal.toPath().startsWith(
                project.getWorkspace().getRoot().directory().toPath().resolve(project.getWorkspaceRelativePath()))) {
            return potentialInternal;
        }
        throw new IllegalArgumentException(potentialInternal + " is not in the project: " + project.getName()); //$NON-NLS-1$
    }

    public static void walk(File file, IProgressMonitor monitor, String taskname, PathHandler pathHandler) {
        try {
            if (monitor != null) {
                initializeAndStartMonitor(file, monitor, taskname);
            }
            Files.walkFileTree(file.toPath(), new PathHandlerFileVisitor(pathHandler, monitor));
            if (monitor != null) {
                monitor.done();
            }
        } catch (IOException e) {
            throw new IpsException(taskname + " " + file + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void initializeAndStartMonitor(File file, IProgressMonitor monitor, String taskname)
            throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        Files.walkFileTree(file.toPath(), new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                    throws IOException {
                count.incrementAndGet();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                count.incrementAndGet();
                return super.postVisitDirectory(dir, exc);
            }
        });
        monitor.beginTask(taskname + " " + file, count.get()); //$NON-NLS-1$
    }

    public static void withMonitor(File file, IProgressMonitor monitor, String taskname, PathHandler pathHandler) {
        if (monitor != null) {
            monitor.beginTask(taskname + " " + file, 1); //$NON-NLS-1$
        }
        try {
            pathHandler.handle(file.toPath());
        } catch (IOException e) {
            throw new IpsException(taskname + " " + file + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (monitor != null) {
            monitor.done();
        }
    }

    @FunctionalInterface
    public interface PathHandler {
        void handle(Path path) throws IOException;
    }

    private static final class PathHandlerFileVisitor extends SimpleFileVisitor<java.nio.file.Path> {
        private final PathHandler pathHandler;
        private final IProgressMonitor monitor;

        private PathHandlerFileVisitor(PathHandler pathHandler, IProgressMonitor monitor) {
            this.pathHandler = pathHandler;
            this.monitor = monitor;
        }

        @Override
        public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                throws IOException {
            pathHandler.handle(file);
            if (monitor != null) {
                monitor.worked(1);
            }
            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            pathHandler.handle(dir);
            if (monitor != null) {
                monitor.worked(1);
            }
            return super.postVisitDirectory(dir, exc);
        }
    }
}

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

import static org.faktorips.devtools.model.abstraction.Wrappers.run;
import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.abstraction.AFile.PlainJavaFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * A folder is a file-system resource that contains files and/or other folders.
 */
public interface AFolder extends AContainer {

    /**
     * Returns the file with the given name contained in this folder.
     * <p>
     * Note that that file does not necessarily {@link AResource#exists() exist}.
     *
     * @param name the file's name
     * @return the file with the given name
     */
    AFile getFile(String name);

    /**
     * Returns the folder with the given name contained in this folder.
     * <p>
     * Note that that folder does not necessarily {@link AResource#exists() exist}.
     *
     * @param name the folder name
     * @return the folder with the given name
     */
    AFolder getFolder(String name);

    /**
     * Creates this folder in the file-system.
     *
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     *
     * @throws CoreRuntimeException if the folder already exists or creation fails
     */
    void create(IProgressMonitor monitor);

    public static class AEclipseFolder extends AEclipseContainer implements AFolder {

        AEclipseFolder(IFolder folder) {
            super(folder);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IFolder unwrap() {
            return (IFolder)super.unwrap();
        }

        IFolder folder() {
            return unwrap();
        }

        @Override
        public AFile getFile(String name) {
            return wrap(folder().getFile(name)).as(AFile.class);
        }

        @Override
        public AFolder getFolder(String name) {
            return wrap(folder().getFolder(name)).as(AFolder.class);
        }

        @Override
        public void create(IProgressMonitor monitor) {
            run(() -> folder().create(true, true, monitor));
        }

    }

    public static class PlainJavaFolder extends PlainJavaContainer implements AFolder {

        public PlainJavaFolder(File directory) {
            super(directory);
        }

        @Override
        public AResourceType getType() {
            return AResourceType.FOLDER;
        }

        @Override
        public PlainJavaFile getFile(String name) {
            return getWorkspace().getRoot().file(directory().toPath().resolve(name));
        }

        @Override
        public PlainJavaFolder getFolder(String name) {
            return getWorkspace().getRoot().folder(directory().toPath().resolve(name));
        }

        @Override
        public void create(IProgressMonitor monitor) {
            if (monitor != null) {
                monitor.beginTask("Creating folder " + directory(), 1); //$NON-NLS-1$
            }
            create();
            if (monitor != null) {
                monitor.done();
            }
        }

        @Override
        void create() {
            directory().mkdir();
        }

    }
}

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

import static org.faktorips.devtools.model.abstraction.PlainJavaUtil.withMonitor;
import static org.faktorips.devtools.model.abstraction.Wrappers.get;
import static org.faktorips.devtools.model.abstraction.Wrappers.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.IoUtil;

/**
 * A file is a resource in the file-system that contains data.
 */
public interface AFile extends AResource {

    /**
     * Returns the corresponding file's extension (the part after the last '.'), if there is any.
     *
     * @return this file's extension or an empty String if the file has no extension
     */
    String getExtension();

    /**
     * Returns whether this file is read-only (meaning data can be read but not written).
     *
     * @return whether this file is read-only
     */
    boolean isReadOnly();

    /**
     * Creates this file in the file-system with data from the given {@link InputStream}.
     *
     * @param source provides the data to be written to the new file
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     * @throws CoreRuntimeException if the file already exists or creation fails
     */
    void create(InputStream source, IProgressMonitor monitor);

    /**
     * Returns the file's data contents as an {@link InputStream}.
     *
     * @return the file's contents
     * @throws CoreRuntimeException if the file can't be read
     */
    InputStream getContents();

    /**
     * Overwrites this file in the file-system with data from the given {@link InputStream}.
     *
     * @param source provides the data to be written to the new file
     * @param keepHistory whether to keep a history of the content (if supported by the workspace)
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     * @throws CoreRuntimeException if the file can't be written or does not exist
     */
    void setContents(InputStream source, boolean keepHistory, IProgressMonitor monitor);

    public static class AEclipseFile extends AEclipseResource implements AFile {

        protected AEclipseFile(IFile file) {
            super(file);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IFile unwrap() {
            return (IFile)super.unwrap();
        }

        IFile file() {
            return unwrap();
        }

        @Override
        public void create(InputStream source, IProgressMonitor monitor) {
            run(() -> file().create(source, true, monitor));
        }

        @Override
        public InputStream getContents() {
            return get(() -> file().getContents(true));
        }

        @Override
        public void setContents(InputStream source, boolean keepHistory, IProgressMonitor monitor) {
            try {
                IFile file = file();
                if (!file.isReadOnly()
                        || file.getWorkspace().validateEdit(new IFile[] { file }, IWorkspace.VALIDATE_PROMPT).isOK()) {
                    file.setContents(source, true, keepHistory, monitor);
                } else {
                    IpsLog.log(new Status(IStatus.ERROR, IpsModelActivator.PLUGIN_ID,
                            "Cannot write to file " + file.getFullPath() + ". Maybe it is locked or readonly.")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            } finally {
                IoUtil.close(source);
            }
        }

        @Override
        public boolean isReadOnly() {
            return file().isReadOnly();
        }

        @Override
        public String getExtension() {
            String extension = file().getFileExtension();
            return extension == null ? StringUtils.EMPTY : extension;
        }

    }

    public static class PlainJavaFile extends PlainJavaResource implements AFile {

        public PlainJavaFile(File file) {
            super(file);
        }

        @Override
        public AResourceType getType() {
            return AResourceType.FILE;
        }

        @Override
        void create() {
            try {
                file().createNewFile();
            } catch (IOException e) {
                throw new CoreRuntimeException(new IpsStatus("Creating " + file() + " failed", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public void create(InputStream source, IProgressMonitor monitor) {
            withMonitor(file(), monitor, "Creating", p -> { //$NON-NLS-1$
                p.getParent().toFile().mkdirs();
                Files.copy(source, p, StandardCopyOption.REPLACE_EXISTING);
            });
        }

        @Override
        public InputStream getContents() {
            try {
                return new FileInputStream(file());
            } catch (FileNotFoundException e) {
                throw new CoreRuntimeException(new IpsStatus("Can't read " + file(), e)); //$NON-NLS-1$
            }
        }

        @Override
        public void setContents(InputStream source, boolean keepHistory, IProgressMonitor monitor) {
            // TODO history?
            withMonitor(file(), monitor, "Writing", p -> //$NON-NLS-1$
            Files.copy(source, p, StandardCopyOption.REPLACE_EXISTING));
        }

        @Override
        public boolean isReadOnly() {
            // TODO Files from JARs?
            return false;
        }

        @Override
        public String getExtension() {
            String name = getName();
            int lastPeriod = name.lastIndexOf('.');
            if (lastPeriod > 0) {
                return name.substring(lastPeriod + 1);
            }
            return StringUtils.EMPTY;
        }

    }

}
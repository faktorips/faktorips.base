/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.faktorips.devtools.abstraction.Wrappers.get;
import static org.faktorips.devtools.abstraction.Wrappers.run;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.IoUtil;

public class AEclipseFile extends AEclipseResource implements AFile {

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
                Abstractions.getLog().log(new Status(IStatus.ERROR, EclipseImplementation.PLUGIN_ID,
                        "Cannot write to file " + file.getFullPath() + ". Maybe it is locked or readonly.")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (CoreException e) {
            throw new IpsException(e);
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
        return extension == null ? IpsStringUtils.EMPTY : extension;
    }

}
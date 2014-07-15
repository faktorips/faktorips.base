/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.w3c.dom.Document;

/**
 * Implementation of <tt>IIpsSrcFile</tt>.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFile extends AbstractIpsSrcFile {

    private IFile file;

    public IpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    @Override
    public IFile getCorrespondingFile() {
        if (file == null) {
            IFolder folder = (IFolder)getParent().getCorrespondingResource();
            file = folder.getFile(getName());
        }
        return file;
    }

    @Override
    public boolean isDirty() {
        IpsSrcFileContent content = getContent();
        if (content == null) {
            return false;
        }
        return content.isModified();
    }

    @Override
    public void markAsClean() {
        IpsSrcFileContent content = getContent();
        if (content != null) {
            content.markAsUnmodified();
        }
    }

    @Override
    public void markAsDirty() {
        IpsSrcFileContent content = getContent();
        if (content != null) {
            content.markAsModified();
        }
    }

    @Override
    public void discardChanges() {
        IpsSrcFileContent content = getContent();
        if (content != null) {
            content.discardChanges();
        }
    }

    @Override
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("File does not exist " + this)); //$NON-NLS-1$
        }
        getContent().save(force, monitor);
    }

    @Override
    public InputStream getContentFromEnclosingResource() throws CoreException {
        return getCorrespondingFile().getContents(true);
    }

    @Override
    public IIpsSrcFileMemento newMemento() throws CoreException {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        return new IpsSrcFileMemento(this, getIpsObject().toXml(doc), isDirty());
    }

    @Override
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        if (!memento.getIpsSrcFile().equals(this)) {
            throw new CoreException(new IpsStatus(this + ": Memento " + memento + " is from different object.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        getContent().updateState(memento.getState(), memento.isDirty());
    }

    String getEncoding() {
        return getIpsProject().getXmlFileCharset();
    }

    @Override
    public boolean exists() {
        return getCorrespondingFile().exists();
    }

    @Override
    public boolean isMutable() {
        IFile file = (IFile)getEnclosingResource();
        return file.exists() && !file.isReadOnly();
    }

    @Override
    public boolean isHistoric() {
        return false;
    }

    @Override
    public String getBasePackageNameForMergableArtefacts() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }

    @Override
    public void delete() throws CoreException {
        getCorrespondingResource().delete(true, null);
        ((IpsModel)getIpsModel()).removeIpsSrcFileContent(this);
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("IpsSrcFile names cannot be changed."); //$NON-NLS-1$};
    }
}

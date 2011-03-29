/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
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

    public IpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    @Override
    public IFile getCorrespondingFile() {
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFile(getName());
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
        return new IIpsSrcFileMemento(this, getIpsObject().toXml(doc), isDirty());
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

}

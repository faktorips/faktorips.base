/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.w3c.dom.Document;

/**
 * @author Jan Ortmann
 */
public class LibraryIpsSrcFile extends AbstractIpsSrcFile {

    public LibraryIpsSrcFile(LibraryIpsPackageFragment pack, String name) {
        super(pack, name);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void markAsClean() {
        // never dirty => nothing to do
    }

    @Override
    public void markAsDirty() {
        throw new RuntimeException("Can't mark an file in an archive as dirty!"); //$NON-NLS-1$
    }

    @Override
    public void discardChanges() {
        // never dirty => nothing to do
    }

    @Override
    public IIpsSrcFileMemento newMemento() throws CoreException {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        return new IpsSrcFileMemento(this, getIpsObject().toXml(doc), false);
    }

    @Override
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        // never dirty => nothing to do
    }

    @Override
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        // not possible => nothing to do
    }

    @Override
    public boolean isHistoric() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public IFile getCorrespondingFile() {
        return null;
    }

    @Override
    public InputStream getContentFromEnclosingResource() {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getIpsPackageFragment().getRoot();
        IIpsStorage storage = root.getIpsStorage();
        if (storage == null) {
            return null;
        }
        return storage.getContent(getQualifiedNameType().toPath());
    }

    @Override
    public String getBasePackageNameForMergableArtefacts() throws CoreException {
        IIpsStorage storage = getIpsPackageFragment().getRoot().getIpsStorage();
        return storage.getBasePackageNameForMergableArtefacts(getQualifiedNameType());
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts() throws CoreException {
        IIpsStorage storage = getIpsPackageFragment().getRoot().getIpsStorage();
        return storage.getBasePackageNameForDerivedArtefacts(getQualifiedNameType());
    }

    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("Archived IPS Source Files cannot be deleted."); //$NON-NLS-1$
    }

}

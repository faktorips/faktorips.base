/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragment;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.ipsobject.ILibraryIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.util.XmlUtil;
import org.w3c.dom.Document;

/**
 * @author Jan Ortmann
 */
public class LibraryIpsSrcFile extends AbstractIpsSrcFile implements ILibraryIpsSrcFile {

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
    public IIpsSrcFileMemento newMemento() {
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        return new IpsSrcFileMemento(this, getIpsObject().toXml(doc), false);
    }

    @Override
    public void setMemento(IIpsSrcFileMemento memento) {
        // never dirty => nothing to do
    }

    /**
     * @deprecated since 22.6 for removal; use {@link #save(IProgressMonitor)} instead, as the
     *             {@code force} parameter is ignored anyways.
     */
    @Override
    @Deprecated(forRemoval = true, since = "22.6")
    public void save(boolean force, IProgressMonitor monitor) {
        // not possible => nothing to do
    }

    @Override
    public void save(IProgressMonitor monitor) {
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
    public AFile getCorrespondingFile() {
        return null;
    }

    @Override
    public InputStream getContentFromEnclosingResource() {
        ILibraryIpsPackageFragmentRoot root = (ILibraryIpsPackageFragmentRoot)getIpsPackageFragment().getRoot();
        IIpsStorage storage = root.getIpsStorage();
        if (storage == null) {
            return null;
        }
        return storage.getContent(getQualifiedNameType().toPath());
    }

    @Override
    public String getBasePackageNameForMergableArtefacts() {
        IIpsStorage storage = getIpsPackageFragment().getRoot().getIpsStorage();
        return storage.getBasePackageNameForMergableArtefacts(getQualifiedNameType());
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts() {
        IIpsStorage storage = getIpsPackageFragment().getRoot().getIpsStorage();
        return storage.getBasePackageNameForDerivedArtefacts(getQualifiedNameType());
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Archived IPS Source Files cannot be deleted."); //$NON-NLS-1$
    }

}

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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AFolder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.XmlUtil;
import org.w3c.dom.Document;

/**
 * Implementation of <code>IIpsSrcFile</code>.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFile extends AbstractIpsSrcFile {

    private AFile correspondingFile;

    public IpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    @Override
    public AFile getCorrespondingFile() {
        if (correspondingFile == null) {
            AFolder folder = (AFolder)getParent().getCorrespondingResource();
            correspondingFile = folder.getFile(getName());
        }
        return correspondingFile;
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
    public void save(boolean force, IProgressMonitor monitor) throws CoreRuntimeException {
        if (!exists()) {
            throw new CoreRuntimeException(new IpsStatus("File does not exist " + this)); //$NON-NLS-1$
        }
        IpsSrcFileContent content = getContent();
        List<IPreSaveProcessor> preSaveProcessors = getPreSaveProcessors();
        if (!preSaveProcessors.isEmpty()) {
            IIpsObject ipsObject = getIpsObject();
            preSaveProcessors.forEach(p -> p.process(ipsObject));
            content = ((AbstractIpsSrcFile)ipsObject.getIpsSrcFile()).getContent();
        }
        content.save(force, monitor);
    }

    /**
     * @since 21.12
     */
    private List<IPreSaveProcessor> getPreSaveProcessors() {
        return IIpsModelExtensions.get().getPreSaveProcessors(getIpsObjectType());
    }

    @Override
    public InputStream getContentFromEnclosingResource() throws CoreRuntimeException {
        return getCorrespondingFile().getContents();
    }

    @Override
    public IIpsSrcFileMemento newMemento() throws CoreRuntimeException {
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        return new IpsSrcFileMemento(this, getIpsObject().toXml(doc), isDirty());
    }

    @Override
    public void setMemento(IIpsSrcFileMemento memento) throws CoreRuntimeException {
        if (!memento.getIpsSrcFile().equals(this)) {
            throw new CoreRuntimeException(new IpsStatus(this + ": Memento " + memento + " is from different object.")); //$NON-NLS-1$ //$NON-NLS-2$
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
        AFile file = (AFile)getEnclosingResource();
        return file.exists() && !file.isReadOnly();
    }

    @Override
    public boolean isHistoric() {
        return false;
    }

    @Override
    public String getBasePackageNameForMergableArtefacts() throws CoreRuntimeException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    @Override
    public String getBasePackageNameForDerivedArtefacts() throws CoreRuntimeException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }

    @Override
    public void delete() throws CoreRuntimeException {
        getCorrespondingResource().delete(null);
        ((IpsModel)getIpsModel()).removeIpsSrcFileContent(this);
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("IpsSrcFile names cannot be changed."); //$NON-NLS-1$
    }

}

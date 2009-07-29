/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.w3c.dom.Document;


/**
 * Implementation of IpsSrcFile.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFile extends AbstractIpsSrcFile implements IIpsSrcFile {
    
    public IpsSrcFile(IIpsElement parent, String name) {
    	super(parent, name);
    }

    /** 
     * {@inheritDoc}
     */
    public IFile getCorrespondingFile() {
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFile(getName());
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isDirty() {
        IpsSrcFileContent content = getContent();
        if (content==null) {
            return false;
        }
        return content.isModified();
    }
    
    /**
     * {@inheritDoc}
     */ 
    public void markAsClean() {
        IpsSrcFileContent content = getContent();
        if (content!=null) {
            content.markAsUnmodified();
        }
    }

    /**
     * {@inheritDoc}
     */ 
    public void markAsDirty() {
        IpsSrcFileContent content = getContent();
        if (content!=null) {
            content.markAsModified();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void discardChanges() {
        IpsSrcFileContent content = getContent();
        if (content!=null) {
            content.discardChanges();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("File does not exist " + this)); //$NON-NLS-1$
        }
        getContent().save(force, monitor);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContentFromEnclosingResource() throws CoreException {
    	return getCorrespondingFile().getContents(true);
    }

    /** 
     * {@inheritDoc}
     */
    public IIpsSrcFileMemento newMemento() throws CoreException {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        return new IIpsSrcFileMemento(this, getIpsObject().toXml(doc), isDirty());
    }
    
    /**
     * {@inheritDoc}
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        if (!memento.getIpsSrcFile().equals(this)) {
            throw new CoreException(new IpsStatus(this + ": Memento " + memento + " is from different object.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        getContent().updateState(memento.getState(), memento.isDirty());
    }
    
    String getEncoding() throws CoreException {
        return getIpsProject().getXmlFileCharset();
    }

    /**
     * {@inheritDoc}
     */
	public boolean isMutable() {
		IFile file = (IFile)getEnclosingResource();
        return file.exists() && !file.isReadOnly();
	}

    /**
     * {@inheritDoc}
     */
    public boolean isHistoric() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForMergableArtefacts() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForDerivedArtefacts() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }
}

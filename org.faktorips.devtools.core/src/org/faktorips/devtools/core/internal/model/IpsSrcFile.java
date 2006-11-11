/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;


/**
 * Implementation of IpsSrcFile.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFile extends AbstractIpsSrcFile implements IIpsSrcFile {
    
    public IpsSrcFile(IIpsElement parent, String name) {

    	super(parent, name);
    	if(IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name)) == null){
    		throw new IllegalArgumentException("The provided file name is not a valid file name for an " //$NON-NLS-1$
    				+  StringUtil.unqualifiedName(IpsSrcFile.class.getName()));
    	}
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
    public String getContentFromEnclosingResource() throws CoreException {
    	InputStream is = getCorrespondingFile().getContents(true);
        try {
            return StringUtil.readFromInputStream(is, getEncoding());
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
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
		return true;
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
    public String getBasePackageNameForGeneratedJavaClass() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForGeneratedJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForExtensionJavaClass() throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragment().getRoot();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getBasePackageNameForExtensionJavaClasses();
    }
    
}

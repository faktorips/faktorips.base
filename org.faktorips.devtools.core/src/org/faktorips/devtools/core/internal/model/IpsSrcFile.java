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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.StringUtil;


/**
 * Implementation of IpsSrcFile.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFile extends IpsElement implements IIpsSrcFile {
    
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
    public IIpsPackageFragment getIpsPackageFragment() {
        return (IIpsPackageFragment)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    }
    
    /** 
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return getCorrespondingFile();
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
    public IIpsElement[] getChildren() throws CoreException {
        if (isContentParsable()) {
            return new IIpsElement[]{getIpsObject()};
        }
        return NO_CHILDREN; 
    }
    
    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsSrcFile.gif"); //$NON-NLS-1$
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isDirty() {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents==null) {
            return false;
        }
        return contents.modified();
    }
    
    /**
     * {@inheritDoc}
     */ 
    public void markAsClean() {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents!=null) {
            contents.markAsUnmodified();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void discardChanges() {
    	IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
    	if (contents!=null) {
    		contents.setSourceText(null, true);
    		contents.markAsUnmodified();
    	}
    }

    /** 
     * {@inheritDoc}
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        if (!isDirty()) {
            return;
        }
        try {
        	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
        		System.out.println("IpsSrcFile.save() begin: " + this);
        	}
            ByteArrayInputStream is = new ByteArrayInputStream(getContents().getBytes(getEncoding()));
        	markAsClean();
            getCorrespondingFile().setContents(is, force, true, monitor);
            // notify listeners because dirty state has changed!
            ContentChangeEvent event = new ContentChangeEvent(this);
            ((IpsModel)getIpsModel()).notifyChangeListeners(event);
        	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
        		System.out.println("IpsSrcFile.save() finished: " + this);
        	}
        } catch (UnsupportedEncodingException e) {
        	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
        		System.out.println("IpsSrcFile.save() failed: " + this);
        	}
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getContents() throws CoreException {
        return getContentObject().getSourceText();
    }

    /**
     * {@inheritDoc}
     */ 
    public void setContents(String newContents) throws CoreException {
        getContentObject().setSourceText(newContents, true);
    }
    
    /**
     * Sets the new contents without enforcing reparsing of the new contents.
     * Should be used by <code>IpsObjectImpl.updateSrcFile()</code> only.
     */
    void setContentsInternal(String newContents) throws CoreException {
        getContentObject().setSourceText(newContents, false);
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isContentParsable() throws CoreException {
    	if (!exists()) {
    		return false;
    	}
        return getContentObject().contentIsParsable();
    }

    /** 
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() throws CoreException {
        return getContentObject().getIpsObject();
    }
    
    private IpsSourceFileContents getContentObject() throws CoreException {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents!=null && contents.getSourceText()!=null) {
            return contents;
        }
        String source = getContentFromCorrespondingFile();
        IpsPlugin.getDefault().getManager().putSrcFileContents(this, source, getEncoding());
        return IpsPlugin.getDefault().getManager().getSrcFileContents(this); 
    }
    
    /**
     * Reads the content from the corresponding file and returns it as string 
     * The bytes read from disk are transformed into a string with the encoding
     * defined in the ips project.
     * 
     * @throws CoreException if an error occurs while reading the contents.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getXmlFileCharset()
     */
    public String getContentFromCorrespondingFile() throws CoreException {
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("IpsSrcFile.readContentFromCorrespondingFile " + this + " Thread: " + Thread.currentThread().getName());
    	}
    	InputStream is = getCorrespondingFile().getContents();
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
        return new IIpsSrcFileMemento(this, getContents(), isDirty());
    }
    
    /**
     * {@inheritDoc}
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        getContentObject().updateStateFromMemento(memento);
    }
    
    String getEncoding() throws CoreException {
        return getIpsProject().getXmlFileCharset();
    }

    /**
     * {@inheritDoc}
     */
    public QualifiedNameType getQualifiedNameType() {
        StringBuffer buf = new StringBuffer();
        String packageFragmentName = getIpsPackageFragment().getName();
        if(!StringUtils.isEmpty(packageFragmentName)){
            buf.append(getIpsPackageFragment().getName());
            buf.append('.');
        }
        
        buf.append(StringUtil.getFilenameWithoutExtension(getName()));
        return new QualifiedNameType(buf.toString(), getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
	public boolean isMutable() {
		return true;
	}

}

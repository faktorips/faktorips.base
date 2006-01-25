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
import org.eclipse.jdt.core.ICompilationUnit;
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
    
    /**
     * @param parent
     * @param name
     */
    IpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getIpsPackageFragment()
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return (IIpsPackageFragment)getParent();
    }
    
    public IFile getArtefactFile(){
        throw new RuntimeException("Not implemented yet.");
    }
    
    public IFile createArtefactFile(){
        throw new RuntimeException("Not implemented yet.");
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getAllJavaCompilationUnits()
     */
    public ICompilationUnit[] getAllJavaCompilationUnits() throws CoreException {
        String objectName = StringUtil.getFilenameWithoutExtension(getName());
        return getIpsObjectType().getAllJavaCompilationUnits(getIpsPackageFragment(), objectName);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getIpsObjectType()
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(getCorrespondingFile().getFileExtension());
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        return getCorrespondingFile();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IFile getCorrespondingFile() {
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFile(getName());
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() throws CoreException {
        if (isContentParsable()) {
            return new IIpsElement[]{getIpsObject()};
        }
        return NO_CHILDREN; 
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsSrcFile.gif");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#isDirty()
     */
    public boolean isDirty() {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents==null) {
            return false;
        }
        return contents.modified();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#markAsClean()
     */ 
    public void markAsClean() {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents!=null) {
            contents.markAsUnmodified();
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#discardChanges()
     */
    public void discardChanges() {
        IpsPlugin.getDefault().getManager().removeSrcFileContents(this);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#save()
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        if (!isDirty()) {
            return;
        }
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(getContents().getBytes(getEncoding()));
            getCorrespondingFile().setContents(is, force, true, monitor);
            // notify listeners because dirty state has changed!
            ContentChangeEvent event = new ContentChangeEvent(this);
            ((IpsModel)getIpsModel()).notifyChangeListeners(event);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getContents()
     */
    public String getContents() throws CoreException {
        return getContentObject().getSourceText();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#setContents(java.lang.String)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#isContentParsable()
     */
    public boolean isContentParsable() throws CoreException {
        return getContentObject().contentIsParsable();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getIpsObject()
     */
    public IIpsObject getIpsObject() throws CoreException {
        return getContentObject().getIpsObject();
    }
    
    private IpsSourceFileContents getContentObject() throws CoreException {
        IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
        if (contents!=null) {
            return contents;
        }
        InputStream is = getCorrespondingFile().getContents();
        try {
            String source = StringUtil.readFromInputStream(is, getEncoding());
            contents = new IpsSourceFileContents(this, source, getEncoding()); 
	        IpsPlugin.getDefault().getManager().putSrcFileContents(this, contents);
	        return contents; 
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#newMemento()
     */
    public IIpsSrcFileMemento newMemento() throws CoreException {
        return new IIpsSrcFileMemento(this, getContents(), isDirty());
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#setMemento(org.faktorips.devtools.core.model.IIpsSrcFileMemento)
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        getContentObject().updateStateFromMemento(memento);
    }
    
    private String getEncoding() throws CoreException {
        return getIpsProject().getXmlFileCharset();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getQualifiedNameType()
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

}

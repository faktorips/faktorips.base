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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of <code>IpsPackageFragment<code>.
 */
public class IpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#getRoot()
     */
    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        String path = name.replace('.', IPath.SEPARATOR);
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFolder(new Path(path));
    }
    
    /**
     * Overridden
     * @throws CoreException 
     */
    public IIpsPackageFragment[] getIpsChildPackageFragments() throws CoreException {
    	IFolder folder = (IFolder)getCorrespondingResource();
    	IResource[] content = folder.members();
    	IIpsPackageFragment[] result = new IIpsPackageFragment[content.length];
    	int count = 0;
    	for (int i = 0; i < content.length; i++) {
    		if (content[i].getType() == IFolder.FOLDER) {
        		String packageName = this.getName().equals("")?content[i].getName():this.getName() + "." + content[i].getName(); //$NON-NLS-1$ //$NON-NLS-2$
        		result[count] = new IpsPackageFragment(this.getParent(), packageName);  
        		count++;
    		}
    	}
    	
    	IIpsPackageFragment[] shrink = new IIpsPackageFragment[count];
    	System.arraycopy(result, 0, shrink, 0, count);
    	return shrink;
	}

    /**
     * Overridden
     */
	public IIpsPackageFragment getIpsParentPackageFragment() {
		IFolder folder = (IFolder)getCorrespondingResource();
		
		// if the default-package is asked for its parent, null is returned
		IFolder defaultPackage = (IFolder)this.getRoot().getIpsDefaultPackageFragment().getCorrespondingResource();
		if (folder.equals(defaultPackage)) {
			return null;
		}
		
		// if the parent of this one is the default-package, return it :-)
		IFolder parent = (IFolder)folder.getParent();
		if (parent.equals(defaultPackage)) {
			return this.getRoot().getIpsDefaultPackageFragment();
		}
		
		return new IpsPackageFragment(this.getParent(), folder.getParent().getName());
	}

	/**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        IIpsElement[] children = new IIpsElement[members.length];
        int counter = 0;
        for (int i=0; i<children.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (IpsObjectType.getTypeForExtension(file.getFileExtension())!=null) {
                    children[counter] = new IpsSrcFile(this, file.getName());
                    counter++;
                }
            }
        }
        if (counter==children.length) {
            return children;
        }
        IIpsElement[] shrinked = new IIpsElement[counter];
        System.arraycopy(children, 0, shrinked, 0, counter);
        return shrinked;
    }
    
    /** 
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsPackageFragment.gif"); //$NON-NLS-1$
    }
    
    /**
     * Overridden.
     */
    public IIpsSrcFile getIpsSrcFile(String name) {
    	
    	IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    	if(type != null){
    		return new IpsSrcFile(this, name);
    	}
        return null;
    }

    /** 
     * Overridden.
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IFile file = folder.getFile(name);
        file.create(source, force, monitor);
        return getIpsSrcFile(name);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#createPdFile(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor) throws CoreException {
        try {
	        InputStream is = new ByteArrayInputStream(content.getBytes(StringUtil.CHARSET_UTF8));
	        return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /** 
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor) throws CoreException {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));
        
        if (type == IpsObjectType.PRODUCT_CMPT) {
        	((IProductCmpt)ipsObject).setRuntimeId(ipsObject.getIpsProject().getRuntimeId((IProductCmpt)ipsObject));
        }
        
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element element = ipsObject.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            return createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        }        
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name, IIpsObject template, GregorianCalendar date, boolean force, IProgressMonitor monitor) throws CoreException {
    	IpsObjectType type = template.getIpsObjectType();
    	String filename = type.getFileName(name);
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
    	Element element;

    	IIpsSrcFile file;
    	if (template instanceof ITimedIpsObject) {
    		file = createIpsFile(type, name, force, monitor);
    		ITimedIpsObject newObject = (ITimedIpsObject)file.getIpsObject();
    		IIpsObjectGeneration target = newObject.newGeneration();
    		IIpsObjectGeneration source = ((ITimedIpsObject)template).findGenerationEffectiveOn(date);
    		target.initFromGeneration(source);
    		target.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
    		if (template instanceof IProductCmpt) {
    			((IProductCmpt)newObject).setPolicyCmptType(((IProductCmpt)template).getPolicyCmptType());
    		}
    		file.save(true, null);
    		
    		if (type == IpsObjectType.PRODUCT_CMPT) {
            	((IProductCmpt)newObject).setRuntimeId(newObject.getIpsProject().getRuntimeId((IProductCmpt)newObject));
    		}
    	}
    	else {
    		element = template.toXml(doc);
            try {
                String encoding = getIpsProject().getXmlFileCharset();
                String contents = XmlUtil.nodeToString(element, encoding);
                file = createIpsFile(filename, contents, force, monitor);
            } catch (TransformerException e) {
                throw new RuntimeException(e); 
            }        
    	}
    	return file;
	}

	/**
     * Searches all objects of the given type and adds them to the result. 
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (int i=0; i<members.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type == IpsObjectType.PRODUCT_CMPT_TYPE && IpsObjectType.POLICY_CMPT_TYPE.getFileExtension().equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject()!=null) {
                    	IPolicyCmptType policyCmptType = (IPolicyCmptType)srcFile.getIpsObject();
                    	IProductCmptType productCmptType = policyCmptType.findProductCmptType();
                    	if (productCmptType != null) {
                            result.add(productCmptType);    
                    	}
                    }
                } else if (type.getFileExtension().equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject()!=null) {
                        result.add(srcFile.getIpsObject());    
                    }
                }
            }
        }
    }

    /**
     * Searches all objects of the given type starting with the given prefix and adds
     * them to the result.
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     *  
     */
    public void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result) throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (int i=0; i<members.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    String filename = ignoreCase ? file.getName().toLowerCase() : file.getName();
                    if (filename.startsWith(newPrefix)) {
                        IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                        if (srcFile.getIpsObject()!=null) {
                            result.add(srcFile.getIpsObject());    
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#getRelativePath()
     */
    public IPath getRelativePath() {
        return new Path(getName().replace('.', '/'));
    }
    
    /**
     * Overridden
     */
	public String getFolderName() {
		return this.getCorrespondingResource().getName();
	}

	public boolean isDefaultPacakge() {
		return this.name.equals(""); //$NON-NLS-1$
	}
    
    
}

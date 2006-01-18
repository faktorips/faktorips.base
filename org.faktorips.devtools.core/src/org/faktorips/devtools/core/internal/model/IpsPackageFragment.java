package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
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
    	IResource[] content = folder.members(IFolder.FOLDER);
    	IpsPackageFragment[] result = new IpsPackageFragment[content.length];
    	for (int i = 0; i < content.length; i++) {
    		String packageName = this.getName().equals("")?content[i].getName():this.getName() + "." + content[i].getName();
    		result[i] = new IpsPackageFragment(this.getParent(), packageName);
    	}
    	return result;
	}

    /**
     * Overridden
     */
	public IIpsPackageFragment getIpsParentPackageFragment() {
		IFolder folder = (IFolder)getCorrespondingResource();
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsPackageFragment.gif");
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#getIpsSrcFile(java.lang.String)
     */
    public IIpsSrcFile getIpsSrcFile(String name) {
        return new IpsSrcFile(this, name);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#createPdFile(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#createIpsFile(org.faktorips.devtools.core.model.IpsObjectType, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String pdObjectName, boolean force, IProgressMonitor monitor) throws CoreException {
        String filename = type.getFileName(pdObjectName);
        IIpsObject pdObject = type.newObject(getIpsSrcFile(filename));
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element element = pdObject.toXml(doc);
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
     * Searches all objects of the given type and adds them to the result. 
     */
    void findPdObjects(IpsObjectType type, List result) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (int i=0; i<members.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type.getFileExtension().equals(file.getFileExtension())) {
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
    void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result) throws CoreException {
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#getJavaPackageFragment(int)
     */ 
    public IPackageFragment getJavaPackageFragment(int type) throws CoreException {
        String javaPackName = getRoot().getJavaPackagePrefix(type);
		if (javaPackName.length() > 0 && getName().length()>0) {
		    javaPackName = javaPackName + '.';
		}
		javaPackName = javaPackName + getName();
		int rootKind = type == JAVA_PACK_EXTENSION ? IIpsPackageFragmentRoot.JAVA_ROOT_EXTENSION_CODE 
		        : IIpsPackageFragmentRoot.JAVA_ROOT_GENERATED_CODE;
        return getRoot().getJavaPackageFragmentRoot(rootKind).getPackageFragment(javaPackName);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#getAllJavaPackageFragments()
     */
    public IPackageFragment[] getAllJavaPackageFragments() throws CoreException {
        IPackageFragment[] javaPacks = new IPackageFragment[3];
        javaPacks[0] = getJavaPackageFragment(JAVA_PACK_IMPLEMENTATION);
        javaPacks[1] = getJavaPackageFragment(JAVA_PACK_PUBLISHED_INTERFACE);
        javaPacks[2] = getJavaPackageFragment(JAVA_PACK_EXTENSION);
        return javaPacks;
    }
}

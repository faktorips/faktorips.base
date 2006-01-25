package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of IpsPackageFragmentRoot.
 */
public class IpsPackageFragmentRoot extends IpsElement implements IIpsPackageFragmentRoot {

    /**
     * Creates a new ips package fragment root with the indicated parent and name.
     */
    IpsPackageFragmentRoot(IIpsElement parent, String name) {
        super(parent, name);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#containsSourceFiles()
     */
    public boolean containsSourceFiles() {
        return true;
    }
    
    /**
     * Returns the artefact destination for the artefacts generated on behalf of the ips objects within this
     * ips package fragment root.
     */
    public IFolder getArtefactDestination() throws CoreException{
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getOutputFolderForGeneratedJavaFiles();
    }

    /**
     * Overridden.
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("IpsPackageFragmentRoot does not exist!"));
        }
        IIpsObjectPathEntry[] entries = getIpsProject().getIpsObjectPath().getEntries();
        for (int i=0; i<entries.length; i++) {
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)) {
                IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)entries[i];
                if (entry.getIpsPackageFragmentRoot(getIpsProject()).equals(this)) {
                    return entry;
                }
            }
        }
        throw new CoreException(new IpsStatus("No IpsObjectPathEntry found for package fragment " + this));
    }
    
    /**
     * Returns the table of contents for this package fragment root.
     * 
     * @throws CoreException if an error occurs while accessing the toc file.
     * 
     * @see saveProductCmptRegistryToc()
     */
    public MutableClRuntimeRepositoryToc getRuntimeRepositoryToc() throws CoreException {
        return ((IpsModel)getIpsModel()).getRuntimeRepositoryToc(this);
    }
    
    /**
     * Returns the file in the package root's associated output folder, that contains the table of contents.
     * 
     * @see saveProductCmptRegistryToc()
     * 
     * @throws CoreException if an error occurs while determining the file.
     */
	public IFile getTocFileInOutputFolder() throws CoreException {
	    IFolder folder;
        IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        String pathToPack = getJavaPackagePrefix(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE).replace('.', '/');
        if (pathToPack.length()==0) {
            folder = srcEntry.getOutputFolderForGeneratedJavaFiles();
        } else {
            folder = srcEntry.getOutputFolderForGeneratedJavaFiles().getFolder(pathToPack);
        }
        return folder.getFile(ClassloaderRuntimeRepository.TABLE_OF_CONTENTS_FILE);
	}
	
	/**
	 * Saves the product component registry's table of contents to a file. The table of contents file is needed
	 * by the FaktorIPS runtime's ClassloaderProductCmptRegistry to load the product component's.
	 * 
	 * @throws CoreException if an error occurs while writing the toc to the file.
	 */
	public void saveProductCmptRegistryToc() throws CoreException {
        IFile tocFile = getTocFileInOutputFolder();
        if (tocFile==null) {
            return;
        }
        String encoding = getIpsProject().getProject().getDefaultCharset();
        if (encoding==null) {
            return;
        }
        String xml = null;
        try {
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
            Element registryElement = doc.createElement(ClassloaderRuntimeRepository.REPOSITORY_XML_ELEMENT);
            Element tocElement = getRuntimeRepositoryToc().toXml(doc);
            registryElement.appendChild(tocElement);
            xml = XmlUtil.nodeToString(registryElement, encoding);
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus("Error transforming product component registry's table of contents to xml.", e));
        }
        InputStream is;
        try {
            is = new ByteArrayInputStream(xml.getBytes(encoding));
        } catch (UnsupportedEncodingException e1) {
            throw new CoreException(new IpsStatus(e1));
        }
        if (tocFile.exists()) {
            tocFile.setContents(is, true, true, null);
        } else {
            if (!tocFile.getParent().exists()) {
                createFolder((IFolder)tocFile.getParent());
            }
            tocFile.create(is, true, null);
        }
	}
	
    private void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getIpsProject()
     */
    public IIpsProject getIpsProject() {
        return (IIpsProject)parent;
    }
    
    /**
     * A root fragment exists if the underlying resource exists and the root
     * fragment is on the object path.
     * <p>   
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#exists()
     */
    public boolean exists() {
        if (!getCorrespondingResource().exists()) {
            return false;
        }
        IIpsPackageFragmentRoot[] roots;
        try {
            roots = getIpsProject().getIpsPackageFragmentRoots();    
        } catch (CoreException e) {
            return false;
        }
        for (int i=0; i<roots.length; i++) {
            if (roots[i].equals(this)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getJavaPackageFragmentRoot(int)
     */
    public IPackageFragmentRoot getJavaPackageFragmentRoot(int kind) throws CoreException {
        IIpsObjectPathEntry entry = getIpsObjectPathEntry();
        if (!entry.getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)) {
            throw new CoreException(new IpsStatus("Can't get Java package fragement root for " + this));
        }
        IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entry;
        switch (kind) {
        	case JAVA_ROOT_GENERATED_CODE:
        	    return getIpsProject().getJavaProject().getPackageFragmentRoot(srcEntry.getOutputFolderForGeneratedJavaFiles());
        	case JAVA_ROOT_EXTENSION_CODE:
        	    return getIpsProject().getJavaProject().getPackageFragmentRoot(srcEntry.getOutputFolderForExtensionJavaFiles());
        }
        throw new IllegalArgumentException("Unknown kind " + kind);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getIpsPackageFragments()
     */
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        List list = new ArrayList();
        list.add(new IpsPackageFragment(this, "")); // add the default package
        getIpsPackageFragments(folder, "", list);
        IIpsPackageFragment[] pdFolders = new IIpsPackageFragment[list.size()];  
        list.toArray(pdFolders);
        return pdFolders;
    }
    
    /*
     * Creates the PdFolders based on the contents of the given platform folder
     * and adds them to the list.
     * This is an application of the collecting parameter pattern. 
     */
    private void getIpsPackageFragments(IFolder folder, String namePrefix, List packs) throws CoreException {
        IResource[] resources = folder.members();
        for (int i=0; i<resources.length; i++) {
            if (resources[i].getType()==IResource.FOLDER) {
                String name = namePrefix + resources[i].getName();
                // package name is not the platform folder name, but the concatenation
                // of platform folder names starting at the root folder separated by dots
                packs.add(new IpsPackageFragment(this, name));
                getIpsPackageFragments((IFolder)resources[i], name + ".", packs);
            }
        }
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getIpsPackageFragment(java.lang.String)
     */
    public IIpsPackageFragment getIpsPackageFragment(String name) {
        return new IpsPackageFragment(this, name);
    }
    
    /**
     * Overridden
     */
    public IIpsPackageFragment getIpsDefaultPackageFragment() {
		return this.getIpsPackageFragment("");
	}

	/** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#createPackageFragment(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        StringTokenizer tokenizer = new StringTokenizer(name, ".");
        while (tokenizer.hasMoreTokens()) {
            folder = folder.getFolder(tokenizer.nextToken());
            if (!folder.exists()) {
                folder.create(force, true, monitor);
            }
        }
        return getIpsPackageFragment(name);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        IProject project = (IProject)getParent().getCorrespondingResource();
        return project.getFolder(getName());
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragments();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsPackageFragmentRoot.gif");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getIpsObject(org.faktorips.devtools.core.model.IpsObjectType, java.lang.String)
     */
    public IIpsObject getIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return new QualifiedNameType(qualifiedName, type).getIpsObject(this);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getIpsObject(org.faktorips.devtools.core.model.QualifiedNameType)
     */
    public IIpsObject getIpsObject(QualifiedNameType nameType) throws CoreException {
        return nameType.getIpsObject(this);
    }

    /**
     * Searches all objects of the given type in the root folder and adds
     * them to the result. 
     */
    void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        if (!exists()) {
            return;
        }
        IIpsPackageFragment[] folders = this.getIpsPackageFragments();
        for (int i=0; i<folders.length; i++) {
            ((IpsPackageFragment)folders[i]).findPdObjects(type, result);
        }
    }
    
    /**
     * Searches all objects of the given type starting with the given prefix in this root folder and adds
     * them to the result. 
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     */
    void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result) throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IIpsPackageFragment[] packs = getIpsPackageFragments();
        for (int i=0; i<packs.length; i++) {
            ((IpsPackageFragment)packs[i]).findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
        }
    }
    
    /**
     * Searches all product components that are based on the given policy component type
     * (either directly or because they are based on a subtype of the given
     * type) and adds them to the result. If pcTypeName is <code>null</code>, returns
     * all product components found in the fragment root.
     * 
     * @param pcTypeName The qualified name of the policy component type, product components are searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based on subtypes
     * of the given policy component are returned, otherwise only product components that are directly based
     * on the given type are returned.
     * @param result List in which the product components being found are stored in.
     */
    void findProductCmpts(String pcTypeName, boolean includeSubtypes, List result) throws CoreException {
        List allCmpts = new ArrayList(100);
        IPolicyCmptType pcType = null;
        ITypeHierarchy hierarchy = null;
        if (includeSubtypes && pcTypeName!=null) {
            pcType = getIpsProject().findPolicyCmptType(pcTypeName);
            if (pcType!=null) {
                hierarchy = pcType.getSubtypeHierarchy();
            }
        }
        findIpsObjects(IpsObjectType.PRODUCT_CMPT, allCmpts);
        for (Iterator it=allCmpts.iterator(); it.hasNext(); ) {
            IProductCmpt each = (IProductCmpt)it.next();
            if (pcTypeName==null || pcTypeName.equals(each.getPolicyCmptType())) {
                result.add(each);
            } else if (hierarchy!=null) {
                IPolicyCmptType eachPcType = getIpsProject().findPolicyCmptType(each.getPolicyCmptType());
                if (hierarchy.isSubtypeOf(eachPcType, pcType)) {
                    result.add(each);
                }
            }
        }
    }
    
    /**
     * Overridden method.
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getJavaPackagePrefix(int)
     */
    public String getJavaPackagePrefix(int kind) throws CoreException {
        IIpsObjectPathEntry entry = getIpsObjectPathEntry();
        if (!entry.getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER)) {
            throw new CoreException(new IpsStatus("Can't get Java package prefix for " + this));
        }
        // TODO: falsch, exension ber?cksichtigen.
        String basePackage = ((IIpsSrcFolderEntry)entry).getBasePackageNameForGeneratedJavaClasses();
        switch (kind) {
        	case IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION:
        	    return basePackage.equals("") ? "internal" : basePackage + ".internal";
        	case IIpsPackageFragment.JAVA_PACK_EXTENSION:
        	    return basePackage.equals("") ? "internal" : basePackage + ".internal";
        	case IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE:
        	    return basePackage;
        	default:
                throw new IllegalArgumentException("Can't get Java package prefix for " + this + ". Kind " + kind + " is illegal.");
        }
    }
    
    /**
     * Overridden method.
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsPackageFragmentRoot#getJavaPackagePrefixes()
     */
    public String[] getJavaPackagePrefixes() throws CoreException {
        String[] prefixes = new String[2];
        prefixes[0] = getJavaPackagePrefix(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
        prefixes[1] = getJavaPackagePrefix(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE);;
        return prefixes;
    }
    
    IIpsSrcFile findIpsSrcFile(ICompilationUnit cu) throws CoreException {
        String ipsPackName = null;
        String prefix = "";
        String javaPackName = cu.getParent().getElementName();
        String[] prefixes = getJavaPackagePrefixes();
        for (int i=0; i<prefixes.length; i++) {
            if (javaPackName.startsWith(prefixes[i])) {
                prefix = prefixes[i];
                if (prefix.equals("")) {
                    ipsPackName = javaPackName;
                } else {
                    // also remove the separating dot (.)
                    ipsPackName = javaPackName.substring(prefix.length()+1);
                }
                break;
            }
        }
        if (ipsPackName==null) {
            return null;
        }
        String cuName = cu.getElementName();
        String ipsObjectName = null;
        IIpsPackageFragment pack = getIpsPackageFragment(ipsPackName);
        if (prefix.equals(getJavaPackagePrefix(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION))) {
            int pos = cuName.lastIndexOf("Impl.java");
            if (pos>0) {
                ipsObjectName = cuName.substring(0, pos);
            } else {}
        } else if (prefix.equals(getJavaPackagePrefix(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE))) {
            int pos = cuName.lastIndexOf(".java");
            if (pos>0) {
                ipsObjectName = cuName.substring(0, pos);
            } else {}
        }
        if (ipsObjectName!=null) {
            return pack.getIpsSrcFile(ipsObjectName + "." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        }
        return null;
    }
}

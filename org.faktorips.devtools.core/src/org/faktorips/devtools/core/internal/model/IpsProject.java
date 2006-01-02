package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumType;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IpsBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 */
public class IpsProject extends IpsElement implements IIpsProject {

    /**
     * Constructor needed for <code>IProject.getNature()</code> and
     * <code>IProject.addNature()</code>.
     * 
     * @see #setProject(IProject)
     */
    public IpsProject() {
    }

    IpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    public IProject getProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getJavaProject()
     */
    public IJavaProject getJavaProject() {
        return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(getName());
    }

    /**
     * Overridden
     */
    public String getXmlFileCharset() {
        return "UTF-8";
    }

    /**
     * Overridden
     */
    public IIpsObjectPath getIpsObjectPath() throws CoreException {
        return ((IpsModel)getIpsModel()).getIpsObjectPath(this);
    }

    private Document getIpsProjectDocument() throws CoreException{
        IFile file = getIpsObjectPathFile();
        if (file.exists()) {
            InputStream contents = file.getContents();
            try {
                return IpsPlugin.getDefault().newDocumentBuilder().parse(contents);
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(
                        "Error while reading the content of the ipsobjectpath.xml file", e));
            }
            finally{
                try{
                    contents.close();    
                } catch(IOException e){
                    IpsPlugin.log(new IpsStatus("Unable to free resource.", e));
                }
            }
        } 
    
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("IpsProject"));
        return doc;
    }
    
    private void setIpsProjectProperty(Element newValue, Document doc) throws CoreException {
        // TODO pe 12-10-05: the ipsobjectpath file doesn't contain just the ipsobjectpath any
        // longer. It now contains the
        // setting for the ipsproject. This has to be cleaned up. The ipsProject probably needs to
        // have
        // toXml() and createFromXml() methods
        Element ipsProjectEl = doc.getDocumentElement();
        IFile file = getIpsObjectPathFile();
        String charset;
        if (file.exists()) {
            charset = file.getCharset();
        } else {
            charset = getProject().getDefaultCharset();
        }
        
        Node oldValue = XmlUtil.getFirstElement(ipsProjectEl, newValue.getTagName());
        if (oldValue == null) {
            ipsProjectEl.appendChild(newValue);
        } else {
            ipsProjectEl.replaceChild(newValue, oldValue);
        }

        String contents;
        try {
            contents = XmlUtil.nodeToString(ipsProjectEl, charset);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Error tranforming ips object path to xml string", e));
        }
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(contents.getBytes(charset));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("Error creating byte stream", e));
        }
        if (file.exists()) {
            file.setContents(is, true, true, null);
        } else {
            file.create(is, true, null);
        }
    }

    /**
     * Overridden.
     */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException {
        Document doc = getIpsProjectDocument();
        Element artefactElement = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
        artefactElement.setAttribute("id", id);
        setIpsProjectProperty(artefactElement, doc);
        ((IpsModel)getIpsModel()).invalidateCurrentArtefactBuilderSet(this);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#setIpsObjectPath(org.faktorips.devtools.core.model.IIpsObjectPath)
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException {
        Document doc = getIpsProjectDocument();
        setIpsProjectProperty(((IpsObjectPath)newPath).toXml(doc), doc);
        ((IpsModel)getIpsModel()).invalidateIpsObjectPath(this);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getIpsObjectPathFile()
     */
    public IFile getIpsObjectPathFile() {
        return getProject().getFile("ipsobjectpath.xml");
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getDatatypesDefinitionFile()
     */
    public IFile getDatatypesDefinitionFile() {
        return getProject().getFile("ipsdatatypes.xml");
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getIpsPackageFragmentRoot(java.lang.String)
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        return new IpsPackageFragmentRoot(this, name);
    }

    /**
     * Overridden IMethod.
     * 
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsProject#getIpsPackageFragmentRoots()
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException {
        List roots = new ArrayList();
        IIpsObjectPathEntry[] entries = getIpsObjectPath().getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof IIpsSrcFolderEntry) {
                roots.add(((IIpsSrcFolderEntry)entries[i]).getIpsPackageFragmentRoot());
            }
        }
        return (IIpsPackageFragmentRoot[])roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsPackageFragment#exists()
     */
    public boolean exists() {
        return getCorrespondingResource().exists();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getExpressionLanguageFunctionsLanguage()
     */
    public Locale getExpressionLanguageFunctionsLanguage() {
        return Locale.GERMAN;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getGeneratedJavaSourcecodeDocumentationLanguage()
     */
    public Locale getGeneratedJavaSourcecodeDocumentationLanguage() {
        return Locale.GERMAN;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsProject.gif");
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        return getProject();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragmentRoots();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getIpsProject()
     */
    public IIpsProject getIpsProject() {
        return this;
    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#configure()
     */
    public void configure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand command = getPdBuildCommand();
        if (command == null) {
            // Add a product definition build command to the build spec
            ICommand newBuildCommand = description.newCommand();
            newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
            addCommandAtFirstPosition(description, newBuildCommand);
        }
    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException {
    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
        this.name = project.getName();
    }

    /**
     * Finds the specific command for product definition builder.
     */
    private ICommand getPdBuildCommand() throws CoreException {
        ICommand[] commands = getProject().getDescription().getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(IpsBuilder.BUILDER_ID)) {
                return commands[i];
            }
        }
        return null;
    }

    /*
     * Adds the command to the build spec
     */
    private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand)
            throws CoreException {

        ICommand[] oldCommands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[oldCommands.length + 1];
        System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
        newCommands[0] = newCommand;
        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsProject#findIpsObject(org.faktorips.devtools.core.model.QualifiedNameType)
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObject(nameType);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getPdObject(org.faktorips.devtools.core.model.IpsObjectType,
     *      java.lang.String)
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObject(type, qualifiedName);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getPdObject(org.faktorips.devtools.core.model.IpsObjectType,
     *      java.lang.String)
     */
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase) throws CoreException {
        ArrayList result = new ArrayList();
        findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }

    /**
     * Searches all objects of the given type starting with the given prefix found on the project's
     * path and adds them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the objects.
     */
    public void findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result) throws CoreException {
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjectsStartingWith(type, prefix,
            ignoreCase, result);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findPolicyCmptType(java.lang.String)
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }
    
    

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findIpsObjects(org.faktorips.devtools.core.model.IpsObjectType)
     */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(type);
    }

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * @throws CoreException
     */
    public void findAllIpsObjects(List result) throws CoreException{
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(IpsObjectType.PRODUCT_CMPT, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(IpsObjectType.TABLE_STRUCTURE, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(IpsObjectType.TABLE_CONTENTS, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(IpsObjectType.BUSINESS_FUNCTION, result);
    }
    
    /**
     * Finds all ips objects of the given type in the project and adds them to the result.
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(type, result);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getValueDatatypes(boolean)
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid) {
        Set result = new HashSet();
        getValueDatatypes(includeVoid, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
    }

    private void getValueDatatypes(boolean includeVoid, Set result) {
        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        ((IpsModel)getIpsModel()).getValueDatatypes(this, result);
        try {
            IIpsProject[] projects = getIpsObjectPath().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                ((IpsModel)getIpsModel()).getValueDatatypes(projects[i], result);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findDatatypes()
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid)
            throws CoreException {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, result);
        if (!valuetypesOnly) {
            List refDatatypes = new ArrayList();
            findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, refDatatypes);
            result.addAll(refDatatypes);
        }
        Datatype[] array = new Datatype[result.size()];
        result.toArray(array);
        return array;
    }

    /**
     * Overridden method.
     * 
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsProject#findDatatype(java.lang.String)
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException {
        Datatype[] datatypes = findDatatypes(false, true);
        for (int i = 0; i < datatypes.length; i++) {
            if (datatypes[i].getQualifiedName().equals(qualifiedName)) {
                return datatypes[i];
            }
        }
        return null;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findValueDatatype(java.lang.String)
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException {
        // TODO Jan. Testfall, noch mal prüfen.
        return (ValueDatatype)findDatatype(qualifiedName);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getDatatypeHelper(org.faktorips.datatype.Datatype)
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        DatatypeHelper helper = ((IpsModel)getIpsModel()).getDatatypeHelper(this,
            (ValueDatatype)datatype);
        if (helper != null) {
            return helper;
        }
        try {
            IIpsProject[] projects = getIpsObjectPath().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                helper = ((IpsModel)getIpsModel()).getDatatypeHelper(projects[i],
                    (ValueDatatype)datatype);
                if (helper != null) {
                    return helper;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getValueSetTypes(org.faktorips.datatype.ValueDatatype)
     */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException {
        ArgumentCheck.notNull(datatype);
        if (datatype instanceof EnumType) {
            return new ValueSetType[] { ValueSetType.ALL_VALUES, ValueSetType.ENUM };
        }
        return ValueSetType.getValueSetTypes();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findProductCmpts(org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
     */
    public IProductCmpt[] findProductCmpts(String qualifiedTypeName, boolean includeSubytpes)
            throws CoreException {
        List result = new ArrayList();
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            ((IpsPackageFragmentRoot)roots[i]).findProductCmpts(qualifiedTypeName,
                includeSubytpes, result);
        }
        IProductCmpt[] array = new IProductCmpt[result.size()];
        result.toArray(array);
        return array;
    }

    /**
     * Overridden method.
     * 
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsProject#getSourceIpsPackageFragmentRoots()
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        getSourcePdPckFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    void getSourcePdPckFragmentRoots(List result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].containsSourceFiles()) {
                result.add(roots[i]);
            }
        }
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#findIpsSrcFile(org.eclipse.jdt.core.ICompilationUnit)
     */
    public IIpsSrcFile findIpsSrcFile(ICompilationUnit cu) throws CoreException {
        IPackageFragmentRoot javaRoot = (IPackageFragmentRoot)cu.getParent().getParent();
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i]
                    .getJavaPackageFragmentRoot(IIpsPackageFragmentRoot.JAVA_ROOT_GENERATED_CODE)
                    .equals(javaRoot)) {
                return ((IpsPackageFragmentRoot)roots[i]).findIpsSrcFile(cu);
            }
        }
        return null;
    }

    /**
     * Currently returns all artefact builder available at runtime. Later implementations might
     * restrict the set of builders available for this project
     * 
     * @throws CoreException
     * 
     * @see org.faktorips.devtools.core.model.IIpsProject#getAvailableArtefactBuilders()
     */
    public IIpsArtefactBuilderSet getCurrentArtefactBuilderSet() throws CoreException {
        return ((IpsModel)getIpsModel()).getCurrentIpsArtefactBuilderSet(this);
    }

    /**
     * Overridden.
     */
    public JavaCodeFragment getCodeToGetTheRuntimeRepository() throws CoreException {
        return new JavaCodeFragment("null"); // TODO must read from ipsproject file.
    }
    
    
    
    
}

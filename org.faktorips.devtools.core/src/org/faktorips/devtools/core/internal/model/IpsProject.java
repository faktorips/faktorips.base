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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IpsBuilder;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class IpsProject extends IpsElement implements IIpsProject {

    private IIpsProjectNamingConventions namingConventions = null;
	
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
        return ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean dependsOn(IIpsProject otherProject) throws CoreException {
        if (this.equals(otherProject)) {
            return false;
        }
        IIpsProject[] projects = getReferencedIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(otherProject)) {
                return true;
            } else {
                return projects[i].dependsOn(otherProject);
            }
        }
        return false;
    }

    /**
	 * {@inheritDoc}
	 */
	public IIpsProjectProperties getProperties() {
    	return new IpsProjectProperties(this, (IpsProjectProperties)getPropertiesInternal());
	}

	/*
	 * Returns the properties from the model.
	 */
	private IpsProjectProperties getPropertiesInternal() {
		return ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProperties(IIpsProjectProperties properties) throws CoreException {
		saveProjectProperties(new IpsProjectProperties(this, (IpsProjectProperties)properties));
	}

	/**
     * Saves the project properties to the .ipsproject file.
     * 
     * @throws CoreException if an error occurs while saving the data.
     */
    private void saveProjectProperties(IIpsProjectProperties properties) throws CoreException {
    	Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
    	Element propertiesEl = ((IpsProjectProperties)properties).toXml(doc);
        doc.appendChild(propertiesEl);
        IFile file = getIpsProjectPropertiesFile();
        String charset = getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(doc, charset);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Error tranforming project data to xml string", e)); //$NON-NLS-1$
        }
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(insertNewLineSeparatorsBeforeComment(contents).getBytes(charset));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("Error creating byte stream", e)); //$NON-NLS-1$
        }
        if (file.exists()) {
            file.setContents(is, true, true, null);
        } else {
            file.create(is, true, null);
        }
    }
    
    private String insertNewLineSeparatorsBeforeComment(String s) {
        StringBuffer newText = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(s, SystemUtils.LINE_SEPARATOR);
        boolean firstComment = true;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.indexOf("<!--")!=-1) { //$NON-NLS-1$
                if (firstComment) {
                    firstComment = false;
                } else {
                    newText.append(SystemUtils.LINE_SEPARATOR);
                    newText.append(SystemUtils.LINE_SEPARATOR);
                }
            }
            newText.append(token);
            newText.append(SystemUtils.LINE_SEPARATOR);
        }
        return newText.toString();
    }
    
	/**
     * {@inheritDoc}
	 */
    public IFile getIpsProjectPropertiesFile() {
    	return getProject().getFile(".ipsproject"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IJavaProject getJavaProject() {
        return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(getName());
    }
    
    /**
     * {@inheritDoc}
     * @throws CoreException 
     */
    public ClassLoader getClassLoaderForJavaProject() throws CoreException {
        return new ClassLoaderProvider(getJavaProject(), true).getClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean isJavaProjectErrorFree(boolean checkReferencedJavaProjects) throws CoreException {
        return isJavaProjectErrorFree(getJavaProject(), checkReferencedJavaProjects);
    }
    
    /**
     * {@inheritDoc}
     */
    private Boolean isJavaProjectErrorFree(IJavaProject javaProject, boolean checkReferencedJavaProjects) throws CoreException {
        IProject project = javaProject.getProject();
        if (!project.isAccessible()) {
            return null;
        }
        if (!javaProject.exists()) {
            return null;
        }
        // implementation note: if the java project has buildpath problems it also hasn't got a build state
        // so we first have to check for problems with the build path. We can't do this via markers as the build path markers
        // are created on a resource change event, and we don't now if it has been executed so far. 
        MessageList list = new MessageList();
        validateJavaProjectBuildPath(list);
        if (!list.isEmpty()) {
            return Boolean.FALSE;
        }
        IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
        if (containsErrorMarker(markers)) {
            return Boolean.FALSE;
        }
        if (checkReferencedJavaProjects) {
            List refProjectcs = getJavaProjectsReferencedInClasspath(javaProject);
            for (Iterator it=refProjectcs.iterator(); it.hasNext(); ) {
                IJavaProject refProject = (IJavaProject)it.next();
                Boolean errorFree = isJavaProjectErrorFree(refProject, true);
                if (errorFree!=null && !errorFree.booleanValue()) {
                    return errorFree;
                }
            }
        }
        if (!javaProject.hasBuildState()) {
            return null;
        }
        return Boolean.TRUE;
    }

    private boolean containsErrorMarker(IMarker[] markers) {
        for (int i = 0; i < markers.length; i++) {
            if (markers[i].getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)==IMarker.SEVERITY_ERROR) {
                return true;
            }
        }
        return false;
    }
    
    private List getJavaProjectsReferencedInClasspath(IJavaProject javaProject) throws JavaModelException {
        List result = new ArrayList();
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getEntryKind()==IClasspathEntry.CPE_PROJECT) {
                IJavaProject refProject = javaProject.getJavaModel().getJavaProject(entries[i].getPath().lastSegment());
                result.add(refProject);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject[] getReferencedIpsProjects() throws CoreException {
        return getIpsObjectPathInternal().getReferencedIpsProjects();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect) throws CoreException {
        if (otherProject==null || otherProject==this) {
            return false;
        }
        Set projectsVisited = new HashSet();
        return isReferencedBy(otherProject, considerIndirect, projectsVisited);
    }
    
    private boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect, Set projectsVisited) throws CoreException {
        IIpsObjectPath otherPath = ((IpsProject)otherProject).getIpsObjectPathInternal();
        IIpsProject[] referencedProjects = otherPath.getReferencedIpsProjects();
        for (int i = 0; i < referencedProjects.length; i++) {
            if (this.equals(referencedProjects[i])) {
                return true;
            }
            if (projectsVisited.contains(referencedProjects[i])) {
                continue;
            }
            if (considerIndirect && isReferencedBy(referencedProjects[i], considerIndirect, projectsVisited)) {
                return true;
            }
            projectsVisited.add(referencedProjects[i]);
        }
        return false;
    }
    

    /**
     * {@inheritDoc}
     */
    public IIpsProject[] getReferencingProjects(boolean includeIndirect) throws CoreException {
        IIpsProject[] projects = getIpsModel().getIpsProjects();
        List result = new ArrayList(projects.length);
        for (int i = 0; i < projects.length; i++) {
            if (isReferencedBy(projects[i], includeIndirect)) {
                result.add(projects[i]);
            }
        }
        return (IIpsProject[])result.toArray(new IIpsProject[result.size()]);
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean canBeBuild() {
		try {
			return !validate().containsErrorMsg();
		} catch (CoreException e) {
			IpsPlugin.log(e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public String getXmlFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPlainTextFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModelProject() {
		return getPropertiesInternal().isModelProject();
	}

    /**
     * {@inheritDoc}
     */
	public boolean isProductDefinitionProject() {
		return getPropertiesInternal().isProductDefinitionProject();
	}

	/**
	 * {@inheritDoc}
	 */
    public IIpsObjectPath getIpsObjectPath() throws CoreException {
    	return getProperties().getIpsObjectPath();
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder[] getOutputFolders() throws CoreException {
    	return getIpsObjectPathInternal().getOutputFolders();
    }

    /**
     * {@inheritDoc}
     */
	public boolean isAccessibleViaIpsObjectPath(IIpsObject ipsObject) throws CoreException {
        if (ipsObject==null) {
            return false;
        }
        IIpsObject objectOnPath = getIpsObjectPathInternal().findIpsObject(this, ipsObject.getQualifiedNameType(), new HashSet());
        return ipsObject==objectOnPath;
    }

    /**
	 * Returns a <strong>reference</strong> to the ips object path, in
	 * contrast to the getIpsObjectPath() method that returns a copy.
	 */
    public IpsObjectPath getIpsObjectPathInternal() throws CoreException {
    	return (IpsObjectPath)getPropertiesInternal().getIpsObjectPath();
    }

    /**
	 * {@inheritDoc}
	 */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException {
    	IIpsProjectProperties properties = getProperties();
    	properties.setBuilderSetId(id);
    	saveProjectProperties(properties);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public void setValueDatatypes(String[] ids) throws CoreException {
        IIpsProjectProperties properties = getProperties();
        properties.setPredefinedDatatypesUsed(ids);
        saveProjectProperties(properties);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException {
    	String[] ids = new String[types.length];
    	for (int i = 0; i < types.length; i++) {
			ids[i] = types[i].getQualifiedName();
		}
		setValueDatatypes(ids);
	}
    
    /**
     * {@inheritDoc}
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	properties.setIpsObjectPath(newPath);
    	saveProjectProperties(properties);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        try {
            if(!getNamingConventions().validateIpsPackageRootName(name).containsErrorMsg()){
                return new IpsPackageFragmentRoot(this, name);
            }
        }
        catch (CoreException e) {
            // nothing to do, return null
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException {
        List roots = new ArrayList();
        IIpsObjectPathEntry[] entries = getIpsObjectPathInternal().getEntries();
        for (int i = 0; i < entries.length; i++) {
            IIpsPackageFragmentRoot root = entries[i].getIpsPackageFragmentRoot(this);
            if (root!=null) {
                roots.add(root);
            }
        }
        return (IIpsPackageFragmentRoot[])roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(String name) {
        try {
            IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].getName().equals(name)){
                    return roots[i];
                }
            }
        }
        catch (CoreException e) {
            // nothing to do, return null
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
	public IResource[] getNonIpsResources() throws CoreException {
		IContainer cont= (IContainer) getCorrespondingResource();
    	List childResources= new ArrayList(); 
        IResource[] children= cont.members();
        for (int i = 0; i < children.length; i++) {
    		if(!isPackageFragmentRoot(children[i]) & !isJavaFolder(children[i])){
    			childResources.add(children[i]);
    		}
		}
        IResource[] resArray = new IResource[childResources.size()];
        return (IResource[]) childResources.toArray(resArray);
	}
	
	/**
	 * Examins the <code>JavaProject</code> corresponding to this <code>IpsProject</code> 
	 * and its relation to the given <code>IResource</code>.
	 * Returns true if the given resource corresponds to a classpath entry of the 
	 * javaproject. Returns true if the given resource corresponds to a folder that is
	 * either the javaprojects default output location or the output location 
	 * of one of the projects classpathentries. False otherwise. 
	 * @param resource
	 * @return
	 */
	 private boolean isJavaFolder(IResource resource) {
		try {
			IPath outputPath= getJavaProject().getOutputLocation();
			IClasspathEntry[] entries= getJavaProject().getResolvedClasspath(true);
			if(resource.getFullPath().equals(outputPath)){
				return true;
			}
			for (int i = 0; i < entries.length; i++) {
				if(resource.getFullPath().equals(entries[i].getOutputLocation())){
					return true;
				}
				if(resource.getFullPath().equals(entries[i].getPath())){
					return true;
				}
			}
			return false;
		} catch (JavaModelException e) {
			IpsPlugin.log(e);
			return false;
		}
	}

	/**
	 * Returns true if the given IResource is a folder that corresponds to
	 * an IpsPackageFragmentRoot of this IpsProject, false otherwise.
	 */
	private boolean isPackageFragmentRoot(IResource res) throws CoreException {
		IIpsPackageFragmentRoot[] roots= getIpsPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			if(roots[i].getCorrespondingResource().equals(res)){
				return true;
			}
		}
		return false;
	}

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        if(!getCorrespondingResource().exists()){
            return false;
        }
        IProject project = getProject();
        try {
            String[] natures = project.getDescription().getNatureIds();
            for (int i = 0; i < natures.length; i++) {
                if(natures[i].equals(IIpsProject.NATURE_ID)){
                    return true;
                }
            }
        } catch (CoreException e) {
            // if we can't get the project nature, the project is not in a state we would consider full existance
            return false;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Locale getExpressionLanguageFunctionsLanguage() {
        return Locale.GERMAN;
    }

    /**
     * {@inheritDoc}
     */
    public Locale getGeneratedJavaSourcecodeDocumentationLanguage() {
    	IpsProjectProperties properties = getPropertiesInternal();
    	return properties.getJavaSrcLanguage();
    }

    /**
     * {@inheritDoc}
     */
	public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode() {
    	IpsProjectProperties properties = getPropertiesInternal();
    	return getIpsModel().getChangesOverTimeNamingConvention(properties.getChangesOverTimeNamingConventionIdForGeneratedCode());
	}

	/**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsProject.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return getProject();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragmentRoots();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand command = getIpsBuildCommand();
        if (command == null) {
            // Add a product definition build command to the build spec
            ICommand newBuildCommand = description.newCommand();
            newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
            addCommandAtFirstPosition(description, newBuildCommand);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deconfigure() throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    public void setProject(IProject project) {
        this.name = project.getName();
    }

    /**
     * Finds the specific command for product definition builder.
     */
    private ICommand getIpsBuildCommand() throws CoreException {
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
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        Set visitedEntries = new HashSet();
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObject(this, nameType, visitedEntries);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        if (qualifiedName==null || qualifiedName.equals("")) {
            return null;
        }
        Set visitedEntries = new HashSet();
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObject(this, type, qualifiedName, visitedEntries);
    }

    /**
     * {@inheritDoc}
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
        Set visitedEntries = new HashSet();
        ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObjectsStartingWith(this, type, prefix,
            ignoreCase, result, visitedEntries);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException {
        return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE_V2, qualifiedName);
    }

	/**
     * {@inheritDoc}
	 */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        Set visitedEntries = new HashSet();
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObjects(this, type, visitedEntries);
    }

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * @throws CoreException
     */
    public void findAllIpsObjects(List result) throws CoreException{
        Set visitedEntries = new HashSet();
        ((IpsObjectPath)getIpsObjectPathInternal()).findAllIpsObjects(this, result, visitedEntries);
    }
    
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        Set visitedEntries = new HashSet();
    	getIpsObjectPathInternal().findIpsObjects(this, type, result, visitedEntries);
    }

    public void findAllIpsObjectsOfSrcFolderEntries(List result) throws CoreException {
        Set visitedEntries = new HashSet();
        getIpsObjectPathInternal().findAllIpsObjectsOfSrcFolderEntries(this, result, visitedEntries);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid) {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, true, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public ValueDatatype[] getValueDatatypes(boolean includeVoid, boolean includePrimitives) {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, includePrimitives, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
	}

	private void getValueDatatypes(boolean includeVoid, boolean includePrimitives, Set result) {
        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        
        // add enum types defined in tables
		try {
			IIpsObject[] structures = (IIpsObject[])findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
	        for (int i = 0; i < structures.length; i++) {
                ITableStructure structure = (ITableStructure)structures[i]; 
				if (structure.isModelEnumType()) {
                    ArrayList enumTableContents = new ArrayList(structures.length);
                    findTableContents(structure, enumTableContents);
                    for (Iterator it = enumTableContents.iterator(); it.hasNext();) {
                        ITableContents contents = (ITableContents)it.next();
                        result.add(new TableContentsEnumDatatypeAdapter(contents));
                    }
				}
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
        
        getValueDatatypes(this, result, new HashSet());
        if (!includePrimitives) {
        	// remove primitives from the result
        	for (Iterator it=result.iterator(); it.hasNext(); ) {
        		ValueDatatype type = (ValueDatatype)it.next();
        		if (type.isPrimitive()) {
        			it.remove();
        		}
        	}
        }
    }
    
    private void getValueDatatypes(IIpsProject ipsProject, Set result, Set visitedProjects){
        try {
            ((IpsModel)getIpsModel()).getValueDatatypes(ipsProject, result);
            IIpsProject[] projects = ((IpsProject)ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                if(!visitedProjects.contains(projects[i])){
                	visitedProjects.add(projects[i]);
                	getValueDatatypes(projects[i], result, visitedProjects);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid)
            throws CoreException {
    	return findDatatypes(valuetypesOnly, includeVoid, true);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives) throws CoreException {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, includePrimitives, result);
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
     * {@inheritDoc}
     */
    public EnumDatatype[] findEnumDatatypes() throws CoreException{
    	//TODO this implementation can be improved and instanceof can be avoided. Therefore the storage of EnumDatatypes an Datatypes
    	//has to be separated within the IpsModel class
    	
    	Datatype[] datatypes = findDatatypes(true, false);
    	ArrayList enumDatatypeList = new ArrayList();
    	for (int i = 0; i < datatypes.length; i++) {
			if(datatypes[i] instanceof EnumDatatype){
				enumDatatypeList.add(datatypes[i]);
			}
		}
    	return (EnumDatatype[])enumDatatypeList.toArray(new EnumDatatype[enumDatatypeList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public EnumDatatype findEnumDatatype(String qualifiedName) throws CoreException{
    	EnumDatatype[] allEnums = findEnumDatatypes();
    	for (int i = 0; i < allEnums.length; i++) {
			if(allEnums[i].getQualifiedName().equals(qualifiedName)){
				return allEnums[i];
			}
		}
    	return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException {
    	if (qualifiedName.equals(Datatype.VOID.getQualifiedName())) {
    		return Datatype.VOID;
    	}
    	Datatype type = findValueDatatype(qualifiedName);
        if (type!=null) {
        	return type;
        }
    	int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
    	if(arrayDimension > 0){
    		qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
    	}
    	IpsObjectType[] objectTypes = IpsObjectType.ALL_TYPES;
    	for (int i = 0; i < objectTypes.length; i++) {
			if (objectTypes[i].isDatatype()) {
	    		type = (Datatype)findIpsObject(objectTypes[i], qualifiedName);
				if (type!=null) {
					break;
				}
			}
		}
        if (arrayDimension==0) {
        	return type;
        }
    	if(type instanceof ValueDatatype){
    		return new ArrayOfValueDatatype(type, arrayDimension);
    	}
    	throw new IllegalArgumentException("The qualified name: \"" + qualifiedName +  //$NON-NLS-1$
    			"\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException {
        if (qualifiedName==null) {
            return null;
        }
    	int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
    	if(arrayDimension > 0){
    		qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
    	}
    	ValueDatatype type = findValueDatatype(this, qualifiedName, new HashSet());
        if (arrayDimension==0) {
        	return type;
        }
    	if(type instanceof ValueDatatype){
    		return new ArrayOfValueDatatype(type, arrayDimension);
    	}
    	throw new IllegalArgumentException("The qualified name: \"" + qualifiedName +  //$NON-NLS-1$
    			"\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private ValueDatatype findValueDatatype(IpsProject ipsProject, String qualifiedName, HashSet visitedProjects)
            throws CoreException {
        ValueDatatype datatype = ((IpsModel)getIpsModel()).getValueDatatype(ipsProject, qualifiedName);
        if (datatype != null) {
            return datatype;
        }

        ITableContents contents = (ITableContents)ipsProject.findIpsObject(IpsObjectType.TABLE_CONTENTS, qualifiedName);
        if (contents != null) {
            ITableStructure structure = contents.findTableStructure();
            if (structure != null && structure.isModelEnumType()) {
                return new TableContentsEnumDatatypeAdapter(contents);
            }
        }
        IIpsProject[] projects = ((IpsProject)ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            if (!visitedProjects.contains(projects[i])) {
                visitedProjects.add(projects[i]);
                datatype = findValueDatatype((IpsProject)projects[i], qualifiedName, visitedProjects);
                if (datatype != null) {
                    return datatype;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        if(datatype instanceof ArrayOfValueDatatype){
        	return new ArrayOfValueDatatypeHelper(datatype);
        }
        if (datatype instanceof TableContentsEnumDatatypeAdapter) {
            return getIpsArtefactBuilderSet().getDatatypeHelperForTableBasedEnum((TableContentsEnumDatatypeAdapter)datatype);
        }
        DatatypeHelper helper = ((IpsModel)getIpsModel()).getDatatypeHelper(this,
            (ValueDatatype)datatype);
        if (helper != null) {
            return helper;
        }
        try {
            IIpsProject[] projects = getIpsObjectPathInternal().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
            	helper = projects[i].getDatatypeHelper(datatype);
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
	 * {@inheritDoc}
	 */
	public DatatypeHelper findDatatypeHelper(String qName) throws CoreException {
		Datatype datatype = findDatatype(qName);
		return getDatatypeHelper(datatype);
	}

	/**
	 * {@inheritDoc}
	 */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException {
        if (datatype==null) {
            return new ValueSetType[] { ValueSetType.ALL_VALUES};
        }
        if (datatype instanceof NumericDatatype) {
            return ValueSetType.getValueSetTypes();
        }
        if (datatype instanceof MoneyDatatype) {
            return ValueSetType.getValueSetTypes();
        }
        if(datatype instanceof ArrayOfValueDatatype){
        	return new ValueSetType[] {ValueSetType.ALL_VALUES};
        }
        return new ValueSetType[] { ValueSetType.ALL_VALUES, ValueSetType.ENUM };
    }

	/**
     * {@inheritDoc}
     */
    public IProductCmpt[] findAllProductCmpts(IProductCmptType productCmptType, boolean includeSubtypes) throws CoreException {
        List result = new ArrayList();
        // find product components in this project
        findAllProductCmpts(this, productCmptType, includeSubtypes, result);
        // find product components in all referenced projects
        IIpsProject[] referencedIpsProjects = getReferencedIpsProjects();
        for (int i = 0; i < referencedIpsProjects.length; i++) {
            findAllProductCmpts(referencedIpsProjects[i], productCmptType, includeSubtypes, result);
        }
        return (IProductCmpt[]) result.toArray(new IProductCmpt[result.size()]);
    }

    /*
     * @see this{@link #findAllProductCmpts(IProductCmptType, boolean)}
     */
    private void findAllProductCmpts(IIpsProject ipsProject, IProductCmptType productCmptType, boolean includeSubytpes, List result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            ((AbstractIpsPackageFragmentRoot)roots[i]).findAllProductCmpts(productCmptType, includeSubytpes, result);
        }
    }
    
    /**
     * {@inheritDoc}
     */
	public IProductCmpt findProductCmptByRuntimeId(String runtimeId) throws CoreException {
		if(runtimeId == null){
			return null;
		}
		IIpsObject[] all = findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		for (int i = 0; i < all.length; i++) {
			if (((IProductCmpt)all[i]).getRuntimeId().equals(runtimeId)) {
				return (IProductCmpt)all[i];
			}
		}
		return null;
	}

    /**
     * {@inheritDoc}
     */
    public IProductCmpt findProductCmpt(String qualifiedName) throws CoreException{
        return (IProductCmpt)findIpsObject(IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void findTableContents(ITableStructure structure, List tableContents) throws CoreException{
        List alltableContents = new ArrayList();
        findIpsObjects(IpsObjectType.TABLE_CONTENTS, alltableContents);
        for (Iterator it = alltableContents.iterator(); it.hasNext();) {
            ITableContents content = (ITableContents)it.next();
            if(content.getTableStructure().equals(structure.getQualifiedName())){
                tableContents.add(content);
            }
        }
    }
    
	/**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        getSourceIpsFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    void getSourceIpsFragmentRoots(List result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].isBasedOnSourceFolder()) {
                result.add(roots[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet() {
        return ((IpsModel)getIpsModel()).getIpsArtefactBuilderSet(this, false);
    }

    /**
     * {@inheritDoc}
     */
    public void reinitializeIpsArtefactBuilderSet() throws CoreException {
        ((IpsModel)getIpsModel()).getIpsArtefactBuilderSet(this, true);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector() {
        return IpsPlugin.getDefault().getIpsLoggingFrameworkConnector(
                getPropertiesInternal().getLoggingFrameworkConnectorId());
    }
    
    /**
     * {@inheritDoc}
     * Find all product cmpt generations which refer to other procuct coponents and table contents. 
     */
	public IProductCmptGeneration[] findReferencingProductCmptGenerations(QualifiedNameType qualifiedNameType) throws CoreException {
        Set result = new HashSet();
        String qualifiedName = qualifiedNameType.getName();
        IIpsObject[] allProductCmpts = this.findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		if (IpsObjectType.PRODUCT_CMPT.equals(qualifiedNameType.getIpsObjectType())){
            for (int i = 0; i < allProductCmpts.length; i++) {
                findReferencingProductCmptGenerationsToProductCmpts((IProductCmpt)allProductCmpts[i], qualifiedName, result);
            }
        } else if (IpsObjectType.TABLE_CONTENTS.equals(qualifiedNameType.getIpsObjectType())){
            for (int i = 0; i < allProductCmpts.length; i++) {
                findReferencingProductCmptGenerationsToTableContents((IProductCmpt)allProductCmpts[i], qualifiedName, result);
            }
        }
		IProductCmptGeneration[] resultArray = new IProductCmptGeneration[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}
    
    /*
     * Finds all product cmpt generations of the given product cmpt which refers to the given product cmpt.
     * The result will be added to the given set.
     */
    private void findReferencingProductCmptGenerationsToProductCmpts(IProductCmpt toBeSearched,
            String qualifiedProductCmptName,
            Set result) throws CoreException {
        int max = toBeSearched.getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = toBeSearched.getProductCmptGeneration(i);
            IProductCmptLink[] relations = generation.getLinks();
            for (int j = 0; j < relations.length; j++) {
                if (relations[j].getTarget().equals(qualifiedProductCmptName)) {
                    result.add(generation);
                    break;
                }
            }
        }
    }

    /*
     * Finds all product cmpt generations of the given product cmpt which refers to the given table contents.
     * The result will be added to the given set.
     */
    private void findReferencingProductCmptGenerationsToTableContents(IProductCmpt toBeSearched,
            String qualifiedTableContentsName,
            Set result) throws CoreException {
        int max = toBeSearched.getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = toBeSearched.getProductCmptGeneration(i);
            ITableContentUsage[] tcus = generation.getTableContentUsages();
            for (int j = 0; j < tcus.length; j++) {
                if (tcus[j].getTableContentName().equals(qualifiedTableContentsName)) {
                    result.add(generation);
                    break;
                }
            }
        }
    }
	
    /**
     * {@inheritDoc}
     */
	public ITestCase[] findReferencingTestCases(String qualifiedProductCmptName) throws CoreException {
        ArrayList result = new ArrayList();
        IIpsObject[] allTestCases = (IIpsObject[])this.findIpsObjects(IpsObjectType.TEST_CASE);
        
        for (int i = 0; i < allTestCases.length; i++) {
            String[] productCmptQualifiedNames = ((ITestCase)allTestCases[i]).getReferencedProductCmpts();
            for (int j = 0; j < productCmptQualifiedNames.length; j++) {
                if (qualifiedProductCmptName.equals(productCmptQualifiedNames[j])){
                    result.add(allTestCases[i]);
                    break;
                }
            }
        }
        ITestCase[] resultArray = new ITestCase[result.size()];
        result.toArray(resultArray);
        return resultArray;
    }

    /**
	 * {@inheritDoc}
	 */
	public IPolicyCmptType[] findReferencingPolicyCmptTypes(IPolicyCmptType pcType) throws CoreException{
		ArrayList list= new ArrayList();
		// get referenced PCTypes
		IIpsObject[] pcTypes= findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
		for(int i=0; i<pcTypes.length; i++){
			IRelation[] relations= ((PolicyCmptType) pcTypes[i]).getRelations();
			for(int x=0; x<relations.length; x++){
				if(relations[x].getTarget().equals(pcType.getQualifiedName())){
					list.add(pcTypes[i]);
				}
			}
		}
		String superType= pcType.getSupertype();
		if(!superType.equals("")){ //$NON-NLS-1$
			IIpsObject ipsObject= findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, superType);
			if(ipsObject!=null){
				list.add(ipsObject);
			}
		}
		return (PolicyCmptType[])list.toArray(new PolicyCmptType[0]);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptNamingStrategy getProductCmptNamingStrategy() throws CoreException {
		return getPropertiesInternal().getProductCmptNamingStrategy();
	}

	public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException  {
		((IpsProjectProperties)getPropertiesInternal()).addDefinedDatatype(newDatatype);
		saveProjectProperties(getProperties());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeIdPrefix() {
		return getPropertiesInternal().getRuntimeIdPrefix();
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validate() throws CoreException {
		MessageList result = new MessageList();
        validateJavaProjectBuildPath(result);
		if (!getIpsProjectPropertiesFile().exists()) {
			String text = Messages.IpsProject_msgMissingDotIpsprojectFile;
			Message msg = new Message(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE, text, Message.ERROR, this);
			result.add(msg);
			return result;
		}
		IpsProjectProperties props = (IpsProjectProperties)getPropertiesInternal();
		if (!props.isCreatedFromParsableFileContents()) {
			String text = Messages.IpsProject_msgUnparsableDotIpsprojectFile;
			Message msg = new Message(IIpsProject.MSGCODE_UNPARSABLE_PROPERTY_FILE, text, Message.ERROR, this);
			result.add(msg);
			return result;
		}
        
        MessageList list = props.validate(this);
        result.add(list);
        if (list.containsErrorMsg()) {
            return result;
        }
        
        validateRequiredFeatures(result, props);
        validateMigration(result, props);
        validateDuplicateTocFilePath(result, props);
        validateIpsObjectPathCycle(result, props); 
		return result;
	}
    
    private void validateJavaProjectBuildPath(MessageList result) throws JavaModelException {
        IJavaProject javaProject = getJavaProject();
        if (!javaProject.exists()) {
            return;
        }
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            if (!JavaConventions.validateClasspathEntry(javaProject, entries[i], false).isOK()) {
                String text = Messages.bind(Messages.IpsProject_javaProjectHasInvalidBuildPath, entries[i].getPath());
                Message msg = new Message(IIpsProject.MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS, text, Message.WARNING, this);
                result.add(msg); 
                return;
            }
        }
    }

    private void validateIpsObjectPathCycle(MessageList result, IpsProjectProperties props) throws CoreException {
        if (getIpsObjectPathInternal().detectCycle(this)){
            String msg = Messages.IpsProject_msgCycleInIpsObjectPath;
            result.add(new Message(MSGCODE_CYCLE_IN_IPS_OBJECT_PATH, msg, Message.ERROR, this));            
        }
    }

    private void validateMigration(MessageList result, IpsProjectProperties props) {
        IIpsFeatureVersionManager[] managers = IpsPlugin.getDefault().getIpsFeatureVersionManagers();
        for (int i = 0; i < managers.length; i++) {
            try {
                managers[i].getMigrationOperations(this);
            }
            catch (Exception e) {
               IpsPlugin.log(e);
               String msg = NLS.bind(Messages.IpsProject_msgInvalidMigrationInformation, managers[i].getFeatureId());
               result.add(new Message(MSGCODE_INVALID_MIGRATION_INFORMATION, msg, Message.ERROR, this));
            }
        }
    }

    private void validateRequiredFeatures(MessageList ml, IpsProjectProperties props) {
        String features[] = props.getRequiredIpsFeatureIds();
        
        for (int i = 0; i < features.length; i++) {
            IIpsFeatureVersionManager manager = IpsPlugin.getDefault().getIpsFeatureVersionManager(features[i]);
            if (manager == null) {
                String msg = NLS.bind(Messages.IpsProject_msgNoFeatureManager, features[i]);
                ml.add(new Message(MSGCODE_NO_VERSIONMANAGER, msg, Message.ERROR, this));
                continue;
            }
            String minVersion = props.getMinRequiredVersionNumber(features[i]);
            if (manager.compareToCurrentVersion(minVersion) > 0 && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                    String[] params = {manager.getCurrentVersion(), minVersion, features[i]};
                    String msg = NLS.bind(Messages.IpsProject_msgVersionTooLow, params);
                    ml.add(new Message(MSGCODE_VERSION_TOO_LOW, msg, Message.ERROR, this));
            }
            
            if (manager.compareToCurrentVersion(minVersion) < 0 && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                String[] params = {manager.getCurrentVersion(), minVersion, features[i]};
                String msg = NLS.bind(Messages.IpsProject_msgIncompatibleVersions, params);
                ml.add(new Message(MSGCODE_INCOMPATIBLE_VERSIONS, msg, Message.ERROR, this));
            }
        }
    }

    /*
     * Validates for duplicate base package generated entries inside the referenced project
     */
    private void validateDuplicateTocFilePath(MessageList result, IpsProjectProperties props)
            throws CoreException {
        IIpsObjectPath path = getIpsObjectPathInternal();
        if (path == null) {
            return;
        }
        // check for same toc file path in referenced projects (only product definition projects)
        List tocPaths = collectTocPaths(path);
        
        IIpsProject[] referencedProjects = getReferencedIpsProjects();
        for (int i = 0; i < referencedProjects.length; i++) {
            if (! referencedProjects[i].isProductDefinitionProject()){
                continue;
            }
            IIpsObjectPath pathRelProject = ((IpsProject)referencedProjects[i]).getIpsObjectPathInternal();
            if (pathRelProject == null) {
                continue;
            }
            List tocPathsInRefProject = collectTocPaths(pathRelProject);
            for (Iterator iter = tocPathsInRefProject.iterator(); iter.hasNext();) {
                String tocPath = (String)iter.next();
                if (tocPaths.contains(tocPath)) {
                    String msg = NLS.bind(Messages.IpsProject_msgDuplicateTocFilePath, tocPath, referencedProjects[i]
                            .getName());
                    result.add(new Message(
                            MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS, msg,
                            Message.ERROR, this));
                    continue;
                }
            }
        }
    }
    
    // Return all toc file paths inside the given ips object path
    private List collectTocPaths(IIpsObjectPath ipsObjectPath){
        List tocPath = new ArrayList();
        IIpsSrcFolderEntry[] srcFolderEntries = ipsObjectPath.getSourceFolderEntries();
        for (int j = 0; j < srcFolderEntries.length; j++) {
            String tocFilePath = srcFolderEntries[j].getBasePackageNameForMergableJavaClasses() + "." + srcFolderEntries[j].getBasePackageRelativeTocPath(); //$NON-NLS-1$
            tocPath.add(tocFilePath);
        }
        return tocPath;
    }
    
    /**
	 * Returns the ClassLoaderProvider for the Java project that belongs to this ips project.
	 */
	public ClassLoaderProvider getClassLoaderProviderForJavaProject() {
		return ((IpsModel)getIpsModel()).getClassLoaderProvider(this);
	}

    public IIpsProjectNamingConventions getNamingConventions() {
        if(namingConventions==null){
            namingConventions = new DefaultIpsProjectNamingConventions(this);
        }
        return namingConventions;
    }
    
    /**
     * {@inheritDoc}
     */
    public MessageList checkForDuplicateRuntimeIds() throws CoreException {
        return checkForDuplicateRuntimeIdsInternal((IIpsObject[]) findIpsObjects(IpsObjectType.PRODUCT_CMPT), true);
    }

    /**
     * {@inheritDoc}
     */
    public MessageList checkForDuplicateRuntimeIds(IProductCmpt[] cmptsToCheck) throws CoreException {
        return checkForDuplicateRuntimeIdsInternal(cmptsToCheck, false);
    }
    
    /*
     * Check product cmpts for duplicate runtime id.
     * 
     * @param cmptsToCheck List of product components to check.
     * @param all <code>true</code> to indicate that the given array of product components is the
     *            whole list of all available product components or <code>false</code> for only a
     *            subset of product components. If <code>false</code> is provided, a list of all
     *            product components is build and all given product components are checked against
     *            this list.
     * @return A message list containing messages for each combination of a given product component
     *         with the same runtime id as another one. The message has either one invalid object
     *         property containing the given product component if <code>all</code> is
     *         <code>false</code>, or two invalid object properties with the both product
     *         components with the same runtime id if <code>all</code> is <code>true</code>.
     * @throws CoreException if an error occurs during processing.
     */
    private MessageList checkForDuplicateRuntimeIdsInternal(IIpsObject[] cmptsToCheck, boolean all)
            throws CoreException {
        IIpsObject[] baseCheck;
        if (all) {
            baseCheck = cmptsToCheck;
        } else {
            baseCheck = (IIpsObject[]) findIpsObjects(IpsObjectType.PRODUCT_CMPT);
        }

        MessageList result = new MessageList();
        IProductCmptNamingStrategy strategyI = null;
        IProductCmptNamingStrategy strategyJ = null;
        for (int i = 0; i < cmptsToCheck.length; i++) {
            ArgumentCheck.isInstanceOf(cmptsToCheck[i], IProductCmpt.class);
            IProductCmpt productCmptToCheck = (IProductCmpt)cmptsToCheck[i];
            strategyI = productCmptToCheck.getIpsProject().getProductCmptNamingStrategy();

            if (all) {
                // because we process the same array with index j as with index
                // i, index j can start allways with i+1 without overlooking some product
                // component combinations.
                for (int j = i + 1; j < cmptsToCheck.length; j++) {
                    ArgumentCheck.isInstanceOf(cmptsToCheck[j], IProductCmpt.class);
                    IProductCmpt productCmptToCheckB = (IProductCmpt)cmptsToCheck[j];
                    strategyJ = productCmptToCheckB.getIpsProject().getProductCmptNamingStrategy();
                    checkRuntimeId(strategyI, productCmptToCheck, productCmptToCheckB, result, true);
                    if (!strategyI.equals(strategyJ)) {
                        checkRuntimeId(strategyJ, productCmptToCheck, productCmptToCheckB, result, true);
                    }
                }
            } else {
                for (int j = 0; j < baseCheck.length; j++) {
                    ArgumentCheck.isInstanceOf(baseCheck[j], IProductCmpt.class);
                    IProductCmpt productCmptToCheckB = (IProductCmpt)baseCheck[j];                    
                    if (productCmptToCheck != productCmptToCheckB) {
                        strategyJ = productCmptToCheckB.getIpsProject().getProductCmptNamingStrategy();
                        checkRuntimeId(strategyI, productCmptToCheck, productCmptToCheckB, result, false);
                        if (!strategyI.equals(strategyJ)) {
                            checkRuntimeId(strategyJ, productCmptToCheck, productCmptToCheckB, result, false);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void checkRuntimeId(IProductCmptNamingStrategy strategy,
            IProductCmpt cmpt1,
            IProductCmpt cmpt2,
            MessageList list,
            boolean addBoth) {
        if (strategy.sameRuntimeId(cmpt1, cmpt2)) {
            ObjectProperty[] objects;

            if (addBoth) {
                objects = new ObjectProperty[2];
                objects[0] = new ObjectProperty(cmpt1, IProductCmpt.PROPERTY_RUNTIME_ID);
                objects[1] = new ObjectProperty(cmpt2, IProductCmpt.PROPERTY_RUNTIME_ID);
            }
            else {
                objects = new ObjectProperty[1];
                objects[0] = new ObjectProperty(cmpt1, IProductCmpt.PROPERTY_RUNTIME_ID);
            }

            String projectName = cmpt2.getIpsProject().getName();
            String msg = NLS.bind(Messages.IpsModel_msgRuntimeIDCollision, new String[] { cmpt1.getQualifiedName(),
                    cmpt2.getQualifiedName(), projectName });
            list.add(new Message(MSGCODE_RUNTIME_ID_COLLISION, msg, Message.ERROR, objects));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResourceExcludedFromProductDefinition(IResource resource) {
        if (resource==null) {
            return false;
        }
        IpsProjectProperties props = (IpsProjectProperties)getPropertiesInternal();
        String projectPath = this.getProject().getLocation().toString();
        String resourcePath = resource.getLocation().toString();
        if (! resourcePath.startsWith(projectPath)){
            throw new RuntimeException("Invalid project path " + projectPath + " of resource: " + resourcePath); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (resourcePath.length() <= projectPath.length()){
            return false;
        }
        String location = resourcePath.substring(projectPath.length() +1);
        return props.isResourceExcludedFromProductDefinition(location);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getName();
    }
}
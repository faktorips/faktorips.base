/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.IpsBuilder;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IIpsProject, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.ipsproject.IIpsProject
 */
public class IpsProject extends IpsElement implements IIpsProject {

    /**
     * The file extension for ips projects.
     */
    public final static String PROPERTY_FILE_EXTENSION = "ipsproject"; //$NON-NLS-1$

    /**
     * The file extension for ips projects but with a dot added before.
     */
    public final static String PROPERTY_FILE_EXTENSION_INCL_DOT = "." + PROPERTY_FILE_EXTENSION; //$NON-NLS-1$

    /** The underlying plattform project */
    private IProject project;

    private IIpsProjectNamingConventions namingConventions = null;

    /**
     * Constructor needed for <code>IProject.getNature()</code> and
     * <code>IProject.addNature()</code>.
     * 
     * @see #setProject(IProject)
     */
    public IpsProject() {
        // Provides default constructor
    }

    public IpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    @Override
    public IProject getProject() {
        if (project == null) {
            // we don't have a threading problem here, as projects are only handles!
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
        }

        return project;
    }

    @Override
    public boolean isReferencing(IIpsProject otherProject) throws CoreException {
        return otherProject.isReferencedBy(this, true);
    }

    @Override
    public IIpsProjectProperties getReadOnlyProperties() {
        return new IpsProjectPropertiesReadOnlyProxy(getPropertiesInternal());
    }

    @Override
    public IIpsProjectProperties getProperties() {
        return new IpsProjectProperties(this, getPropertiesInternal());
    }

    /**
     * Returns the properties from the model.
     */
    private IpsProjectProperties getPropertiesInternal() {
        return ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    }

    @Override
    public void setProperties(IIpsProjectProperties properties) throws CoreException {
        IpsProjectProperties newProjectProperties = new IpsProjectProperties(this, (IpsProjectProperties)properties);
        newProjectProperties.setPersistenceOptions(properties.getPersistenceOptions());
        saveProjectProperties(newProjectProperties);
    }

    @Override
    public ExtendedExprCompiler newExpressionCompiler() {
        ExtendedExprCompiler compiler = new ExtendedExprCompiler();
        IFunctionResolverFactory[] resolvers = IpsPlugin.getDefault().getFlFunctionResolverFactories();
        for (IFunctionResolverFactory resolver : resolvers) {
            try {
                compiler.add(resolver.newFunctionResolver(getExpressionLanguageFunctionsLanguage()));
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Unable the function resolver for the following factory: " //$NON-NLS-1$
                        + resolver.getClass(), e));
            }
        }

        return compiler;
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
            throw new CoreException(new IpsStatus("Error tranforming project data to xml string", e)); //$NON-NLS-1$
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
            if (token.indexOf("<!--") != -1) { //$NON-NLS-1$
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

    @Override
    public IFile getIpsProjectPropertiesFile() {
        return getProject().getFile(PROPERTY_FILE_EXTENSION_INCL_DOT);
    }

    @Override
    public IJavaProject getJavaProject() {
        return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(getName());
    }

    @Override
    public ClassLoader getClassLoaderForJavaProject() throws CoreException {
        return getClassLoaderForJavaProject(ClassLoader.getSystemClassLoader());
    }

    @Override
    public ClassLoader getClassLoaderForJavaProject(ClassLoader parent) throws CoreException {
        ArgumentCheck.notNull(parent);
        // always creates a new classloader,
        final ClassLoaderProvider classLoaderProvider = new ClassLoaderProvider(getJavaProject(), parent, true, false);
        return classLoaderProvider.getClassLoader();
    }

    @Override
    public Boolean isJavaProjectErrorFree(boolean checkReferencedJavaProjects) throws CoreException {
        return isJavaProjectErrorFree(getJavaProject(), checkReferencedJavaProjects);
    }

    private Boolean isJavaProjectErrorFree(IJavaProject javaProject, boolean checkReferencedJavaProjects)
            throws CoreException {
        IProject project = javaProject.getProject();
        if (!project.isAccessible()) {
            return null;
        }
        if (!javaProject.exists()) {
            return null;
        }
        // implementation note: if the java project has buildpath problems it also hasn't got a
        // build state
        // so we first have to check for problems with the build path. We can't do this via markers
        // as the build path markers
        // are created on a resource change event, and we don't now if it has been executed so far.
        if (getJavaProjectBuildPathProblemSeverity(javaProject) == IStatus.ERROR) {
            return Boolean.FALSE;
        }
        IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
                IResource.DEPTH_INFINITE);
        if (containsErrorMarker(markers)) {
            return Boolean.FALSE;
        }
        if (checkReferencedJavaProjects) {
            List<IJavaProject> refProjectcs = getJavaProjectsReferencedInClasspath(javaProject);
            for (IJavaProject refProject : refProjectcs) {
                Boolean errorFree = isJavaProjectErrorFree(refProject, true);
                if (errorFree != null && !errorFree.booleanValue()) {
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
        for (IMarker marker : markers) {
            if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
                return true;
            }
        }

        return false;
    }

    private List<IJavaProject> getJavaProjectsReferencedInClasspath(IJavaProject javaProject) throws JavaModelException {
        List<IJavaProject> result = new ArrayList<IJavaProject>();
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (IClasspathEntry entrie : entries) {
            if (entrie.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                IJavaProject refProject = javaProject.getJavaModel().getJavaProject(entrie.getPath().lastSegment());
                result.add(refProject);
            }
        }

        return result;
    }

    @Override
    public IIpsProject[] getReferencedIpsProjects() throws CoreException {
        return getIpsObjectPathInternal().getReferencedIpsProjects();
    }

    @Override
    public boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect) throws CoreException {
        if (otherProject == null || otherProject == this) {
            return false;
        }
        Set<IIpsProject> projectsVisited = new HashSet<IIpsProject>();

        return isReferencedBy(otherProject, considerIndirect, projectsVisited);
    }

    private boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect, Set<IIpsProject> projectsVisited)
            throws CoreException {

        IIpsObjectPath otherPath = ((IpsProject)otherProject).getIpsObjectPathInternal();
        IIpsProject[] referencedProjects = otherPath.getReferencedIpsProjects();
        for (IIpsProject referencedProject : referencedProjects) {
            if (equals(referencedProject)) {
                return true;
            }
            if (projectsVisited.contains(referencedProject)) {
                continue;
            }
            if (considerIndirect && isReferencedBy(referencedProject, considerIndirect, projectsVisited)) {
                return true;
            }
            projectsVisited.add(referencedProject);
        }

        return false;
    }

    @Override
    public IIpsProject[] findReferencingProjects(boolean includeIndirect) throws CoreException {
        IIpsProject[] projects = getIpsModel().getIpsProjects();
        List<IIpsProject> result = new ArrayList<IIpsProject>(projects.length);
        for (IIpsProject project2 : projects) {
            if (isReferencedBy(project2, includeIndirect)) {
                result.add(project2);
            }
        }

        return result.toArray(new IIpsProject[result.size()]);
    }

    @Override
    public IIpsProject[] findReferencingProjectLeavesOrSelf() throws CoreException {
        IIpsProject[] projects = getIpsModel().getIpsProjects();
        List<IIpsProject> result = new ArrayList<IIpsProject>(projects.length);
        result.add(this);
        for (IIpsProject project : projects) {
            if (this.isReferencedBy(project, true)) {
                boolean foundDependent = false;
                for (Iterator<IIpsProject> iterator = result.iterator(); iterator.hasNext();) {
                    IIpsProject aResult = iterator.next();
                    if (project.isReferencedBy(aResult, true)) {
                        foundDependent = true;
                        break;
                    } else {
                        if (aResult.isReferencedBy(project, true)) {
                            iterator.remove();
                        }
                    }
                }
                if (!foundDependent) {
                    result.add(project);
                }
            }
        }
        return result.toArray(new IIpsProject[result.size()]);
    }

    @Override
    public boolean canBeBuild() {
        try {
            return !validate().containsErrorMsg();
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public String getXmlFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }

    @Override
    public String getPlainTextFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }

    @Override
    public boolean isModelProject() {
        return getPropertiesInternal().isModelProject();
    }

    @Override
    public boolean isProductDefinitionProject() {
        return getPropertiesInternal().isProductDefinitionProject();
    }

    @Override
    public IIpsObjectPath getIpsObjectPath() throws CoreException {
        return getProperties().getIpsObjectPath();
    }

    @Override
    public IFolder[] getOutputFolders() throws CoreException {
        return getIpsObjectPathInternal().getOutputFolders();
    }

    @Override
    public boolean isAccessibleViaIpsObjectPath(IIpsObject ipsObject) throws CoreException {
        if (ipsObject == null) {
            return false;
        }

        IIpsSrcFile file = findIpsSrcFile(ipsObject.getQualifiedNameType());
        if (file == null) {
            return false;
        }

        return file.equals(ipsObject.getIpsSrcFile());
    }

    /**
     * Returns a <strong>reference</strong> to the ips object path, in contrast to the
     * getIpsObjectPath() method that returns a copy.
     */
    public IpsObjectPath getIpsObjectPathInternal() {
        return (IpsObjectPath)getPropertiesInternal().getIpsObjectPath();
    }

    @Override
    public void setCurrentArtefactBuilderSet(String id) throws CoreException {
        IIpsProjectProperties properties = getProperties();
        properties.setBuilderSetId(id);
        saveProjectProperties(properties);
    }

    public void setValueDatatypes(String[] ids) throws CoreException {
        IIpsProjectProperties properties = getProperties();
        properties.setPredefinedDatatypesUsed(ids);
        saveProjectProperties(properties);
    }

    @Override
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException {
        String[] ids = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ids[i] = types[i].getQualifiedName();
        }
        setValueDatatypes(ids);
    }

    @Override
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException {
        IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
        properties.setIpsObjectPath(newPath);
        saveProjectProperties(properties);
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        try {
            if (!getNamingConventions().validateIpsPackageRootName(name).containsErrorMsg()) {
                return new IpsPackageFragmentRoot(this, name);
            }
        } catch (CoreException e) {
            // nothing to do, return null
        }

        return null;
    }

    @Override
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException {
        List<IIpsPackageFragmentRoot> roots = new ArrayList<IIpsPackageFragmentRoot>();
        IIpsObjectPathEntry[] entries = getIpsObjectPathInternal().getEntries();
        for (IIpsObjectPathEntry entrie : entries) {
            IIpsPackageFragmentRoot root = entrie.getIpsPackageFragmentRoot();
            if (root != null) {
                roots.add(root);
            }
        }

        return roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }

    @Override
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(String name) {
        try {
            IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot root : roots) {
                if (root.getName().equals(name)) {
                    return root;
                }
            }
        } catch (CoreException e) {
            // nothing to do, return null
        }

        return null;
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        if (!cont.isAccessible()) {
            return new IResource[0];
        }
        List<IResource> childResources = new ArrayList<IResource>();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isPackageFragmentRoot(children[i]) & !isJavaFolder(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];

        return childResources.toArray(resArray);
    }

    /**
     * Examins the <code>JavaProject</code> corresponding to this <code>IpsProject</code> and its
     * relation to the given <code>IResource</code>. Returns true if the given resource corresponds
     * to a classpath entry of the javaproject. Returns true if the given resource corresponds to a
     * folder that is either the javaprojects default output location or the output location of one
     * of the projects classpathentries. False otherwise.
     */
    private boolean isJavaFolder(IResource resource) {
        try {
            IPath outputPath = getJavaProject().getOutputLocation();
            IClasspathEntry[] entries = getJavaProject().getResolvedClasspath(true);
            if (resource.getFullPath().equals(outputPath)) {
                return true;
            }
            for (IClasspathEntry entrie : entries) {
                if (resource.getFullPath().equals(entrie.getOutputLocation())) {
                    return true;
                }
                if (resource.getFullPath().equals(entrie.getPath())) {
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
     * Returns true if the given IResource is a folder that corresponds to an IpsPackageFragmentRoot
     * of this IpsProject, false otherwise.
     */
    private boolean isPackageFragmentRoot(IResource res) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            if (res.equals(root.getCorrespondingResource())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean exists() {
        if (!getCorrespondingResource().exists()) {
            return false;
        }
        IProject project = getProject();
        try {
            String[] natures = project.getDescription().getNatureIds();
            for (String nature : natures) {
                if (nature.equals(IIpsProject.NATURE_ID)) {
                    return true;
                }
            }
        } catch (CoreException e) {
            // if we can't get the project nature, the project is not in a state we would consider
            // full existance
            return false;
        }

        return false;
    }

    @Override
    public Locale getExpressionLanguageFunctionsLanguage() {
        return Locale.GERMAN;
    }

    @Override
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode() {
        IpsProjectProperties properties = getPropertiesInternal();
        return getIpsModel().getChangesOverTimeNamingConvention(
                properties.getChangesOverTimeNamingConventionIdForGeneratedCode());
    }

    @Override
    public IResource getCorrespondingResource() {
        return getProject();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragmentRoots();
    }

    @Override
    public IIpsProject getIpsProject() {
        return this;
    }

    @Override
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

    @Override
    public void deconfigure() throws CoreException {
        // Nothing to do
    }

    @Override
    public void setProject(IProject project) {
        name = project.getName();
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

    /**
     * Adds the command to the build spec
     */
    private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand) throws CoreException {
        ICommand[] oldCommands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[oldCommands.length + 1];
        System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
        newCommands[0] = newCommand;
        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

    //
    // Find methods with single result
    //

    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(type, qualifiedName);
        if (file == null) {
            return null;
        }

        return file.getIpsObject();
    }

    @Override
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(nameType.getIpsObjectType(), nameType.getName());
        if (file == null) {
            return null;
        }

        return file.getIpsObject();
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    @Override
    public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException {
        return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
    }

    @Override
    public IProductCmpt findProductCmpt(String qualifiedName) throws CoreException {
        return (IProductCmpt)findIpsObject(IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    @Override
    public IEnumType findEnumType(String qualifiedName) throws CoreException {
        return (IEnumType)findIpsObject(IpsObjectType.ENUM_TYPE, qualifiedName);
    }

    @Override
    public List<IEnumType> findEnumTypes(boolean includeAbstract, boolean includeNotContainingValues)
            throws CoreException {

        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.ENUM_TYPE);
        List<IEnumType> enumTypes = filesToIpsObjects(ipsSrcFiles, IEnumType.class);
        if (includeAbstract && includeNotContainingValues) {
            return enumTypes;
        }

        List<IEnumType> filteredList = new ArrayList<IEnumType>(enumTypes.size());
        for (IEnumType currentEnumType : enumTypes) {
            if (!includeAbstract && currentEnumType.isAbstract()) {
                continue;
            }
            if (!includeNotContainingValues && !(currentEnumType.isContainingValues())) {
                continue;
            }
            filteredList.add(currentEnumType);
        }
        return filteredList;
    }

    @Override
    public IProductCmpt findProductCmptByRuntimeId(String runtimeId) throws CoreException {
        if (runtimeId == null) {
            return null;
        }
        IIpsSrcFile[] all = findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile element : all) {
            if (runtimeId.equals((element).getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID))) {
                return (IProductCmpt)(element).getIpsObject();
            }
        }

        return null;
    }

    // TODO cd: maybe remove this method and use findAllTableContentsSrcFiles instead
    @Override
    public void findTableContents(ITableStructure structure, List<ITableContents> tableContents) throws CoreException {
        if (structure == null) {
            return;
        }
        String structureQName = structure.getQualifiedName();
        List<IIpsSrcFile> alltableContents = new ArrayList<IIpsSrcFile>();
        findAllIpsSrcFiles(alltableContents, IpsObjectType.TABLE_CONTENTS);
        for (IIpsSrcFile file : alltableContents) {
            if (file.exists()) {
                if (structureQName.equals(file.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE))) {
                    tableContents.add((ITableContents)file.getIpsObject());
                }
            }
        }
    }

    public ITableStructure findTableStructure(String tableContetnsQName) throws CoreException {
        return (ITableStructure)findIpsObject(IpsObjectType.TABLE_STRUCTURE, tableContetnsQName);
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qNameType) throws CoreException {
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        return (getIpsObjectPathInternal()).findIpsSrcFile(qNameType, visitedEntries);
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(IpsObjectType type, String qualifiedName) throws CoreException {
        return findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
    }

    @Override
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase)
            throws CoreException {

        List<IIpsSrcFile> files = new ArrayList<IIpsSrcFile>(1000);
        findIpsSrcFilesStartingWith(type, prefix, ignoreCase, files);
        return filesToIpsObjects(files).toArray(new IIpsObject[files.size()]);
    }

    @Override
    public IIpsSrcFile[] findIpsSrcFilesStartingWith(IpsObjectType type, String prefix, boolean ignoreCase)
            throws CoreException {

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>(1000);
        findIpsSrcFilesStartingWith(type, prefix, ignoreCase, result);
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    /**
     * Searches all objects of the given type starting with the given prefix found on the project's
     * path and adds them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the objects.
     */
    private void findIpsSrcFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) throws CoreException {

        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        (getIpsObjectPathInternal()).findIpsSrcFilesStartingWith(type, prefix, ignoreCase, result, visitedEntries);
    }

    /**
     * @deprecated use this{@link #findIpsSrcFiles(IpsObjectType)} instead
     */
    @Override
    @Deprecated
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        return filesToIpsObjects(findIpsSrcFiles(type));
    }

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * 
     * @deprecated use this{@link #findAllIpsSrcFiles(List)} instead
     */
    @Override
    @Deprecated
    public void findAllIpsObjects(List<IIpsObject> result) throws CoreException {
        // this is not the most efficient implementation, however, you should use
        // findIpsSrcFiles anyway!
        List<IIpsSrcFile> files = new ArrayList<IIpsSrcFile>();
        findAllIpsSrcFiles(files);
        for (IIpsSrcFile file : files) {
            IIpsObject ipsObject = null;
            if (file.exists()) {
                ipsObject = file.getIpsObject();
                if (ipsObject != null) {
                    result.add(ipsObject);
                }
            }
        }
    }

    private IIpsObject[] filesToIpsObjects(IIpsSrcFile[] files) throws CoreException {
        // this is not the most effizient implementation, however, you should use
        // findIpsSrcFiles anyway!
        List<IIpsObject> objects = new ArrayList<IIpsObject>(files.length);
        for (IIpsSrcFile file : files) {
            IIpsObject ipsObject = null;
            if (file.exists()) {
                ipsObject = file.getIpsObject();
                if (ipsObject != null) {
                    objects.add(ipsObject);
                }
            }
        }

        return objects.toArray(new IIpsObject[objects.size()]);
    }

    private List<IIpsObject> filesToIpsObjects(List<IIpsSrcFile> files) throws CoreException {
        List<IIpsObject> objects = new ArrayList<IIpsObject>(files.size());
        for (IIpsSrcFile file : files) {
            IIpsObject ipsObject = null;
            if (file.exists()) {
                ipsObject = file.getIpsObject();
                if (ipsObject != null) {
                    objects.add(ipsObject);
                }
            }
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T extends IIpsObject> List<T> filesToIpsObjects(List<IIpsSrcFile> files, Class<? extends T> clazz)
            throws CoreException {

        List<T> objects = new ArrayList<T>(files.size());
        for (IIpsSrcFile file : files) {
            IIpsObject ipsObject = null;
            if (file.exists()) {
                ipsObject = file.getIpsObject();
                if (ipsObject != null && clazz.isAssignableFrom(ipsObject.getClass())) {
                    objects.add((T)ipsObject);
                }
            }
        }
        return objects;
    }

    @Override
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result) throws CoreException {
        getIpsObjectPathInternal().collectAllIpsSrcFilesOfSrcFolderEntries(result);
    }

    @Override
    public IIpsSrcFile[] findIpsSrcFiles(IpsObjectType type) throws CoreException {
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        return (getIpsObjectPathInternal()).findIpsSrcFiles(type, visitedEntries);
    }

    @Override
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result) throws CoreException {
        findAllIpsSrcFiles(result, getIpsModel().getIpsObjectTypes());
    }

    @Override
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType ipsObjectType, String packageFragment)
            throws CoreException {

        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        getIpsObjectPathInternal().findIpsSrcFiles(ipsObjectType, packageFragment, result, visitedEntries);
    }

    @Override
    public IEnumContent findEnumContent(IEnumType enumType) throws CoreException {
        ArgumentCheck.notNull(enumType, this);
        if (enumType.isContainingValues()) {
            return null;
        }
        IIpsSrcFile enumContentSrcFile = findIpsSrcFile(IpsObjectType.ENUM_CONTENT, enumType.getEnumContentName());

        if (enumContentSrcFile != null && enumContentSrcFile.exists()) {
            return (IEnumContent)enumContentSrcFile.getIpsObject();
        }
        return null;
    }

    private void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType ipsObjectType) throws CoreException {
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        getIpsObjectPathInternal().findIpsSrcFiles(ipsObjectType, result, visitedEntries);
    }

    @Override
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType[] ipsObjectTypes) throws CoreException {
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        for (IpsObjectType ipsObjectType : ipsObjectTypes) {
            getIpsObjectPathInternal().findIpsSrcFiles(ipsObjectType, result, visitedEntries);
            visitedEntries.clear();
        }
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid) throws CoreException {
        return findDatatypes(valuetypesOnly, includeVoid, true);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives)
            throws CoreException {

        return findDatatypes(valuetypesOnly, includeVoid, includePrimitives, null);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes) throws CoreException {

        return findDatatypes(valuetypesOnly, includeVoid, includePrimitives, excludedDatatypes, true);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes,
            boolean includeAbstract) throws CoreException {

        Set<Datatype> result = new LinkedHashSet<Datatype>();
        getDatatypesDefinedInProjectPropertiesInclSubprojects(valuetypesOnly, includeVoid, includePrimitives, result);

        List<IEnumType> enumTypeList = findEnumTypes(includeAbstract, true);
        for (IEnumType enumType : enumTypeList) {
            if (enumType.isContainingValues()) {
                result.add(new EnumTypeDatatypeAdapter(enumType, null));
                continue;
            }
            IEnumContent enumContent = findEnumContent(enumType);
            result.add(new EnumTypeDatatypeAdapter(enumType, enumContent));
        }
        if (!valuetypesOnly) {
            findDatatypesDefinedByIpsObjects(result);
        }

        // Remove abstract datatypes from the list if not included
        if (!includeAbstract) {
            for (Datatype currentDatatype : result.toArray(new Datatype[result.size()])) {
                if (currentDatatype.isAbstract()) {
                    result.remove(currentDatatype);
                }
            }
        }

        // Remove every excluded datatype from the list
        if (excludedDatatypes != null) {
            for (Datatype currentDatatype : excludedDatatypes) {
                if (result.contains(currentDatatype)) {
                    result.remove(currentDatatype);
                }
            }
        }

        return result.toArray(new Datatype[result.size()]);
    }

    private void getDatatypesDefinedInProjectPropertiesInclSubprojects(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            Set<Datatype> result) throws CoreException {

        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        Set<IIpsProject> visitedProjects = new HashSet<IIpsProject>();
        getDatatypesDefinedInProjectPropertiesInclSubprojects(this, valuetypesOnly, includePrimitives, visitedProjects,
                result);
    }

    private void getDatatypesDefinedInProjectPropertiesInclSubprojects(IpsProject ipsProject,
            boolean valuetypesOnly,
            boolean includePrimitives,
            Set<IIpsProject> visitedProjects,
            Set<Datatype> result) throws CoreException {

        ((IpsModel)getIpsModel()).getDatatypesDefinedInProjectProperties(ipsProject, valuetypesOnly, includePrimitives,
                result);
        IIpsProject[] projects = ipsProject.getIpsObjectPathInternal().getReferencedIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            if (!visitedProjects.contains(projects[i])) {
                visitedProjects.add(projects[i]);
                getDatatypesDefinedInProjectPropertiesInclSubprojects(((IpsProject)projects[i]), valuetypesOnly,
                        includePrimitives, visitedProjects, result);
            }
        }
    }

    private void findDatatypesDefinedByIpsObjects(Set<Datatype> result) throws CoreException {
        List<IIpsSrcFile> refDatatypeFiles = new ArrayList<IIpsSrcFile>();
        IpsObjectType[] objectTypes = getIpsModel().getIpsObjectTypes();
        for (IpsObjectType objectType : objectTypes) {
            if (objectType.isDatatype()) {
                findAllIpsSrcFiles(refDatatypeFiles, objectType);
            }
        }
        for (IIpsSrcFile file : refDatatypeFiles) {
            if (file.exists()) {
                result.add((Datatype)file.getIpsObject());
            }
        }
    }

    @Override
    public EnumDatatype[] findEnumDatatypes() throws CoreException {
        // TODO this implementation can be improved and instanceof can be avoided. Therefore the
        // storage of EnumDatatypes an Datatypes
        // has to be separated within the IpsModel class

        Datatype[] datatypes = findDatatypes(true, false);
        ArrayList<Datatype> enumDatatypeList = new ArrayList<Datatype>();
        for (Datatype datatype : datatypes) {
            if (datatype instanceof EnumDatatype) {
                enumDatatypeList.add(datatype);
            }
        }

        return enumDatatypeList.toArray(new EnumDatatype[enumDatatypeList.size()]);
    }

    @Override
    public Datatype findDatatype(String qualifiedName) throws CoreException {
        if (qualifiedName.equals(Datatype.VOID.getQualifiedName())) {
            return Datatype.VOID;
        }
        Datatype type = findDatatypeDefinedInProjectPropertiesInclSubprojects(qualifiedName);
        if (type != null) {
            return type;
        }
        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
        if (arrayDimension > 0) {
            qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
        }
        IpsObjectType[] objectTypes = getIpsModel().getIpsObjectTypes();
        for (IpsObjectType objectType : objectTypes) {
            if (objectType.isDatatype()) {
                type = (Datatype)findIpsObject(objectType, qualifiedName);
                if (type != null) {
                    break;
                }
            }
        }
        if (type != null) {
            if (arrayDimension == 0) {
                return type;
            }
            if (type instanceof ValueDatatype) {
                return new ArrayOfValueDatatype(type, arrayDimension);
            }
            throw new IllegalArgumentException("The qualified name: \"" + qualifiedName + //$NON-NLS-1$
                    "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
        }
        return getEnumTypeDatatypeAdapter(qualifiedName, this);
    }

    private EnumTypeDatatypeAdapter getEnumTypeDatatypeAdapter(String qualifiedName, IIpsProject ipsProject)
            throws CoreException {

        IIpsSrcFile enumTypeSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, qualifiedName);
        if (enumTypeSrcFile != null && enumTypeSrcFile.exists()) {
            IEnumType enumType = (IEnumType)enumTypeSrcFile.getIpsObject();
            if (enumType.isContainingValues()) {
                return new EnumTypeDatatypeAdapter(enumType, null);
            }
            IEnumContent enumContent = ipsProject.findEnumContent(enumType);
            return new EnumTypeDatatypeAdapter(enumType, enumContent);
        }
        return null;
    }

    @Override
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException {
        if (qualifiedName == null) {
            return null;
        }
        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
        if (arrayDimension > 0) {
            qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
        }
        ValueDatatype type = findValueDatatype(this, qualifiedName, new HashSet<IIpsProject>());
        if (arrayDimension == 0) {
            return type;
        }
        if (type != null) {
            return new ArrayOfValueDatatype(type, arrayDimension);
        }

        throw new IllegalArgumentException("The qualified name: \"" + qualifiedName + //$NON-NLS-1$
                "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private ValueDatatype findValueDatatype(IpsProject ipsProject,
            String qualifiedName,
            Set<IIpsProject> visitedProjects) throws CoreException {

        ValueDatatype datatype = ((IpsModel)getIpsModel()).getValueDatatypeDefinedInProjectProperties(ipsProject,
                qualifiedName);
        if (datatype != null) {
            return datatype;
        }
        datatype = getEnumTypeDatatypeAdapter(qualifiedName, ipsProject);
        if (datatype != null) {
            return datatype;
        }
        IIpsProject[] projects = (ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
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

    private Datatype findDatatypeDefinedInProjectPropertiesInclSubprojects(String qualifiedName) throws CoreException {
        if (qualifiedName == null) {
            return null;
        }

        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
        if (arrayDimension > 0) {
            qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
        }

        Datatype type = findDatatypeDefinedInProjectPropertiesInclSubprojects(this, qualifiedName,
                new HashSet<IIpsProject>());
        if (arrayDimension == 0) {
            return type;
        }
        if (type instanceof ValueDatatype) {
            return new ArrayOfValueDatatype(type, arrayDimension);
        }

        throw new IllegalArgumentException("The qualified name: \"" + qualifiedName + //$NON-NLS-1$
                "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private Datatype findDatatypeDefinedInProjectPropertiesInclSubprojects(IpsProject ipsProject,
            String qualifiedName,
            HashSet<IIpsProject> visitedProjects) throws CoreException {

        Datatype datatype = ((IpsModel)getIpsModel()).getDatatypeDefinedInProjectProperties(ipsProject, qualifiedName);
        if (datatype != null) {
            return datatype;
        }
        IIpsProject[] projects = (ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            if (!visitedProjects.contains(projects[i])) {
                visitedProjects.add(projects[i]);
                datatype = findDatatypeDefinedInProjectPropertiesInclSubprojects((IpsProject)projects[i],
                        qualifiedName, visitedProjects);
                if (datatype != null) {
                    return datatype;
                }
            }
        }

        return null;
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        if (datatype instanceof ArrayOfValueDatatype) {
            return new ArrayOfValueDatatypeHelper(datatype);
        }
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            return getIpsArtefactBuilderSet().getDatatypeHelperForEnumType((EnumTypeDatatypeAdapter)datatype);
        }
        DatatypeHelper helper = ((IpsModel)getIpsModel()).getDatatypeHelper(this, (ValueDatatype)datatype);
        if (helper != null) {
            return helper;
        }

        IIpsProject[] projects = getIpsObjectPathInternal().getReferencedIpsProjects();
        for (IIpsProject project2 : projects) {
            helper = project2.getDatatypeHelper(datatype);
            if (helper != null) {
                return helper;
            }
        }

        return null;
    }

    @Override
    public DatatypeHelper findDatatypeHelper(String qName) throws CoreException {
        Datatype datatype = findDatatype(qName);
        return getDatatypeHelper(datatype);
    }

    @Override
    public List<ValueSetType> getValueSetTypes(ValueDatatype datatype) {
        List<ValueSetType> types = new ArrayList<ValueSetType>();
        if (datatype == null) {
            types.add(ValueSetType.UNRESTRICTED);
            return types;
        }
        if (datatype instanceof NumericDatatype) {
            return ValueSetType.getValueSetTypesAsList();
        }
        if (datatype instanceof MoneyDatatype) {
            return ValueSetType.getValueSetTypesAsList();
        }
        if (datatype instanceof ArrayOfValueDatatype) {
            types.add(ValueSetType.UNRESTRICTED);
            return types;
        }
        types.add(ValueSetType.UNRESTRICTED);
        types.add(ValueSetType.ENUM);
        return types;
    }

    @Override
    public boolean isValueSetTypeApplicable(ValueDatatype datatype, ValueSetType valueSetType) throws CoreException {
        if (valueSetType == null) {
            return false;
        }
        List<ValueSetType> types = getValueSetTypes(datatype);
        for (ValueSetType vsType : types) {
            if (vsType.equals(valueSetType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IProductCmpt[] findAllProductCmpts(IProductCmptType productCmptType, boolean includeSubtypes)
            throws CoreException {

        List<IProductCmpt> result = new ArrayList<IProductCmpt>();
        getIpsObjectPathInternal().findAllProductCmpts(productCmptType, includeSubtypes, result);
        return result.toArray(new IProductCmpt[result.size()]);
    }

    @Override
    public IIpsSrcFile[] findAllProductCmptSrcFiles(IProductCmptType productCmptType, boolean includeCmptsForSubtypes)
            throws CoreException {

        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>(ipsSrcFiles.length);
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            String strProductCmptTypeOfCandidate = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            if (productCmptType == null || productCmptType.getQualifiedName().equals(strProductCmptTypeOfCandidate)) {
                result.add(ipsSrcFile);
            } else if (includeCmptsForSubtypes) {
                IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(strProductCmptTypeOfCandidate);
                if (type == null) {
                    continue;
                }
                if (type.isSubtypeOrSameType(productCmptType, type.getIpsProject())) {
                    result.add(ipsSrcFile);
                }
            }
        }

        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public IIpsSrcFile[] findAllTestCaseSrcFiles(ITestCaseType testCaseType) throws CoreException {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.TEST_CASE);
        if (testCaseType == null) {
            return ipsSrcFiles;
        }
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>(ipsSrcFiles.length);
        for (IIpsSrcFile srcFile : ipsSrcFiles) {
            String testCaseTypeCandidateQName = srcFile.getPropertyValue(ITestCase.PROPERTY_TEST_CASE_TYPE);
            if (testCaseType.getQualifiedName().equals(testCaseTypeCandidateQName)) {
                result.add(srcFile);
            }
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public IIpsSrcFile[] findAllEnumContentSrcFiles(IEnumType enumType, boolean includingSubtypes) throws CoreException {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.ENUM_CONTENT);
        if (enumType == null) {
            return ipsSrcFiles;
        }
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>(ipsSrcFiles.length);
        for (IIpsSrcFile srcFile : ipsSrcFiles) {
            String enumTypeCandidateQNmae = srcFile.getPropertyValue(IEnumContent.PROPERTY_ENUM_TYPE);
            if (enumType.getQualifiedName().equals(enumTypeCandidateQNmae)) {
                result.add(srcFile);
            } else if (includingSubtypes) {
                IEnumType enumTypeCandiate = findEnumType(enumTypeCandidateQNmae);
                if (enumTypeCandiate == null) {
                    continue;
                } else if (enumTypeCandiate.isSubEnumTypeOf(enumType, this)) {
                    result.add(srcFile);
                }
            }
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public IIpsSrcFile[] findAllTableContentsSrcFiles(ITableStructure structure) throws CoreException {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        if (structure == null) {
            return ipsSrcFiles;
        }
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>(ipsSrcFiles.length);
        for (IIpsSrcFile srcFile : ipsSrcFiles) {
            String tableContentsCandidateQName = srcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
            if (structure.getQualifiedName().equals(tableContentsCandidateQName)) {
                result.add(srcFile);
            }
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException {
        List<IIpsPackageFragmentRoot> result = new ArrayList<IIpsPackageFragmentRoot>();
        getSourceIpsFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    public void getSourceIpsFragmentRoots(List<IIpsPackageFragmentRoot> result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.isBasedOnSourceFolder()) {
                result.add(root);
            }
        }
    }

    @Override
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet() {
        return ((IpsModel)getIpsModel()).getIpsArtefactBuilderSet(this, false);
    }

    @Override
    public void reinitializeIpsArtefactBuilderSet() throws CoreException {
        ((IpsModel)getIpsModel()).getIpsArtefactBuilderSet(this, true);
    }

    /**
     * Find all product cmpt generations which refer to other procuct coponents and table contents.
     */
    @Override
    public IProductCmptGeneration[] findReferencingProductCmptGenerations(QualifiedNameType qualifiedNameType)
            throws CoreException {

        Set<IProductCmptGeneration> result = new HashSet<IProductCmptGeneration>();
        String qualifiedName = qualifiedNameType.getName();
        IIpsObject[] allProductCmpts = findIpsObjects(IpsObjectType.PRODUCT_CMPT);
        if (IpsObjectType.PRODUCT_CMPT.equals(qualifiedNameType.getIpsObjectType())) {
            for (IIpsObject allProductCmpt : allProductCmpts) {
                findReferencingProductCmptGenerationsToProductCmpts((IProductCmpt)allProductCmpt, qualifiedName, result);
            }
        } else if (IpsObjectType.TABLE_CONTENTS.equals(qualifiedNameType.getIpsObjectType())) {
            for (IIpsObject allProductCmpt : allProductCmpts) {
                findReferencingProductCmptGenerationsToTableContents((IProductCmpt)allProductCmpt, qualifiedName,
                        result);
            }
        }
        IProductCmptGeneration[] resultArray = new IProductCmptGeneration[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    /**
     * + Finds all product cmpt generations of the given product cmpt which refers to the given
     * product cmpt. The result will be added to the given set.
     */
    private void findReferencingProductCmptGenerationsToProductCmpts(IProductCmpt toBeSearched,
            String qualifiedProductCmptName,
            Set<IProductCmptGeneration> result) {

        int max = toBeSearched.getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = toBeSearched.getProductCmptGeneration(i);
            IProductCmptLink[] relations = generation.getLinks();
            for (IProductCmptLink relation : relations) {
                if (relation.getTarget().equals(qualifiedProductCmptName)) {
                    result.add(generation);
                    break;
                }
            }
        }
    }

    /**
     * Finds all product cmpt generations of the given product cmpt which refers to the given table
     * contents. The result will be added to the given set.
     */
    private void findReferencingProductCmptGenerationsToTableContents(IProductCmpt toBeSearched,
            String qualifiedTableContentsName,
            Set<IProductCmptGeneration> result) {

        int max = toBeSearched.getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = toBeSearched.getProductCmptGeneration(i);
            ITableContentUsage[] tcus = generation.getTableContentUsages();
            for (ITableContentUsage tcu : tcus) {
                if (tcu.getTableContentName().equals(qualifiedTableContentsName)) {
                    result.add(generation);
                    break;
                }
            }
        }
    }

    @Override
    public List<ITestCase> getAllTestCases() throws CoreException {
        List<IIpsSrcFile> resultSrcFiles = new ArrayList<IIpsSrcFile>();

        List<IIpsPackageFragmentRoot> roots = new ArrayList<IIpsPackageFragmentRoot>();
        getSourceIpsFragmentRoots(roots);
        for (IIpsPackageFragmentRoot root : roots) {
            ((IpsPackageFragmentRoot)root).findIpsSourceFiles(IpsObjectType.TEST_CASE, null, resultSrcFiles);
        }

        return filesToIpsObjects(resultSrcFiles, ITestCase.class);
    }

    @Override
    public IPolicyCmptType[] findReferencingPolicyCmptTypes(IPolicyCmptType pcType) throws CoreException {
        ArrayList<IIpsObject> list = new ArrayList<IIpsObject>();
        // get referenced PCTypes
        IIpsObject[] pcTypes = findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
        for (IIpsObject pcType2 : pcTypes) {
            IPolicyCmptTypeAssociation[] relations = ((PolicyCmptType)pcType2).getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation relation : relations) {
                if (relation.getTarget().equals(pcType.getQualifiedName())) {
                    list.add(pcType2);
                }
            }
        }

        String superType = pcType.getSupertype();
        if (!(superType.length() == 0)) {
            IIpsObject ipsObject = findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, superType);
            if (ipsObject != null) {
                list.add(ipsObject);
            }
        }

        return list.toArray(new PolicyCmptType[0]);
    }

    @Override
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() throws CoreException {
        return getPropertiesInternal().getProductCmptNamingStrategy();
    }

    @Override
    public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException {
        (getPropertiesInternal()).addDefinedDatatype(newDatatype);
        saveProjectProperties(getProperties());
    }

    @Override
    public String getRuntimeIdPrefix() {
        return getPropertiesInternal().getRuntimeIdPrefix();
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        validateJavaProjectBuildPath(result);
        if (!getIpsProjectPropertiesFile().exists()) {
            String text = Messages.IpsProject_msgMissingDotIpsprojectFile;
            Message msg = new Message(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE, text, Message.ERROR, this);
            result.add(msg);
            return result;
        }

        IpsProjectProperties props = getPropertiesInternal();
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
        validateMigration(result);
        validateDuplicateTocFilePath(result);
        validateIpsObjectPathCycle(result);

        return result;
    }

    private void validateJavaProjectBuildPath(MessageList result) throws JavaModelException {
        IJavaProject javaProject = getJavaProject();
        if (!javaProject.exists()) {
            return;
        }

        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (IClasspathEntry entrie : entries) {
            if (JavaConventions.validateClasspathEntry(javaProject, entrie, false).getSeverity() == IStatus.ERROR) {
                String text = NLS.bind(Messages.IpsProject_javaProjectHasInvalidBuildPath, entrie.getPath());
                Message msg = new Message(IIpsProject.MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS, text, Message.WARNING,
                        this);
                result.add(msg);
                return;
            }
        }
    }

    private int getJavaProjectBuildPathProblemSeverity(IJavaProject javaProject) throws JavaModelException {
        if (!javaProject.exists()) {
            return IStatus.OK;
        }

        int severity = IStatus.OK;
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (IClasspathEntry entrie : entries) {
            int entrySeverity = JavaConventions.validateClasspathEntry(javaProject, entrie, false).getSeverity();
            if (entrySeverity > severity) {
                severity = entrySeverity;
            }
        }

        return severity;
    }

    private void validateIpsObjectPathCycle(MessageList result) throws CoreException {
        if (getIpsObjectPathInternal().detectCycle()) {
            String msg = Messages.IpsProject_msgCycleInIpsObjectPath;
            result.add(new Message(MSGCODE_CYCLE_IN_IPS_OBJECT_PATH, msg, Message.ERROR, this));
        }
    }

    private void validateMigration(MessageList result) {
        IIpsFeatureVersionManager[] managers = IpsPlugin.getDefault().getIpsFeatureVersionManagers();
        for (IIpsFeatureVersionManager manager : managers) {
            try {
                manager.getMigrationOperations(this);
            } catch (Exception e) {
                IpsPlugin.log(e);
                String msg = NLS.bind(Messages.IpsProject_msgInvalidMigrationInformation, manager.getFeatureId());
                result.add(new Message(MSGCODE_INVALID_MIGRATION_INFORMATION, msg, Message.ERROR, this));
            }
        }
    }

    private void validateRequiredFeatures(MessageList ml, IpsProjectProperties props) {
        String features[] = props.getRequiredIpsFeatureIds();

        for (String feature : features) {
            IIpsFeatureVersionManager manager = IpsPlugin.getDefault().getIpsFeatureVersionManager(feature);
            if (manager == null) {
                String msg = NLS.bind(Messages.IpsProject_msgNoFeatureManager, feature);
                ml.add(new Message(MSGCODE_NO_VERSIONMANAGER, msg, Message.ERROR, this));
                continue;
            }
            String minVersion = props.getMinRequiredVersionNumber(feature);
            if (manager.compareToCurrentVersion(minVersion) > 0 && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                String[] params = { manager.getCurrentVersion(), minVersion, feature };
                String msg = NLS.bind(Messages.IpsProject_msgVersionTooLow, params);
                ml.add(new Message(MSGCODE_VERSION_TOO_LOW, msg, Message.ERROR, this));
            }

            if (manager.compareToCurrentVersion(minVersion) < 0 && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                String[] params = { manager.getCurrentVersion(), minVersion, feature };
                String msg = NLS.bind(Messages.IpsProject_msgIncompatibleVersions, params);
                ml.add(new Message(MSGCODE_INCOMPATIBLE_VERSIONS, msg, Message.ERROR, this));
            }
        }
    }

    /**
     * Validates for duplicate base package generated entries inside the referenced project
     */
    private void validateDuplicateTocFilePath(MessageList result) throws CoreException {
        // check for same toc file path in referenced projects (only product definition projects)
        List<IPath> tocPaths = collectTocPaths(getIpsArtefactBuilderSet(), this);

        IIpsProject[] referencedProjects = getReferencedIpsProjects();
        for (int i = 0; i < referencedProjects.length; i++) {
            if (!isProductDefinitionProject() || !referencedProjects[i].isProductDefinitionProject()) {
                continue;
            }
            IIpsArtefactBuilderSet builderSet = referencedProjects[i].getIpsArtefactBuilderSet();
            List<IPath> tocPathsInRefProject = collectTocPaths(builderSet, referencedProjects[i]);

            for (IPath tocPath : tocPathsInRefProject) {
                if (tocPaths.contains(tocPath)) {
                    String msg = NLS.bind(Messages.IpsProject_msgDuplicateTocFilePath, tocPath,
                            referencedProjects[i].getName());
                    result.add(new Message(MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS, msg, Message.ERROR,
                            this));
                }
            }
        }
    }

    private List<IPath> collectTocPaths(IIpsArtefactBuilderSet builderSet, IIpsProject ipsProject) throws CoreException {
        List<IPath> tocPaths = new ArrayList<IPath>();
        IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            String fileName = builderSet.getRuntimeRepositoryTocResourceName(root);
            if (fileName != null) {
                tocPaths.add(new Path(fileName));
            }
        }

        return tocPaths;
    }

    /**
     * Returns a cached ClassLoaderProvider for the Java project that belongs to this ips project.
     */
    public ClassLoaderProvider getClassLoaderProviderForJavaProject() {
        return ((IpsModel)getIpsModel()).getClassLoaderProvider(this);
    }

    @Override
    public IIpsProjectNamingConventions getNamingConventions() {
        if (namingConventions == null) {
            namingConventions = new DefaultIpsProjectNamingConventions(this);
        }

        return namingConventions;
    }

    @Override
    public MessageList checkForDuplicateRuntimeIds() throws CoreException {
        return checkForDuplicateRuntimeIdsInternal(findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT), true);
    }

    @Override
    public MessageList checkForDuplicateRuntimeIds(IIpsSrcFile[] cmptsToCheck) throws CoreException {
        return checkForDuplicateRuntimeIdsInternal(cmptsToCheck, false);
    }

    /**
     * Check product cmpts for duplicate runtime id.
     * 
     * @param cmptsToCheck List of product components to check.
     * 
     * @param all <code>true</code> to indicate that the given array of product components is the
     *            whole list of all available product components or <code>false</code> for only a
     *            subset of product components. If <code>false</code> is provided, a list of all
     *            product components is build and all given product components are checked against
     *            this list.
     * 
     * @return A message list containing messages for each combination of a given product component
     *         with the same runtime id as another one. The message has either one invalid object
     *         property containing the given product component if <code>all</code> is
     *         <code>false</code>, or two invalid object properties with the both product components
     *         with the same runtime id if <code>all</code> is <code>true</code>.
     * 
     * @throws CoreException if an error occurs during processing.
     */
    private MessageList checkForDuplicateRuntimeIdsInternal(IIpsSrcFile[] cmptsToCheck, boolean all)
            throws CoreException {

        IIpsSrcFile[] baseCheck;
        if (all) {
            baseCheck = cmptsToCheck;
        } else {
            baseCheck = findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        }

        MessageList result = new MessageList();
        IProductCmptNamingStrategy strategyI = null;
        IProductCmptNamingStrategy strategyJ = null;
        for (int i = 0; i < cmptsToCheck.length; i++) {
            ArgumentCheck.equals(cmptsToCheck[i].getIpsObjectType(), IpsObjectType.PRODUCT_CMPT_TYPE);

            IIpsSrcFile productCmptToCheck = cmptsToCheck[i];
            strategyI = productCmptToCheck.getIpsProject().getProductCmptNamingStrategy();

            if (all) {
                // because we process the same array with index j as with index
                // i, index j can start allways with i+1 without overlooking some product
                // component combinations.
                for (int j = i + 1; j < cmptsToCheck.length; j++) {
                    ArgumentCheck.equals(cmptsToCheck[j].getIpsObjectType(), IpsObjectType.PRODUCT_CMPT_TYPE);
                    IIpsSrcFile productCmptToCheckB = cmptsToCheck[j];
                    strategyJ = productCmptToCheckB.getIpsProject().getProductCmptNamingStrategy();
                    checkRuntimeId(strategyI, productCmptToCheck, productCmptToCheckB, result, true);
                    if (!strategyI.equals(strategyJ)) {
                        checkRuntimeId(strategyJ, productCmptToCheck, productCmptToCheckB, result, true);
                    }
                }
            } else {
                for (IIpsSrcFile element : baseCheck) {
                    ArgumentCheck.equals(element.getIpsObjectType(), IpsObjectType.PRODUCT_CMPT_TYPE);
                    IIpsSrcFile productCmptToCheckB = element;
                    if (!productCmptToCheck.getQualifiedNameType().equals((productCmptToCheckB.getQualifiedNameType()))) {
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
            IIpsSrcFile cmpt1,
            IIpsSrcFile cmpt2,
            MessageList list,
            boolean addBoth) throws CoreException {

        String runtimeId1 = cmpt1.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID);
        String runtimeId2 = cmpt2.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID);
        if (strategy.sameRuntimeId(runtimeId1, runtimeId2)) {
            ObjectProperty[] objects;

            if (addBoth) {
                objects = new ObjectProperty[2];
                objects[0] = new ObjectProperty(cmpt1.getIpsObject(), IProductCmpt.PROPERTY_RUNTIME_ID);
                objects[1] = new ObjectProperty(cmpt2.getIpsObject(), IProductCmpt.PROPERTY_RUNTIME_ID);
            } else {
                objects = new ObjectProperty[1];
                objects[0] = new ObjectProperty(cmpt1.getIpsObject(), IProductCmpt.PROPERTY_RUNTIME_ID);
            }

            String projectName = cmpt2.getIpsProject().getName();
            String msg = NLS.bind(Messages.IpsProject_msgRuntimeIDCollision, new String[] {
                    cmpt1.getQualifiedNameType().getName(), cmpt2.getQualifiedNameType().getName(), projectName });
            list.add(new Message(MSGCODE_RUNTIME_ID_COLLISION, msg, Message.ERROR, objects));
        }
    }

    @Override
    public boolean isResourceExcludedFromProductDefinition(IResource resource) {
        if (resource == null) {
            return false;
        }
        IpsProjectProperties props = getPropertiesInternal();
        String projectPath = getProject().getLocation().toString();
        String resourcePath = resource.getLocation().toString();
        if (!resourcePath.startsWith(projectPath)) {
            throw new RuntimeException("Invalid project path " + projectPath + " of resource: " + resourcePath); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (resourcePath.length() <= projectPath.length()) {
            return false;
        }
        String location = resourcePath.substring(projectPath.length() + 1);

        return props.isResourceExcludedFromProductDefinition(location);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        try {
            return getIpsObjectPath().getResourceAsStream(path);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return getPropertiesInternal().getTableColumnNamingStrategy();
    }

    @Override
    public ITableNamingStrategy getTableNamingStrategy() {
        return getPropertiesInternal().getTableNamingStrategy();
    }

    @Override
    public boolean isPersistenceSupportEnabled() {
        return getPropertiesInternal().isPersistenceSupportEnabled();
    }

    @Override
    public IVersionFormat getVersionFormat() throws CoreException {
        final IConfigurationElement releaseExtension = ProductReleaseProcessor.getReleaseExtensionElement(this);
        if (releaseExtension == null) {
            return null;
        }
        return new IVersionFormat() {

            private Pattern versionPattern = Pattern.compile(releaseExtension.getAttribute("versionFormatRegex")); //$NON-NLS-1$

            @Override
            public boolean isCorrectVersionFormat(String version) {
                return versionPattern.matcher(version).matches();
            }

            @Override
            public String getVersionFormat() {
                return releaseExtension.getAttribute("readableVersionFormat"); //$NON-NLS-1$
            }
        };
    }
}

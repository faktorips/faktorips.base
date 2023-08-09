/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.ExtensionFunctionResolversCache;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.internal.ipsproject.cache.RuntimeIdCache;
import org.faktorips.devtools.model.internal.ipsproject.cache.TableContentsStructureCache;
import org.faktorips.devtools.model.internal.ipsproject.cache.UnqualifiedNameCache;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectPropertiesReadOnlyProxy;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateHierarchyFinder;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Implementation of IIpsProject, see the corresponding interface for more details.
 *
 * @see org.faktorips.devtools.model.ipsproject.IIpsProject
 */
public class IpsProject extends IpsElement implements IIpsProject {

    public static final boolean TRACE_IPSPROJECT_PROPERTIES;

    static {
        TRACE_IPSPROJECT_PROPERTIES = Boolean
                .parseBoolean(Abstractions.getDebugOption("org.faktorips.devtools.model/trace/properties")); //$NON-NLS-1$
    }

    /**
     * The file extension for IPS projects.
     */
    public static final String PROPERTY_FILE_EXTENSION = "ipsproject"; //$NON-NLS-1$

    /**
     * The file extension for IPS projects but with a dot added before.
     */
    public static final String PROPERTY_FILE_EXTENSION_INCL_DOT = "." + PROPERTY_FILE_EXTENSION; //$NON-NLS-1$

    /**
     * The nature ID before Faktor-IPS 22.12
     *
     * @since 22.12
     * @deprecated do not use except for backwards compatibility
     */
    @Deprecated(since = "22.12")
    public static final String OLD_NATURE_ID = IpsModelActivator.PLUGIN_ID + ".ipsnature"; //$NON-NLS-1$

    private final IJavaNamingConvention javaNamingConvention = new JavaNamingConvention();

    /** The underlying platform project */
    private AProject project;

    private IIpsProjectNamingConventions namingConventions = null;

    private AFile propertyFile;

    private final UnqualifiedNameCache unqualifiedNameCache = new UnqualifiedNameCache(this);
    private final RuntimeIdCache runtimeIdCache = new RuntimeIdCache(this);
    private final TableContentsStructureCache tableContentsStructureCache = new TableContentsStructureCache(this);

    /**
     * Constructor needed for <code>IProject.getNature()</code> and
     * <code>IProject.addNature()</code>.
     */
    public IpsProject() {
        // Provides default constructor
    }

    public IpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    @Override
    public IpsModel getIpsModel() {
        return (IpsModel)super.getIpsModel();
    }

    @Override
    public AProject getProject() {
        if (project == null) {
            project = Abstractions.getWorkspace().getRoot().getProject(getName());
        }
        return project;
    }

    @Override
    public boolean isReferencing(IIpsProject otherProject) {
        return otherProject.isReferencedBy(this, true);
    }

    @Override
    public IIpsProjectProperties getReadOnlyProperties() {
        return new IpsProjectPropertiesReadOnlyProxy(getPropertiesInternal());
    }

    @Override
    public IIpsProjectProperties getProperties() {
        if (TRACE_IPSPROJECT_PROPERTIES) {
            System.out.println(
                    "Calling getProperties() is really expensive, use getReadOnlyProperties() wherever possible!"); //$NON-NLS-1$
        }
        return new IpsProjectProperties(this, getPropertiesInternal());
    }

    /**
     * Returns the properties from the model.
     */
    private IpsProjectProperties getPropertiesInternal() {
        return getIpsModel().getIpsProjectProperties(this);
    }

    @Override
    public void setProperties(IIpsProjectProperties properties) {
        IpsProjectProperties newProjectProperties = new IpsProjectProperties(this, (IpsProjectProperties)properties);
        newProjectProperties.setPersistenceOptions(properties.getPersistenceOptions());
        saveProjectProperties(newProjectProperties);
    }

    @Override
    public ExtendedExprCompiler newExpressionCompiler() {
        ExtendedExprCompiler compiler = new ExtendedExprCompiler();
        compiler.setDatatypeHelperProvider(new IpsProjectDatatypeHelperProvider(this));

        ExtensionFunctionResolversCache resolverCache = getIpsModel().getExtensionFunctionResolverCache(this);
        resolverCache.addExtensionFunctionResolversToCompiler(compiler);
        return compiler;
    }

    /**
     * Saves the project properties to the .ipsproject file.
     */
    private void saveProjectProperties(IIpsProjectProperties properties) {
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element propertiesEl = ((IpsProjectProperties)properties).toXml(doc);
        doc.appendChild(propertiesEl);
        AFile file = getIpsProjectPropertiesFile();
        String charset = getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(doc, charset);
        } catch (TransformerException e) {
            throw new IpsException(new IpsStatus("Error transforming project data to xml string", e)); //$NON-NLS-1$
        }
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(insertNewLineSeparatorsBeforeComment(contents).getBytes(charset));
            if (file.exists()) {
                file.setContents(is, true, null);
            } else {
                file.create(is, null);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus("Error creating byte stream", e)); //$NON-NLS-1$
        } finally {
            IoUtil.close(is);
        }
    }

    private String insertNewLineSeparatorsBeforeComment(String s) {
        StringBuilder newText = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(s, System.lineSeparator());
        boolean firstComment = true;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.indexOf("<!--") != -1) { //$NON-NLS-1$
                if (firstComment) {
                    firstComment = false;
                } else {
                    newText.append(System.lineSeparator());
                }
            }
            newText.append(token);
            newText.append(System.lineSeparator());
        }

        return newText.toString();
    }

    @Override
    public AFile getIpsProjectPropertiesFile() {
        if (propertyFile == null) {
            propertyFile = getProject().getFile(PROPERTY_FILE_EXTENSION_INCL_DOT);
        }
        return propertyFile;
    }

    @Override
    public AJavaProject getJavaProject() {
        return AJavaProject.from(getProject());
    }

    @Override
    public IJavaNamingConvention getJavaNamingConvention() {
        return javaNamingConvention;
    }

    @Override
    public ClassLoader getClassLoaderForJavaProject() {
        return getClassLoaderForJavaProject(ClassLoader.getSystemClassLoader());
    }

    @Override
    public ClassLoader getClassLoaderForJavaProject(ClassLoader parent) {
        ArgumentCheck.notNull(parent);
        // always creates a new classloader
        return IIpsModelExtensions.get()
                .getClassLoaderProviderFactory()
                .getClassLoaderProvider(this, parent)
                .getClassLoader();
    }

    @Override
    public Boolean isJavaProjectErrorFree(boolean checkReferencedJavaProjects) {
        return isJavaProjectErrorFree(getJavaProject(), checkReferencedJavaProjects);
    }

    @CheckForNull
    private Boolean isJavaProjectErrorFree(AJavaProject javaProject, boolean checkReferencedJavaProjects) {
        AProject tmpProject = javaProject.getProject();
        if (!tmpProject.isAccessible() || !javaProject.exists()) {
            return null;
        }
        // implementation note: if the java project has buildpath problems it also hasn't got a
        // build state
        // so we first have to check for problems with the build path. We can't do this via markers
        // as the build path markers
        // are created on a resource change event, and we don't now if it has been executed so far.
        if (getJavaProjectBuildPathProblemSeverity(javaProject) == Severity.ERROR) {
            return Boolean.FALSE;
        }
        Set<AMarker> markers = tmpProject.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
                AResourceTreeTraversalDepth.INFINITE);
        if (containsErrorMarker(markers)) {
            return Boolean.FALSE;
        }
        if (checkReferencedJavaProjects) {
            for (AJavaProject refProject : getJavaProjectsReferencedInClasspath(javaProject)) {
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

    private boolean containsErrorMarker(Set<AMarker> markers) {
        return markers.stream().anyMatch(AMarker::isError);
    }

    private Set<AJavaProject> getJavaProjectsReferencedInClasspath(AJavaProject javaProject) {
        return javaProject.getReferencedJavaProjects();
    }

    /**
     * Returns the relevant referenced {@link IpsProject}s. If <code>includeIndirect</code> is set
     * to true all referenced {@link IpsProject}s will be shown in the resulting list. If
     * <code>includeIndirect</code> is false only the directly referenced {@link IpsProject}s will
     * be included in the resulting list.
     */
    public List<IIpsProject> getReferencedIpsProjects(boolean includeIndirect) {
        return getIpsObjectPathInternal().getReferencedIpsProjects(includeIndirect);
    }

    @Override
    public List<IIpsProject> getAllReferencedIpsProjects() {
        return getReferencedIpsProjects(true);
    }

    @Override
    public List<IIpsProject> getDirectlyReferencedIpsProjects() {
        return getReferencedIpsProjects(false);
    }

    @Override
    public boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect) {
        if (otherProject == null || otherProject == this) {
            return false;
        }

        return isReferencedByInternal(otherProject, considerIndirect);
    }

    private boolean isReferencedByInternal(IIpsProject otherProject, boolean considerIndirect) {
        IpsObjectPath otherPath = ((IpsProject)otherProject).getIpsObjectPathInternal();
        List<IIpsProject> referencedProjects = otherPath.getReferencedIpsProjects(considerIndirect);
        for (IIpsProject referencedProject : referencedProjects) {
            if (equals(referencedProject)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IIpsProject[] findReferencingProjects(boolean includeIndirect) {
        IIpsProject[] projects = getIpsModel().getIpsProjects();
        List<IIpsProject> result = new ArrayList<>(projects.length);
        for (IIpsProject project2 : projects) {
            if (isReferencedBy(project2, includeIndirect)) {
                result.add(project2);
            }
        }
        return result.toArray(new IIpsProject[result.size()]);
    }

    @Override
    public IIpsProject[] findReferencingProjectLeavesOrSelf() {
        IIpsProject[] ipsPprojects = getIpsModel().getIpsProjects();
        List<IIpsProject> result = new ArrayList<>(ipsPprojects.length);
        result.add(this);
        for (IIpsProject ipsProject : ipsPprojects) {
            if (isReferencedBy(ipsProject, true)) {
                boolean foundDependent = false;
                for (Iterator<IIpsProject> iterator = result.iterator(); iterator.hasNext();) {
                    IIpsProject aResult = iterator.next();
                    if (ipsProject.isReferencedBy(aResult, true)) {
                        foundDependent = true;
                        break;
                    } else {
                        if (aResult.isReferencedBy(ipsProject, true)) {
                            iterator.remove();
                        }
                    }
                }
                if (!foundDependent) {
                    result.add(ipsProject);
                }
            }
        }
        return result.toArray(new IIpsProject[result.size()]);
    }

    @Override
    public IDependencyGraph getDependencyGraph() {
        return getIpsModel().getDependencyGraph(this);
    }

    @Override
    public boolean canBeBuild() {
        try {
            return !validate().containsErrorMsg();
        } catch (IpsException e) {
            IpsLog.log(e);
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
    public IIpsObjectPath getIpsObjectPath() {
        return getProperties().getIpsObjectPath();
    }

    /**
     * Returns a <strong>reference</strong> to the IPS object path, in contrast to the
     * getIpsObjectPath() method that returns a copy.
     */
    public IpsObjectPath getIpsObjectPathInternal() {
        return (IpsObjectPath)getPropertiesInternal().getIpsObjectPath();
    }

    @Override
    public AFolder[] getOutputFolders() {
        return getIpsObjectPathInternal().getOutputFolders();
    }

    @Override
    public boolean isAccessibleViaIpsObjectPath(IIpsObject ipsObject) {
        if (ipsObject == null) {
            return false;
        }

        IIpsSrcFile file = findIpsSrcFile(ipsObject.getQualifiedNameType());
        if (file == null) {
            return false;
        }

        return file.equals(ipsObject.getIpsSrcFile());
    }

    public void setValueDatatypes(String[] ids) {
        IIpsProjectProperties properties = getProperties();
        properties.setPredefinedDatatypesUsed(ids);
        saveProjectProperties(properties);
    }

    @Override
    public void setIpsObjectPath(IIpsObjectPath newPath) {
        IIpsProjectProperties properties = getIpsModel().getIpsProjectProperties(this);
        properties.setIpsObjectPath(newPath);
        saveProjectProperties(properties);
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        try {
            if (!getNamingConventions().validateIpsPackageRootName(name).containsErrorMsg()) {
                return new IpsPackageFragmentRoot(this, name);
            }
        } catch (IpsException e) {
            // nothing to do
        }
        return null;
    }

    @Override
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() {
        return getIpsPackageFragmentRoots(true);
    }

    @Override
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots(boolean resolveContainerEntries) {
        List<IIpsPackageFragmentRoot> roots = new ArrayList<>();
        IIpsObjectPathEntry[] entries = getIpsObjectPathInternal().getEntries();
        addPackageFragmentRoots(Arrays.asList(entries), roots, resolveContainerEntries);
        return roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }

    private void addPackageFragmentRoots(List<IIpsObjectPathEntry> entries,
            List<IIpsPackageFragmentRoot> roots,
            boolean resolveContainerEntries) {
        for (IIpsObjectPathEntry entry : entries) {
            if (resolveContainerEntries && entry.isContainer()) {
                IIpsContainerEntry containerEntry = (IIpsContainerEntry)entry;
                addPackageFragmentRoots(containerEntry.resolveEntries(), roots, resolveContainerEntries);
            }
            IIpsPackageFragmentRoot root = entry.getIpsPackageFragmentRoot();
            if (root != null) {
                roots.add(root);
            }
        }
    }

    @Override
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(String name) {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.getName().equals(name)) {
                return root;
            }
        }
        return null;
    }

    @Override
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(java.nio.file.Path path) {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        String pathRoot = PathUtil.toPortableString(path);
        for (IIpsPackageFragmentRoot root : roots) {
            if (pathRoot.startsWith(root.getName())) {
                return root;
            }
        }
        return null;
    }

    @Override
    public AResource[] getNonIpsResources() {
        AContainer cont = (AContainer)getCorrespondingResource();
        if (!cont.isAccessible()) {
            return new AResource[0];
        }
        List<AResource> childResources = new ArrayList<>();
        for (AResource child : cont) {
            if (!isPackageFragmentRoot(child) & !isJavaFolder(child)) {
                childResources.add(child);
            }
        }
        AResource[] resArray = new AResource[childResources.size()];

        return childResources.toArray(resArray);
    }

    private boolean isJavaFolder(AResource resource) {
        return getJavaProject().isJavaFolder(resource);
    }

    /**
     * Returns true if the given AResource is a folder that corresponds to an IpsPackageFragmentRoot
     * of this IpsProject, false otherwise.
     */
    private boolean isPackageFragmentRoot(AResource res) {
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
        return getCorrespondingResource().exists();
    }

    @Override
    public Locale getFormulaLanguageLocale() {
        return getPropertiesInternal().getFormulaLanguageLocale();
    }

    @Override
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode() {
        IIpsProjectProperties properties = getPropertiesInternal();
        return getIpsModel()
                .getChangesOverTimeNamingConvention(properties.getChangesOverTimeNamingConventionIdForGeneratedCode());
    }

    @Override
    public AResource getCorrespondingResource() {
        return getProject();
    }

    @Override
    public IIpsElement[] getChildren() {
        return getIpsPackageFragmentRoots();
    }

    @Override
    public IIpsProject getIpsProject() {
        return this;
    }

    //
    // Find methods with single result
    //

    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) {
        IIpsSrcFile file = findIpsSrcFile(type, qualifiedName);
        if (file == null) {
            return null;
        }

        return file.getIpsObject();
    }

    @Override
    public IIpsObject findIpsObject(QualifiedNameType nameType) {
        IIpsSrcFile file = findIpsSrcFile(nameType);
        if (file == null) {
            return null;
        }

        return file.getIpsObject();
    }

    @Override
    public boolean findDuplicateIpsSrcFile(QualifiedNameType qNameType) {
        return getIpsObjectPathInternal().findDuplicateIpsSrcFile(qNameType);
    }

    @Override
    public boolean findDuplicateIpsSrcFile(IpsObjectType type, String qualifiedName) {
        return findDuplicateIpsSrcFile(new QualifiedNameType(qualifiedName, type));
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    @Override
    public IProductCmptType findProductCmptType(String qualifiedName) {
        return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
    }

    @Override
    public IProductCmpt findProductCmpt(String qualifiedName) {
        return (IProductCmpt)findIpsObject(IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    @Override
    public IProductCmpt findProductTemplate(String qualifiedName) {
        return (IProductCmpt)findIpsObject(IpsObjectType.PRODUCT_TEMPLATE, qualifiedName);
    }

    @Override
    public Collection<IIpsSrcFile> findProductCmptByUnqualifiedName(String unqualifiedName) {
        return unqualifiedNameCache.findProductCmptByUnqualifiedName(unqualifiedName);
    }

    @Override
    public IEnumType findEnumType(String qualifiedName) {
        return (IEnumType)findIpsObject(IpsObjectType.ENUM_TYPE, qualifiedName);
    }

    @Override
    public List<IEnumType> findEnumTypes(boolean includeAbstract, boolean includeNotContainingValues) {

        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();
        findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.ENUM_TYPE);
        List<IEnumType> enumTypes = filesToIpsObjects(ipsSrcFiles, IEnumType.class);
        if (includeAbstract && includeNotContainingValues) {
            return enumTypes;
        }

        List<IEnumType> filteredList = new ArrayList<>(enumTypes.size());
        for (IEnumType currentEnumType : enumTypes) {
            if ((!includeAbstract && currentEnumType.isAbstract())
                    || (!includeNotContainingValues && currentEnumType.isExtensible())) {
                continue;
            }
            filteredList.add(currentEnumType);
        }
        return filteredList;
    }

    @Override
    public IProductCmpt findProductCmptByRuntimeId(String runtimeId) {
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

    public ITableStructure findTableStructure(String tableContetnsQName) {
        return (ITableStructure)findIpsObject(IpsObjectType.TABLE_STRUCTURE, tableContetnsQName);
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qNameType) {
        return getIpsObjectPathInternal().findIpsSrcFile(qNameType);
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(IpsObjectType type, String qualifiedName) {
        return findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
    }

    @SuppressWarnings("unchecked")
    private <T extends IIpsObject> List<T> filesToIpsObjects(List<IIpsSrcFile> files, Class<? extends T> clazz) {

        List<T> objects = new ArrayList<>(files.size());
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
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result) {
        getIpsObjectPathInternal().collectAllIpsSrcFilesOfSrcFolderEntries(result);
    }

    @Override
    public IIpsSrcFile[] findIpsSrcFiles(IpsObjectType type) {
        List<IIpsSrcFile> foundSrcFiles = findAllIpsSrcFiles(type);
        return foundSrcFiles.toArray(new IIpsSrcFile[foundSrcFiles.size()]);
    }

    @Override
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result) {
        result.addAll(findAllIpsSrcFilesInternal());
    }

    @Override
    public List<IIpsSrcFile> findAllIpsSrcFiles(IpsObjectType... ipsObjectTypes) {
        return findAllIpsSrcFilesInternal(ipsObjectTypes);
    }

    @Override
    public IEnumContent findEnumContent(IEnumType enumType) {
        ArgumentCheck.notNull(enumType, this);

        if (enumType.isExtensible()) {
            IIpsSrcFile enumContentSrcFile = findIpsSrcFile(IpsObjectType.ENUM_CONTENT, enumType.getEnumContentName());
            if (enumContentSrcFile != null && enumContentSrcFile.exists()) {
                return (IEnumContent)enumContentSrcFile.getIpsObject();
            }
        }
        return null;
    }

    private void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType ipsObjectType) {
        result.addAll(getIpsObjectPathInternal().findIpsSrcFiles(ipsObjectType));
    }

    protected List<IIpsSrcFile> findAllIpsSrcFilesInternal(IpsObjectType... ipsObjectTypesVarArg) {
        return getIpsObjectPathInternal().findIpsSrcFiles(ipsObjectTypesVarArg);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid) {
        return findDatatypes(valuetypesOnly, includeVoid, true);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives) {

        return findDatatypes(valuetypesOnly, includeVoid, includePrimitives, null);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes) {

        return findDatatypes(valuetypesOnly, includeVoid, includePrimitives, excludedDatatypes, true);
    }

    @Override
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes,
            boolean includeAbstract) {

        Set<Datatype> result = new LinkedHashSet<>();
        getDatatypesDefinedInProjectPropertiesInclSubprojects(valuetypesOnly, includeVoid, includePrimitives, result);

        List<IEnumType> enumTypeList = findEnumTypes(includeAbstract, true);
        for (IEnumType enumType : enumTypeList) {
            if (enumType.isInextensibleEnum()) {
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
            Set<Datatype> result) {

        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        getIpsModel().getDatatypesDefinedInProjectProperties(this, valuetypesOnly, includePrimitives, result);
        List<IIpsProject> referencedProjects = getAllReferencedIpsProjects();
        for (IIpsProject referencedProject : referencedProjects) {
            getIpsModel().getDatatypesDefinedInProjectProperties(referencedProject, valuetypesOnly, includePrimitives,
                    result);
        }
    }

    private void findDatatypesDefinedByIpsObjects(Set<Datatype> result) {
        List<IIpsSrcFile> refDatatypeFiles = new ArrayList<>();
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
    public EnumDatatype[] findEnumDatatypes() {
        // TODO this implementation can be improved and instanceof can be avoided. Therefore the
        // storage of EnumDatatypes an Datatypes
        // has to be separated within the IpsModel class

        Datatype[] datatypes = findDatatypes(true, false);
        ArrayList<Datatype> enumDatatypeList = new ArrayList<>();
        for (Datatype datatype : datatypes) {
            if (datatype instanceof EnumDatatype) {
                enumDatatypeList.add(datatype);
            }
        }

        return enumDatatypeList.toArray(new EnumDatatype[enumDatatypeList.size()]);
    }

    @Override
    public Datatype findDatatype(String qualifiedName) {
        String qualifiedNameDatatype = qualifiedName;
        if (qualifiedNameDatatype.equals(Datatype.VOID.getQualifiedName())) {
            return Datatype.VOID;
        }
        Datatype type = findDatatypeDefinedInProjectPropertiesInclSubprojects(qualifiedNameDatatype);
        if (type != null) {
            return type;
        }
        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedNameDatatype);
        if (arrayDimension > 0) {
            qualifiedNameDatatype = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedNameDatatype);
        }
        IpsObjectType[] objectTypes = getIpsModel().getIpsObjectTypes();
        for (IpsObjectType objectType : objectTypes) {
            if (objectType.isDatatype()) {
                type = (Datatype)findIpsObject(objectType, qualifiedNameDatatype);
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
            throw new IllegalArgumentException("The qualified name: \"" + qualifiedNameDatatype //$NON-NLS-1$
                    + "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
        }
        return getEnumTypeDatatypeAdapter(qualifiedNameDatatype, this);
    }

    private EnumTypeDatatypeAdapter getEnumTypeDatatypeAdapter(String qualifiedName, IIpsProject ipsProject) {

        IIpsSrcFile enumTypeSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, qualifiedName);
        if (enumTypeSrcFile != null && enumTypeSrcFile.exists()) {
            IEnumType enumType = (IEnumType)enumTypeSrcFile.getIpsObject();
            if (enumType.isInextensibleEnum()) {
                return new EnumTypeDatatypeAdapter(enumType, null);
            }
            IEnumContent enumContent = ipsProject.findEnumContent(enumType);
            return new EnumTypeDatatypeAdapter(enumType, enumContent);
        }
        return null;
    }

    @Override
    public ValueDatatype findValueDatatype(String qualifiedName) {
        if (qualifiedName == null) {
            return null;
        }
        String qualifiedNameDatatype = qualifiedName;
        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedNameDatatype);
        if (arrayDimension > 0) {
            qualifiedNameDatatype = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedNameDatatype);
        }
        ValueDatatype type = findValueDatatypeInclSubprojects(this, qualifiedNameDatatype);
        if (arrayDimension == 0) {
            return type;
        }
        if (type != null) {
            return new ArrayOfValueDatatype(type, arrayDimension);
        }

        throw new IllegalArgumentException("The qualified name: \"" + qualifiedNameDatatype //$NON-NLS-1$
                + "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private ValueDatatype findValueDatatypeInclSubprojects(IpsProject ipsProject, String qualifiedName) {

        ValueDatatype datatype = getIpsModel().getValueDatatypeDefinedInProjectProperties(ipsProject, qualifiedName);
        if (datatype != null) {
            return datatype;
        }
        datatype = getEnumTypeDatatypeAdapter(qualifiedName, ipsProject);
        if (datatype != null) {
            return datatype;
        }
        return findValueDatatypeInReferencedProjects(ipsProject, qualifiedName);
    }

    private ValueDatatype findValueDatatypeInReferencedProjects(IpsProject ipsProject, String qualifiedName) {
        ValueDatatype datatype;
        List<IIpsProject> referencedProjects = ipsProject.getAllReferencedIpsProjects();
        for (IIpsProject referencedProject : referencedProjects) {
            datatype = getIpsModel().getValueDatatypeDefinedInProjectProperties(referencedProject, qualifiedName);
            if (datatype != null) {
                return datatype;
            }
            datatype = getEnumTypeDatatypeAdapter(qualifiedName, referencedProject);
            if (datatype != null) {
                return datatype;
            }
        }
        return null;
    }

    private Datatype findDatatypeDefinedInProjectPropertiesInclSubprojects(String qualifiedName) {
        if (qualifiedName == null) {
            return null;
        }
        String qualifiedNameDatatype = qualifiedName;
        int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedNameDatatype);
        if (arrayDimension > 0) {
            qualifiedNameDatatype = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedNameDatatype);
        }

        Datatype type = findDatatypeDefinedInProjectPropertiesInclSubprojects(this, qualifiedNameDatatype);
        if (arrayDimension == 0) {
            return type;
        }
        if (type instanceof ValueDatatype) {
            return new ArrayOfValueDatatype(type, arrayDimension);
        }

        throw new IllegalArgumentException("The qualified name: \"" + qualifiedNameDatatype //$NON-NLS-1$
                + "\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private Datatype findDatatypeDefinedInProjectPropertiesInclSubprojects(IIpsProject ipsProject,
            String qualifiedName) {
        Datatype datatype = getIpsModel().getDatatypeDefinedInProjectProperties(ipsProject, qualifiedName);
        if (datatype != null) {
            return datatype;
        }

        List<IIpsProject> referencedProjects = ipsProject.getAllReferencedIpsProjects();
        for (IIpsProject referencedProject : referencedProjects) {
            datatype = getIpsModel().getDatatypeDefinedInProjectProperties(referencedProject, qualifiedName);
            if (datatype != null) {
                return datatype;
            }
        }
        return null;
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        if (datatype instanceof ArrayOfValueDatatype arrayDatatype) {
            return new ArrayOfValueDatatypeHelper(arrayDatatype, getDatatypeHelper(arrayDatatype.getBasicDatatype()));
        }

        DatatypeHelper helper = getIpsArtefactBuilderSet().getDatatypeHelper(datatype);
        if (helper != null) {
            return helper;
        }

        List<IIpsProject> projects = getDirectlyReferencedIpsProjects();
        for (IIpsProject project2 : projects) {
            helper = project2.getDatatypeHelper(datatype);
            if (helper != null) {
                return helper;
            }
        }

        return null;
    }

    @Override
    public DatatypeHelper findDatatypeHelper(String qName) {
        Datatype datatype = findDatatype(qName);
        return getDatatypeHelper(datatype);
    }

    @Override
    public List<ValueSetType> getValueSetTypes(ValueDatatype datatype) {
        List<ValueSetType> types = new ArrayList<>();
        types.add(ValueSetType.DERIVED);
        if (datatype == null) {
            types.add(ValueSetType.UNRESTRICTED);
            return types;
        }
        if (datatype instanceof NumericDatatype) {
            return ValueSetType.getNumericValueSetTypesAsList();
        }
        if (datatype instanceof ArrayOfValueDatatype) {
            types.add(ValueSetType.UNRESTRICTED);
            return types;
        }
        types.add(ValueSetType.UNRESTRICTED);
        types.add(ValueSetType.ENUM);
        if (datatype instanceof StringDatatype) {
            types.add(ValueSetType.STRINGLENGTH);
        }
        return types;
    }

    @Override
    public boolean isValueSetTypeApplicable(ValueDatatype datatype, ValueSetType valueSetType) {
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
    public IIpsSrcFile[] findAllProductCmptSrcFiles(IProductCmptType productCmptType, boolean includeCmptsForSubtypes) {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        List<IIpsSrcFile> result = findAllProducts(ipsSrcFiles, productCmptType, includeCmptsForSubtypes);
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public List<IIpsSrcFile> findAllProductTemplates(IProductCmptType productCmptType, boolean includeSubtypes) {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.PRODUCT_TEMPLATE);
        return findAllProducts(ipsSrcFiles, productCmptType, includeSubtypes);
    }

    private List<IIpsSrcFile> findAllProducts(IIpsSrcFile[] ipsSrcFiles,
            IProductCmptType productCmptType,
            boolean includeSubtypes) {
        List<IIpsSrcFile> result = new ArrayList<>();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            String referencedTypeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            if (productCmptType == null || productCmptType.getQualifiedName().equals(referencedTypeName)) {
                result.add(ipsSrcFile);
            } else if (includeSubtypes) {
                IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(referencedTypeName);
                if (type == null) {
                    continue;
                }
                if (type.isSubtypeOrSameType(productCmptType, type.getIpsProject())) {
                    result.add(ipsSrcFile);
                }
            }
        }
        return result;
    }

    @Override
    public List<IIpsSrcFile> findCompatibleProductTemplates(IProductCmptType productCmptType) {
        IIpsSrcFile[] allTemplates = findIpsSrcFiles(IpsObjectType.PRODUCT_TEMPLATE);
        List<String> subtypes = getSupertypes(productCmptType);
        List<IIpsSrcFile> result = new ArrayList<>();
        for (IIpsSrcFile templateCandidate : allTemplates) {
            String referencedTypeName = templateCandidate.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            if (subtypes.contains(referencedTypeName)) {
                result.add(templateCandidate);
            }
        }
        return result;
    }

    @Override
    public Tree<IIpsSrcFile> findTemplateHierarchy(IProductCmpt template) {
        return TemplateHierarchyFinder.findTemplateHierarchyFor(template, this);
    }

    protected List<String> getSupertypes(IType type) {
        final List<String> supertypes = new ArrayList<>();
        TypeHierarchyVisitor<IType> collector = new TypeHierarchyVisitor<>(this) {

            @Override
            public boolean visit(IType type) {
                supertypes.add(type.getQualifiedName());
                return true;
            }
        };
        collector.start(type);
        return supertypes;
    }

    @Override
    public IIpsSrcFile[] findAllTestCaseSrcFiles(ITestCaseType testCaseType) {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.TEST_CASE);
        if (testCaseType == null) {
            return ipsSrcFiles;
        }
        List<IIpsSrcFile> result = new ArrayList<>(ipsSrcFiles.length);
        for (IIpsSrcFile srcFile : ipsSrcFiles) {
            String testCaseTypeCandidateQName = srcFile.getPropertyValue(ITestCase.PROPERTY_TEST_CASE_TYPE);
            if (testCaseType.getQualifiedName().equals(testCaseTypeCandidateQName)) {
                result.add(srcFile);
            }
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public IIpsSrcFile[] findAllEnumContentSrcFiles(IEnumType enumType, boolean includingSubtypes) {
        IIpsSrcFile[] ipsSrcFiles = findIpsSrcFiles(IpsObjectType.ENUM_CONTENT);
        if (enumType == null) {
            return ipsSrcFiles;
        }
        List<IIpsSrcFile> result = new ArrayList<>(ipsSrcFiles.length);
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
    public List<IIpsSrcFile> findAllTableContentsSrcFiles(ITableStructure structure) {
        if (structure == null) {
            return findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        }

        return getTableContentsStructureCache().getTableContents(structure.getIpsSrcFile());
    }

    public TableContentsStructureCache getTableContentsStructureCache() {
        return tableContentsStructureCache;
    }

    @Override
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() {
        List<IIpsPackageFragmentRoot> result = new ArrayList<>();
        getSourceIpsFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    public void getSourceIpsFragmentRoots(List<IIpsPackageFragmentRoot> result) {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.isBasedOnSourceFolder()) {
                result.add(root);
            }
        }
    }

    @Override
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet() {
        return getIpsModel().getIpsArtefactBuilderSet(this, false);
    }

    @Override
    public void reinitializeIpsArtefactBuilderSet() {
        getIpsModel().getIpsArtefactBuilderSet(this, true);
    }

    @Override
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
        return getPropertiesInternal().getProductCmptNamingStrategy();
    }

    @Override
    public String getRuntimeIdPrefix() {
        return getPropertiesInternal().getRuntimeIdPrefix();
    }

    @Override
    public MessageList validate() {
        MessageList result = getJavaProject().validateJavaProjectBuildPath();
        if (!getIpsProjectPropertiesFile().exists()) {
            String text = Messages.IpsProject_msgMissingDotIpsprojectFile;
            Message msg = new Message(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE, text, Message.ERROR, this);
            result.add(msg);
            return result;
        }

        IpsProjectProperties props = getPropertiesInternal();
        if (!props.isCreatedFromParsableFileContents()) {
            if (props.isValidateIpsSchema()) {
                for (String xsdError : props.getXsdValidationHandler().getXsdValidationErrors()) {
                    Message msg = new Message(IIpsProject.MSGCODE_XSD_VALIDATION_ERROR, xsdError, Message.ERROR, this);
                    result.add(msg);
                }
            }
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

        if (props.isValidateIpsSchema()) {
            for (String xsdWarning : props.getXsdValidationHandler().getXsdValidationWarnings()) {
                Message msg = new Message(IIpsProject.MSGCODE_XSD_VALIDATION_WARNING, xsdWarning, Message.WARNING,
                        this);
                result.add(msg);
            }
        }

        validateRequiredFeatures(result, props);
        validateMigration(result);
        validateDuplicateTocFilePath(result);
        validateIpsObjectPathCycle(result);
        validateVersionProvider(result);
        validateMarkerEnums(result);

        return result;
    }

    private void validateMarkerEnums(MessageList messageList) {
        IIpsProjectProperties properties = getReadOnlyProperties();
        Set<String> markerEnumQNames = properties.getMarkerEnums();
        Set<String> checkedIds = new HashSet<>();

        for (String enumQName : markerEnumQNames) {
            validateEnumQName(messageList, enumQName);
        }
        String duplicateIds = markerEnumQNames.stream()
                .map(this::findEnumType)
                .filter(Objects::nonNull)
                .peek(enumType -> validateEnumQName(messageList, enumType.getQualifiedName()))
                .flatMap(enumType -> enumType.findAllIdentifierAttributeValues(getIpsProject()).stream())
                .flatMap(id -> {
                    if (!checkedIds.add(id)) {
                        return Stream.of(id);
                    } else {
                        return Stream.empty();
                    }
                }).collect(Collectors.joining("; "));
        if (!duplicateIds.isEmpty()) {
            String msg = MessageFormat.format(Messages.IpsProjectProperties_msgUniqueMarkerIds, duplicateIds);
            messageList.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS, msg,
                    Message.ERROR, getIpsProjectPropertiesFile()));
        }
    }

    private void validateEnumQName(MessageList messageList, String enumQName) {
        IIpsSrcFile ipsSrcFile = findIpsSrcFile(new QualifiedNameType(enumQName, IpsObjectType.ENUM_TYPE));
        if (ipsSrcFile == null || !ipsSrcFile.exists()) {
            messageList.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS,
                    Messages.IpsProjectProperties_unknownMarkerEnums, Message.ERROR, getIpsProjectPropertiesFile()));
        } else {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            validateMarkerEnumProperties(enumType, messageList);
        }
    }

    private void validateMarkerEnumProperties(IEnumType markerEnum, MessageList result) {
        if (markerEnum.isAbstract()) {
            String msg = MessageFormat.format(Messages.IpsProjectProperties_msgAbstractMarkerEnumsNotAllowed,
                    markerEnum.getQualifiedName());
            result.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS, msg, Message.ERROR,
                    getIpsProjectPropertiesFile()));
        }
        if (markerEnum.isExtensible()) {
            String msg = MessageFormat.format(Messages.IpsProjectProperties_msgExtensibleMarkerEnumsNotAllowed,
                    markerEnum.getQualifiedName());
            result.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS, msg, Message.ERROR,
                    getIpsProjectPropertiesFile()));
        }
    }

    private Severity getJavaProjectBuildPathProblemSeverity(AJavaProject javaProject) {
        return javaProject.validateJavaProjectBuildPath().getSeverity();
    }

    private void validateIpsObjectPathCycle(MessageList result) {
        if (getIpsObjectPathInternal().detectCycle()) {
            String msg = Messages.IpsProject_msgCycleInIpsObjectPath;
            result.add(new Message(MSGCODE_CYCLE_IN_IPS_OBJECT_PATH, msg, Message.ERROR, this));
        }
    }

    private void validateMigration(MessageList result) {
        IIpsFeatureVersionManager[] managers = IIpsModelExtensions.get().getIpsFeatureVersionManagers();
        for (IIpsFeatureVersionManager manager : managers) {
            try {
                manager.getMigrationOperations(this);
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                IpsLog.log(e);
                String msg = MessageFormat.format(Messages.IpsProject_msgInvalidMigrationInformation,
                        manager.getFeatureId());
                result.add(new Message(MSGCODE_INVALID_MIGRATION_INFORMATION, msg, Message.ERROR, this));
            }
        }
    }

    private void validateRequiredFeatures(MessageList ml, IIpsProjectProperties props) {
        String[] features = props.getRequiredIpsFeatureIds();

        for (String feature : features) {
            IIpsFeatureVersionManager manager = IIpsModelExtensions.get().getIpsFeatureVersionManager(feature);
            if (manager == null) {
                String msg = MessageFormat.format(Messages.IpsProject_msgNoFeatureManager, feature);
                ml.add(new Message(MSGCODE_NO_VERSIONMANAGER, msg, Message.ERROR, this));
                continue;
            }
            String minVersion = props.getMinRequiredVersionNumber(feature);
            if (manager.compareToCurrentVersion(minVersion) > 0
                    && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                String msg = MessageFormat.format(Messages.IpsProject_msgVersionTooLow, manager.getCurrentVersion(),
                        minVersion, feature);
                ml.add(new Message(MSGCODE_VERSION_TOO_LOW, msg, Message.ERROR, this));
            }

            if (manager.compareToCurrentVersion(minVersion) < 0
                    && !manager.isCurrentVersionCompatibleWith(minVersion)) {
                String msg = MessageFormat.format(Messages.IpsProject_msgIncompatibleVersions,
                        manager.getCurrentVersion(), minVersion, feature);
                ml.add(new Message(MSGCODE_INCOMPATIBLE_VERSIONS, msg, Message.ERROR, this));
            }
        }
    }

    /**
     * Validates for duplicate base package generated entries inside the referenced project
     */
    private void validateDuplicateTocFilePath(MessageList result) {
        // check for same toc file path in referenced projects
        List<IPath> tocPaths = collectTocPaths(getIpsArtefactBuilderSet(), this);
        List<IIpsProject> referencedProjects = getDirectlyReferencedIpsProjects();
        for (IIpsProject referencedProject : referencedProjects) {
            IIpsArtefactBuilderSet builderSet = referencedProject.getIpsArtefactBuilderSet();
            List<IPath> tocPathsInRefProject = collectTocPaths(builderSet, referencedProject);

            for (IPath tocPath : tocPathsInRefProject) {
                if (tocPaths.contains(tocPath)) {
                    String msg = MessageFormat.format(Messages.IpsProject_msgDuplicateTocFilePath, tocPath,
                            referencedProject.getName());
                    result.add(new Message(MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS, msg, Message.ERROR,
                            this));
                }
            }
        }
    }

    private void validateVersionProvider(MessageList result) {
        String versionProviderId = getReadOnlyProperties().getVersionProviderId();
        if (IpsStringUtils.isNotEmpty(versionProviderId)
                && !IIpsModelExtensions.get().getVersionProviderFactories().containsKey(versionProviderId)) {
            String text = MessageFormat.format(Messages.VersionProviderExtensionPoint_error_invalidVersionProvider,
                    getReadOnlyProperties().getVersionProviderId());
            result.newError(IIpsProjectProperties.MSGCODE_INVALID_VERSION_SETTING, text, getProperties(),
                    IIpsProjectProperties.PROPERTY_VERSION_PROVIDER_ID);
        }
    }

    private List<IPath> collectTocPaths(IIpsArtefactBuilderSet builderSet, IIpsProject ipsProject) {
        List<IPath> tocPaths = new ArrayList<>();
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
     * Returns a cached ClassLoaderProvider for the Java project that belongs to this IPS project.
     */
    @Override
    public IClassLoaderProvider getClassLoaderProviderForJavaProject() {
        return getIpsModel().getClassLoaderProvider(this);
    }

    @Override
    public IIpsProjectNamingConventions getNamingConventions() {
        if (namingConventions == null) {
            namingConventions = new DefaultIpsProjectNamingConventions(this);
        }

        return namingConventions;
    }

    @Override
    public MessageList checkForDuplicateRuntimeIds(IIpsSrcFile... cmptsToCheck) {
        MessageList result = new MessageList();
        for (IIpsSrcFile cmptToCheck : cmptsToCheck) {
            if (!cmptToCheck.exists()) {
                continue;
            }

            runtimeIdCache
                    .findProductCmptByRuntimeId(cmptToCheck.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID))
                    .stream()
                    .filter(p -> !p.equals(cmptToCheck))
                    .forEach(p -> {
                        ObjectProperty[] invalidObjectProperties = new ObjectProperty[1];
                        invalidObjectProperties[0] = new ObjectProperty(cmptToCheck.getIpsObject(),
                                IProductCmpt.PROPERTY_RUNTIME_ID);

                        String msg = MessageFormat.format(Messages.IpsProject_msgRuntimeIDCollision,
                                cmptToCheck.getQualifiedNameType().getName(), p.getQualifiedNameType().getName());
                        result.add(
                                new Message(MSGCODE_RUNTIME_ID_COLLISION, msg, Message.ERROR, invalidObjectProperties));
                    });
        }
        return result;
    }

    @Override
    public boolean isResourceExcludedFromProductDefinition(AResource resource) {
        if (resource == null) {
            return false;
        }
        IIpsProjectProperties props = getPropertiesInternal();
        String projectPath = getProject().getLocation().toString();
        String resourcePath = resource.getLocation().toString();
        if (resourcePath.length() <= projectPath.length()) {
            return false;
        }
        String location = PathUtil.toPortableString(resource.getProjectRelativePath());
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
    public boolean containsResource(String path) {
        return getIpsObjectPathInternal().containsResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return getIpsObjectPathInternal().getResourceAsStream(path);
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
    public IVersionProvider<?> getVersionProvider() {
        return getIpsModel().getVersionProvider(this);
    }

    @Override
    public void delete() {
        for (IIpsPackageFragmentRoot root : getIpsPackageFragmentRoots()) {
            root.delete();
        }
        getCorrespondingResource().delete(null);
        unqualifiedNameCache.dispose();
        runtimeIdCache.dispose();
        tableContentsStructureCache.dispose();
    }

    @Override
    public void clearCaches() {
        unqualifiedNameCache.clear();
        runtimeIdCache.clear();
        tableContentsStructureCache.clear();
    }

    @Override
    public LinkedHashSet<IIpsSrcFile> getMarkerEnums() {
        return getIpsModel().getMarkerEnums(this);
    }

    public static class EclipseIpsProject extends IpsProject {

        /** The underlying platform project */
        private IProject project;

        public EclipseIpsProject(IProject project) {
            super(IIpsModel.get(), project.getName());
        }

        public EclipseIpsProject(IIpsModel model, String name) {
            super(model, name);
        }

        public IProject getEclipseProject() {
            if (project == null) {
                // we don't have a threading problem here, as projects are only handles!
                project = ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
            }
            return project;
        }

        @Override
        public boolean exists() {
            if (!super.exists()) {
                return false;
            }
            try {
                String[] natures = getEclipseProject().getDescription().getNatureIds();
                for (String nature : natures) {
                    if (nature.equals(IIpsProject.NATURE_ID) || nature.equals(OLD_NATURE_ID)) {
                        return true;
                    }
                }
            } catch (CoreException e) {
                // does not exist
            }
            return false;
        }
    }

    public static class EclipseProjectNature implements IProjectNature {

        private EclipseIpsProject ipsProject;

        @Override
        public IProject getProject() {
            return ipsProject.getEclipseProject();
        }

        @Override
        public void setProject(IProject project) {
            ipsProject = new EclipseIpsProject(project);
        }

        @Override
        public void configure() {
            try {
                IProjectDescription description = getProject().getDescription();
                ICommand command = getIpsBuildCommand();
                if (command == null) {
                    // Add a product definition build command to the build spec
                    ICommand newBuildCommand = description.newCommand();
                    newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
                    addCommandAtFirstPosition(description, newBuildCommand);
                }
            } catch (CoreException e) {
                throw new IpsException(e);
            }
        }

        @Override
        public void deconfigure() {
            // Nothing to do
        }

        /**
         * Finds the specific command for product definition builder.
         */
        private ICommand getIpsBuildCommand() {
            try {
                ICommand[] commands = getProject().getDescription().getBuildSpec();
                for (ICommand command : commands) {
                    if (command.getBuilderName().equals(IpsBuilder.BUILDER_ID)) {
                        return command;
                    }
                }

                return null;
            } catch (CoreException e) {
                throw new IpsException(e);
            }
        }

        /**
         * Adds the command to the build spec
         */
        private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand) {
            ICommand[] oldCommands = description.getBuildSpec();
            ICommand[] newCommands = new ICommand[oldCommands.length + 1];
            System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
            newCommands[0] = newCommand;
            // Commit the spec change into the project
            description.setBuildSpec(newCommands);
            try {
                getProject().setDescription(description, null);
            } catch (CoreException e) {
                throw new IpsException(e);
            }
        }
    }
}

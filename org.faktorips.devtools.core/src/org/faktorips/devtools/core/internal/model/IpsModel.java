/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.builder.EmptyBuilderSet;
import org.faktorips.devtools.core.builder.IDependencyGraph;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.datatype.DatatypeDefinition;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileOffRoot;
import org.faktorips.devtools.core.internal.model.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsproject.ChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.internal.model.ipsproject.ClassLoaderProvider;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.internal.model.ipsproject.VersionProviderExtensionPoint;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ICustomModelExtensions;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of <tt>IIpsModel</tt>.
 * 
 * @see IIpsModel
 */
public class IpsModel extends IpsElement implements IIpsModel, IResourceChangeListener {

    public static final boolean TRACE_MODEL_MANAGEMENT;

    public static final boolean TRACE_MODEL_CHANGE_LISTENERS;

    public static final boolean TRACE_VALIDATION;

    /**
     * We must use a value different from {@link IResource#NULL_STAMP} because otherwise files which
     * do not exist in workspace (like {@link LibraryIpsSrcFile}) would remain cached forever.
     * <p>
     * Described in FIPS-5745
     */
    private static final int INVALID_MOD_STAMP = -42;

    static {
        TRACE_MODEL_MANAGEMENT = Boolean
                .valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/modelmanagement")).booleanValue(); //$NON-NLS-1$
        TRACE_MODEL_CHANGE_LISTENERS = Boolean
                .valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/modelchangelisteners")) //$NON-NLS-1$
                .booleanValue();
        TRACE_VALIDATION = Boolean.valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/validation")) //$NON-NLS-1$
                .booleanValue();
    }

    /**
     * Resource delta visitor used to generate IPS source file contents changed events and trigger a
     * build after changes to the IPS project properties file.
     */
    private ResourceDeltaVisitor resourceDeltaVisitor;

    /** set of model change listeners that are notified about model changes */
    private CopyOnWriteArraySet<ContentsChangeListener> changeListeners = new CopyOnWriteArraySet<ContentsChangeListener>();

    private final Set<IIpsSrcFilesChangeListener> ipsSrcFilesChangeListeners = new CopyOnWriteArraySet<IIpsSrcFilesChangeListener>();

    /** set of modification status change listeners */
    private Set<IModificationStatusChangeListener> modificationStatusChangeListeners = new HashSet<IModificationStatusChangeListener>(
            100);

    /**
     * a map that contains per thread if changes should be broadcasted to the registered listeners
     * or squeezed.
     */
    private Map<Thread, Integer> listenerNoticicationLevelMap = new HashMap<Thread, Integer>();

    /**
     * A map containing the datatypes (value) by id (key). The map is initialized with null to point
     * at the lazy loading mechanism
     */
    private Map<String, Datatype> datatypes = null;

    /**
     * A map containing the project for every name.
     */
    private Map<String, IpsProject> projectMap = new ConcurrentHashMap<String, IpsProject>();

    private List<IIpsArtefactBuilderSetInfo> builderSetInfoList = null;

    /** map containing all changes in time naming conventions by id. */
    private Map<String, IChangesOverTimeNamingConvention> changesOverTimeNamingConventionMap = null;

    /** map containing IpsSrcFileContents as values and IpsSrcFiles as keys. */
    private HashMap<IIpsSrcFile, IpsSrcFileContent> ipsObjectsMap = new HashMap<IIpsSrcFile, IpsSrcFileContent>(1000);

    /** validation result cache */
    private ValidationResultCache validationResultCache = new ValidationResultCache();

    private IpsObjectType[] ipsObjectTypes;

    private final CustomModelExtensions customModelExtensions;

    /**
     * A map containing project data per project.
     */
    private final Map<IIpsProject, IpsProjectData> ipsProjectDatas = new ConcurrentHashMap<IIpsProject, IpsProjectData>(
            3, 0.9f, 2);

    private IpsObjectPathContainerFactory ipsObjectPathContainerFactory = IpsObjectPathContainerFactory
            .newFactoryBasedOnExtensions();

    public IpsModel() {
        super(null, "IpsModel"); //$NON-NLS-1$
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.Constructor(): IpsModel created."); //$NON-NLS-1$
        }
        customModelExtensions = new CustomModelExtensions(this);
        initIpsObjectTypes();
        // has to be done after the ips object types are initialized!
        resourceDeltaVisitor = new ResourceDeltaVisitor(this);
    }

    @Override
    public ICustomModelExtensions getCustomModelExtensions() {
        return customModelExtensions;
    }

    private void initIpsObjectTypes() {
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.initIpsObjectType: start."); //$NON-NLS-1$
        }
        List<IpsObjectType> types = new ArrayList<IpsObjectType>();
        types.add(IpsObjectType.PRODUCT_CMPT);
        types.add(IpsObjectType.PRODUCT_TEMPLATE);
        types.add(IpsObjectType.TEST_CASE);
        types.add(IpsObjectType.POLICY_CMPT_TYPE);
        types.add(IpsObjectType.PRODUCT_CMPT_TYPE);
        types.add(IpsObjectType.TABLE_STRUCTURE);
        types.add(IpsObjectType.TABLE_CONTENTS);
        types.add(IpsObjectType.TEST_CASE_TYPE);
        types.add(IpsObjectType.BUSINESS_FUNCTION);
        types.add(IpsObjectType.ENUM_TYPE);
        types.add(IpsObjectType.ENUM_CONTENT);

        ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(ExtensionPoints.IPS_OBJECT_TYPE);
        for (IExtension extension : extensions) {
            List<IpsObjectType> additionalTypes = createIpsObjectTypes(extension);
            for (IpsObjectType objType : additionalTypes) {
                addIpsObjectTypeIfNotDuplicate(types, objType);
            }
        }
        IpsObjectType[] typesArray = types.toArray(new IpsObjectType[types.size()]);
        ipsObjectTypes = typesArray;
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.initIpsObjectType: finished."); //$NON-NLS-1$
        }
    }

    private void addIpsObjectTypeIfNotDuplicate(List<IpsObjectType> types, IpsObjectType newType) {
        for (IpsObjectType exisingType : types) {
            if (exisingType.getFileExtension().equalsIgnoreCase(newType.getFileExtension())) {
                IpsPlugin.log(new IpsStatus("Can't register IpsObjectType " + newType //$NON-NLS-1$
                        + " as it has the same file extension as the type " + exisingType)); //$NON-NLS-1$
                return;
            }
        }
        types.add(newType);
    }

    private List<IpsObjectType> createIpsObjectTypes(IExtension extension) {
        IpsObjectType type = null;
        List<IpsObjectType> validTypes = new ArrayList<IpsObjectType>();
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if (!"ipsobjecttype".equalsIgnoreCase(configElements[i].getName())) { //$NON-NLS-1$
                String text = "Illegal ips object type definition" + extension.getUniqueIdentifier() //$NON-NLS-1$
                        + ". Expected Config Element <ipsobjectytpe> was " //$NON-NLS-1$
                        + configElements[i].getName();
                IpsPlugin.log(new IpsStatus(text));
                continue;
            }
            type = ExtensionPoints.createExecutableExtension(extension, configElements[i], "class", //$NON-NLS-1$
                    IpsObjectType.class);

            if (type == null) {
                String text = "Illegal ips object type definition " + extension.getUniqueIdentifier(); //$NON-NLS-1$
                IpsPlugin.log(new IpsStatus(text));
            } else {
                validTypes.add(type);
            }
        }
        return validTypes;
    }

    /**
     * Returns the data for the given IPS project.
     */
    private IpsProjectData getIpsProjectData(IIpsProject ipsProject) {
        IpsProjectData data = ipsProjectDatas.get(ipsProject);
        if (data == null) {
            data = new IpsProjectData(ipsProject, ipsObjectPathContainerFactory);
            ipsProjectDatas.put(ipsProject, data);
        }
        return data;
    }

    public void startListeningToResourceChanges() {
        getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
                | IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_REFRESH);
    }

    public void stopListeningToResourceChanges() {
        getWorkspace().removeResourceChangeListener(this);
    }

    @Override
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) {

        runAndQueueChangeEvents(action, getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
    }

    @Override
    public void runAndQueueChangeEvents(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor) {

        if (changeListeners.isEmpty() && modificationStatusChangeListeners.isEmpty()) {
            try {
                getWorkspace().run(action, rule, flags, monitor);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                throw new CoreRuntimeException(e);
            }
            return;
        }
        List<ContentsChangeListener> listeners = new ArrayList<ContentsChangeListener>(changeListeners);
        final Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents = new HashMap<IIpsSrcFile, ContentChangeEvent>();
        ContentsChangeListener batchListener = new ContentsChangeListenerImplementation(changedSrcFileEvents);
        changeListeners.clear();
        addChangeListener(batchListener);

        HashSet<IModificationStatusChangeListener> copyOfCurrentModifyListeners = new HashSet<IModificationStatusChangeListener>(
                modificationStatusChangeListeners);
        final Set<IIpsSrcFile> modifiedSrcFiles = new LinkedHashSet<IIpsSrcFile>(0);
        IModificationStatusChangeListener batchModifiyListener = new IModificationStatusChangeListener() {

            @Override
            public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
                modifiedSrcFiles.add(event.getIpsSrcFile());
            }

        };
        modificationStatusChangeListeners.clear();
        addModifcationStatusChangeListener(batchModifiyListener);

        try {
            runSafe(action, rule, flags, monitor, modifiedSrcFiles);
        } finally {
            // restore change listeners
            removeChangeListener(batchListener);
            changeListeners = new CopyOnWriteArraySet<ContentsChangeListener>(listeners);

            // notify about changes
            for (IIpsSrcFile file : changedSrcFileEvents.keySet()) {
                notifyChangeListeners(changedSrcFileEvents.get(file));
            }

            removeModificationStatusChangeListener(batchModifiyListener);
            modificationStatusChangeListeners = copyOfCurrentModifyListeners;
            for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
                ModificationStatusChangedEvent event = new ModificationStatusChangedEvent(ipsSrcFile);
                notifyModificationStatusChangeListener(event);
            }
        }

    }

    protected void runSafe(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor,
            final Set<IIpsSrcFile> modifiedSrcFiles) {
        try {
            getWorkspace().run(action, rule, flags, monitor);
        } catch (CoreException e) {
            for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
                ipsSrcFile.discardChanges();
            }
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (CoreRuntimeException e) {
            for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
                ipsSrcFile.discardChanges();
            }
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    @Override
    public IIpsProject createIpsProject(IJavaProject javaProject) throws CoreException {
        if (javaProject.getProject().getNature(IIpsProject.NATURE_ID) != null) {
            return getIpsProject(javaProject.getProject());
        }
        IIpsProject ipsProject = getIpsProject(javaProject.getProject());
        Util.addNature(javaProject.getProject(), IIpsProject.NATURE_ID);

        IIpsArtefactBuilderSetInfo[] infos = getIpsArtefactBuilderSetInfos();
        if (infos.length > 0) {
            IIpsProjectProperties props = ipsProject.getProperties();
            props.setBuilderSetId(infos[0].getBuilderSetId());
            ipsProject.setProperties(props);
        }

        return ipsProject;
    }

    @Override
    public IIpsProject[] getIpsProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IIpsProject[] ipsProjects = new IIpsProject[projects.length];
        int counter = 0;
        for (IProject project : projects) {
            if (project.isOpen() && hasIpsNature(project)) {
                ipsProjects[counter] = getIpsProject(project.getName());
                counter++;
            }
        }
        if (counter == ipsProjects.length) {
            return ipsProjects;
        }
        IIpsProject[] shrinked = new IIpsProject[counter];
        System.arraycopy(ipsProjects, 0, shrinked, 0, shrinked.length);
        return shrinked;
    }

    private boolean hasIpsNature(IProject project) {
        try {
            return project.hasNature(IIpsProject.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public IIpsProject[] getIpsModelProjects() throws CoreException {
        IIpsProject[] allIpsProjects = getIpsProjects();
        List<IIpsProject> modelProjects = new ArrayList<IIpsProject>(allIpsProjects.length);
        for (IIpsProject ipsProject : allIpsProjects) {
            if (ipsProject.isModelProject()) {
                modelProjects.add(ipsProject);
            }
        }
        return modelProjects.toArray(new IIpsProject[modelProjects.size()]);
    }

    @Override
    public IIpsProject[] getIpsProductDefinitionProjects() throws CoreException {
        IIpsProject[] allIpsProjects = getIpsProjects();
        List<IIpsProject> productDefinitionProjects = new ArrayList<IIpsProject>(allIpsProjects.length);
        for (IIpsProject ipsProject : allIpsProjects) {
            if (ipsProject.isProductDefinitionProject()) {
                productDefinitionProjects.add(ipsProject);
            }
        }
        return productDefinitionProjects.toArray(new IIpsProject[productDefinitionProjects.size()]);
    }

    @Override
    public IResource[] getNonIpsProjects() throws CoreException {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IProject[] nonIpsProjects = new IProject[projects.length];
        int counter = 0;
        for (int i = 0; i < projects.length; i++) {
            if (!projects[i].isOpen() || !projects[i].hasNature(IIpsProject.NATURE_ID)) {
                nonIpsProjects[counter] = projects[i];
                counter++;
            }
        }
        if (counter == nonIpsProjects.length) {
            return nonIpsProjects;
        }
        IProject[] shrinked = new IProject[counter];
        System.arraycopy(nonIpsProjects, 0, shrinked, 0, shrinked.length);
        return shrinked;
    }

    @Override
    public IIpsModel getIpsModel() {
        return this;
    }

    @Override
    public String getNextPartId(IIpsObjectPartContainer parentPart) {
        return UUID.randomUUID().toString();
    }

    @Override
    public IIpsProject getIpsProject(String name) {
        IpsProject ipsProject = projectMap.get(name);
        if (ipsProject == null) {
            ipsProject = new IpsProject(this, name);
            projectMap.put(name, ipsProject);
        }
        return ipsProject;
    }

    @Override
    public IIpsProject getIpsProject(IProject project) {
        return getIpsProject(project.getName());
    }

    /**
     * Returns the workspace root. Overridden method.
     */
    @Override
    public IResource getCorrespondingResource() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    @Override
    public boolean exists() {
        return getCorrespondingResource() != null && getCorrespondingResource().exists();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsProjects();
    }

    @Override
    public IIpsElement getIpsElement(IResource resource) {
        ArgumentCheck.notNull(resource);
        if (resource.getType() == IResource.ROOT) {
            return this;
        }
        if (resource.getType() == IResource.PROJECT) {
            return getIpsProject(resource.getName());
        }
        IIpsProject ipsProject = getIpsProject(resource.getProject().getName());
        String[] segments = resource.getProjectRelativePath().segments();
        IIpsPackageFragmentRoot root = ipsProject.findIpsPackageFragmentRoot(segments[0]);
        if (root == null) {
            return getExternalIpsSrcFile(resource);
        }
        if (segments.length == 1) {
            return root;
        }
        StringBuffer folderName = new StringBuffer();
        for (int i = 1; i < segments.length - 1; i++) {
            if (i > 1) {
                folderName.append(IIpsPackageFragment.SEPARATOR);
            }
            folderName.append(segments[i]);
        }
        if (resource.getType() == IResource.FOLDER) {
            if (segments.length > 2) {
                folderName.append(IIpsPackageFragment.SEPARATOR);
            }
            folderName.append(resource.getName());
            return root.getIpsPackageFragment(folderName.toString());
        }
        IIpsPackageFragment ipsFolder = root.getIpsPackageFragment(folderName.toString());
        if (ipsFolder == null) {
            return null;
        }

        return ipsFolder.getIpsSrcFile(resource.getName());
    }

    /**
     * Returns an IpsElement of an IPS project that is not categorized as such (For example the
     * parent folder of an IPS project).
     * 
     * @param resource the input file
     * @return the respective IPS file or <code>null</code> if the resource isn't an IPS SRC File
     */
    private IIpsElement getExternalIpsSrcFile(IResource resource) {
        if (resource.getType() == IResource.FILE && resource.exists()
                && getIpsObjectTypeByFileExtension(resource.getFileExtension()) != null) {
            return new IpsSrcFileOffRoot((IFile)resource);
        }
        return null;
    }

    @Override
    public IIpsElement findIpsElement(IResource resource) {
        if (resource == null) {
            return null;
        }
        IIpsElement element = getIpsElement(resource);
        if (element != null && element.exists()) {
            return element;
        }
        return null;
    }

    /**
     * Tells the model to stop broadcasting any changes made to ips objects by the current thread.
     * By default changes are broadcasted until this method is called. To restart brodcasting
     * changes the method resumeBroadcastingChangesMadeByCurrentThread() has to be called.
     * <p>
     * <strong>Note<strong> that these two method have a "nested transaction behavior". That means
     * broadcasting resumes only if the resume method has been called as many times as the stop
     * method. This allows to implement method that stop/resume broadcasting to call other method
     * that use these methods without resuming broadcasting to early.
     */
    public void stopBroadcastingChangesMadeByCurrentThread() {
        Integer level = listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level == null) {
            level = Integer.valueOf(1);
        } else {
            level = Integer.valueOf(level.intValue() + 1);
        }
        listenerNoticicationLevelMap.put(Thread.currentThread(), level);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.stopBroadcastingChangesMadeByCurrentThread(): Thread=" //$NON-NLS-1$
                    + Thread.currentThread() + ", new level=" + level); //$NON-NLS-1$
        }
    }

    /**
     * Tells the model to resume broadcasting any changes made to ips objects by the current thread.
     * <p>
     * <strong>Note<strong> that these two method have a "nested transaction behavior". That means
     * broadcasting resumes only if the resume method has been called as many times as the stop
     * method. This allows to implement method that stop/resume broadcasting to call other method
     * that use these methods without resuming broadcasting to early.
     */
    public void resumeBroadcastingChangesMadeByCurrentThread() {
        Integer level = listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level != null && level.intValue() > 0) {
            level = Integer.valueOf(level.intValue() - 1);
        }
        listenerNoticicationLevelMap.put(Thread.currentThread(), level);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.restartBroadcastingChangesMadeByCurrentThread(): Thread=" //$NON-NLS-1$
                    + Thread.currentThread() + ", new level=" + level); //$NON-NLS-1$
        }
    }

    /**
     * Returns <code>true</code> if the model is currently broadcasting changes made to an ips
     * object by the current thread.
     */
    public boolean isBroadcastingChangesForCurrentThread() {
        Integer level = listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level == null || level.intValue() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void addModifcationStatusChangeListener(IModificationStatusChangeListener listener) {
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.addModificationStatusChangeListener(): " + listener); //$NON-NLS-1$
        }
        if (modificationStatusChangeListeners == null) {
            modificationStatusChangeListeners = new HashSet<IModificationStatusChangeListener>(1);
        }
        modificationStatusChangeListeners.add(listener);
    }

    @Override
    public void removeModificationStatusChangeListener(IModificationStatusChangeListener listener) {
        if (modificationStatusChangeListeners != null) {
            boolean wasRemoved = modificationStatusChangeListeners.remove(listener);
            if (TRACE_MODEL_CHANGE_LISTENERS) {
                System.out.println("IpsModel.removeModificationStatusChangeListener(): " + listener + ", was removed=" //$NON-NLS-1$ //$NON-NLS-2$
                        + wasRemoved);
            }
        }
    }

    public void notifyModificationStatusChangeListener(final ModificationStatusChangedEvent event) {
        if (modificationStatusChangeListeners.size() == 0 || !isBroadcastingChangesForCurrentThread()) {
            return;
        }
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.notifyModificationStatusChangeListener(): "//$NON-NLS-1$
                    + modificationStatusChangeListeners.size() + " listeners"); //$NON-NLS-1$
        }
        Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
        display.syncExec(new RunnableModificationStatusChangeListenerImplementation(event));
    }

    @Override
    public void addChangeListener(ContentsChangeListener listener) {
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.addChangeListeners(): " + listener); //$NON-NLS-1$
        }
        changeListeners.add(listener);
    }

    @Override
    public void removeChangeListener(ContentsChangeListener listener) {
        boolean wasRemoved = changeListeners.remove(listener);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.removeChangeListeners(): " + listener + ", was removed=" + wasRemoved); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void notifyChangeListeners(final ContentChangeEvent event) {
        if (!isBroadcastingChangesForCurrentThread()) {
            return;
        }
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.notfiyChangeListeners(): " + changeListeners.size() + " listeners"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        final Runnable notifier = new RunnableChangeListenerImplementation(event);
        if (PlatformUI.isWorkbenchRunning() && Display.getCurrent() == null) {
            // only run notify in async display thread if we are not already in display thread.
            PlatformUI.getWorkbench().getDisplay().asyncExec(notifier);
        } else {
            notifier.run();
        }
    }

    @Override
    public boolean addIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener) {
        return ipsSrcFilesChangeListeners.add(listener);
    }

    @Override
    public boolean removeIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener) {
        return ipsSrcFilesChangeListeners.remove(listener);
    }

    private void notifyIpsSrcFileChangedListeners(final Map<IIpsSrcFile, IResourceDelta> changedIpsSrcFiles) {
        for (IIpsSrcFilesChangeListener listener : ipsSrcFilesChangeListeners) {
            listener.ipsSrcFilesChanged(new IpsSrcFilesChangedEvent(changedIpsSrcFiles));
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IIpsModel;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "IpsModel"; //$NON-NLS-1$
    }

    @Override
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException {
        List<IIpsPackageFragmentRoot> result = new ArrayList<IIpsPackageFragmentRoot>();
        IIpsProject[] projects = getIpsProjects();
        for (IIpsProject project : projects) {
            ((IpsProject)project).getSourceIpsFragmentRoots(result);
        }
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    /**
     * Returns the value datatype identified by the given qualified name or null, if the ips project
     * does not contain such a datatype.
     */
    public ValueDatatype getValueDatatypeDefinedInProjectProperties(IIpsProject ipsProject, String qName) {
        Datatype datatype = getDatatypeDefinedInProjectProperties(ipsProject, qName);
        if (datatype != null && datatype.isValueDatatype()) {
            return (ValueDatatype)datatype;
        }
        return null;
    }

    /**
     * Returns the datatype identified by the given qualified name or null, if the ips project does
     * not contain such a datatype.
     */
    public Datatype getDatatypeDefinedInProjectProperties(IIpsProject ipsProject, String qName) {
        Map<String, Datatype> map = getDatatypesDefinedInProjectProperties(ipsProject);
        return map.get(qName);
    }

    /**
     * Adds the datatypes defined in the IPS project properties to the set of datatypes.
     */
    public void getDatatypesDefinedInProjectProperties(IIpsProject ipsProject,
            boolean valuetypesOnly,
            boolean includePrimitives,
            Set<Datatype> datatypes) {
        Map<String, Datatype> map = getDatatypesDefinedInProjectProperties(ipsProject);
        for (Datatype datatype : map.values()) {
            if (!valuetypesOnly || datatype.isValueDatatype()) {
                if (includePrimitives || !datatype.isPrimitive()) {
                    datatypes.add(datatype);
                }
            }
        }
        return;
    }

    public Map<String, Datatype> getDatatypesDefinedInProjectProperties(IIpsProject ipsProject) {
        reinitIpsProjectPropertiesIfNecessary((IpsProject)ipsProject);
        Map<String, Datatype> map = getIpsProjectData(ipsProject).getProjectDatatypesMap();
        if (map.isEmpty()) {
            initDatatypesDefinedInProjectProperties(ipsProject);
            map = getIpsProjectData(ipsProject).getProjectDatatypesMap();
        }
        return map;
    }

    /**
     * Intializes the datatypes and their helpers for the project.
     */
    private void initDatatypesDefinedInProjectProperties(IIpsProject project) {
        if (datatypes == null) {
            initDatatypesDefinedViaExtension();
        }
        IpsProjectData ipsProjectData = getIpsProjectData(project);
        LinkedHashMap<String, Datatype> projectTypes = ipsProjectData.getProjectDatatypesMap();

        IIpsProjectProperties props = getIpsProjectProperties((IpsProject)project);
        String[] datatypeIds = props.getPredefinedDatatypesUsed();
        for (String datatypeId : datatypeIds) {
            Datatype datatype = datatypes.get(datatypeId);
            if (datatype == null) {
                continue;
            }
            projectTypes.put(datatypeId, datatype);
        }
        List<Datatype> definedDatatypes = props.getDefinedDatatypes();
        for (Datatype datatype : definedDatatypes) {
            projectTypes.put(datatype.getQualifiedName(), datatype);
        }
    }

    /**
     * Returns the IIpsArtefactBuilderSet that is set for the provided IIpsProject by means of the
     * project's builder set id. If no builder set is set for the project an EmptyBuilderSet will be
     * returned. If the builder set for the current builder set id is not found in the set of
     * registered builder sets a warning is logged and an EmptyBuilderSet will be returned.
     */
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet(IIpsProject project, boolean reinit) {
        ArgumentCheck.notNull(project, this);
        reinitIpsProjectPropertiesIfNecessary((IpsProject)project);
        IIpsArtefactBuilderSet builderSet = getIpsProjectData(project).getIpsArtefactBuilderSet();
        if (builderSet == null) {
            return registerBuilderSet(project);
        }

        IIpsProjectProperties data = getIpsProjectProperties((IpsProject)project);
        if (!builderSet.getId().equals(data.getBuilderSetId())) {
            return registerBuilderSet(project);
        }

        if (reinit) {
            initBuilderSet(builderSet, project, data);
        }
        return builderSet;
    }

    private IIpsArtefactBuilderSet registerBuilderSet(IIpsProject project) {
        IIpsProjectProperties data = getIpsProjectProperties((IpsProject)project);
        IIpsArtefactBuilderSet builderSet = createIpsArtefactBuilderSet(data.getBuilderSetId(), project);
        if (builderSet == null || !initBuilderSet(builderSet, project, data)) {
            EmptyBuilderSet emptyBuilderSet = new EmptyBuilderSet();
            try {
                emptyBuilderSet.initialize(new IpsArtefactBuilderSetConfig(new HashMap<String, Object>()));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return emptyBuilderSet;
        }
        getIpsProjectData(project).setIpsArtefactBuilderSet(builderSet);
        return builderSet;
    }

    /**
     * @return true if the initialization was successful
     */
    private boolean initBuilderSet(IIpsArtefactBuilderSet builderSet,
            IIpsProject ipsProject,
            IIpsProjectProperties properties) {
        try {
            IIpsArtefactBuilderSetInfo builderSetInfo = getIpsArtefactBuilderSetInfo(builderSet.getId());
            if (builderSetInfo == null) {
                IpsPlugin.log(new IpsStatus("There is no builder set info registered with the id: " //$NON-NLS-1$
                        + builderSet.getId()));
                return false;
            }
            IIpsArtefactBuilderSetConfig builderSetConfig = properties.getBuilderSetConfig().create(ipsProject,
                    builderSetInfo);
            builderSet.initialize(builderSetConfig);
            return true;
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("An exception occurred while trying to initialize" //$NON-NLS-1$
                    + " the artefact builder set: " //$NON-NLS-1$
                    + builderSet.getId(), e));
            return false;
        }
    }

    /**
     * Returns the <code>DependencyGraph</code> of the provided <code>IpsProject</code>. If the
     * provided IpsProject doesn't exist or if it isn't a valid <code>IpsProject</code>
     * <code>null</code> will be returned by this method. This method is not part of the published
     * interface.
     * 
     * @throws NullPointerException if the argument is null
     */
    // TODO the resource change listener method of this IpsModel needs to update
    // the dependencyGraphForProjectsMap
    public DependencyGraph getDependencyGraph(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, this);
        DependencyGraph graph = getIpsProjectData(ipsProject).getDependencyGraph();
        if (graph == null) {
            graph = IpsPlugin.getDefault().getDependencyGraphPersistenceManager().getDependencyGraph(ipsProject);
            if (graph != null) {
                getIpsProjectData(ipsProject).setDependencyGraph(graph);
                return graph;
            }
            if (ipsProject.exists()) {
                graph = new DependencyGraph(ipsProject);
                getIpsProjectData(ipsProject).setDependencyGraph(graph);
            }
            return graph;
        }

        return graph;
    }

    /**
     * Returns the dependency graph objects that are currently hold by this model. This method
     * doesn't guarantee to return the dependency graph objects for all IpsProjects within the
     * workspace but only for those whom have already been instantiated.
     * <p>
     * This method is not part of the published interface.
     */
    public IDependencyGraph[] getCachedDependencyGraphs() {
        ArrayList<DependencyGraph> graphs = new ArrayList<DependencyGraph>();
        for (IpsProjectData projectData : ipsProjectDatas.values()) {
            if (projectData.getDependencyGraph() != null) {
                graphs.add(projectData.getDependencyGraph());
            }
        }
        return graphs.toArray(new DependencyGraph[graphs.size()]);
    }

    /**
     * Returns the properties (stored in the .ipsproject file) for the given IPS project. If an
     * error occurs while accessing the .ipsproject file or the file does not exist an error is
     * logged and an empty IPS project data instance is returned.
     */
    public IpsProjectProperties getIpsProjectProperties(IpsProject ipsProject) {
        IFile propertyFile = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties properties = getIpsProjectData(ipsProject).getProjectProperties();
        if (properties != null
                && propertyFile.getModificationStamp() != properties.getLastPersistentModificationTimestamp()) {
            clearProjectSpecificCaches(ipsProject);
            properties = null;
        }
        if (properties == null) {
            properties = readProjectProperties(ipsProject);
            getIpsProjectData(ipsProject).setProjectProperties(properties);
        }
        return properties;
    }

    private void reinitIpsProjectPropertiesIfNecessary(IpsProject ipsProject) {
        getIpsProjectProperties(ipsProject);
    }

    /**
     * Clears caches for a given project. Affects only caches whose objects depend on project
     * settings, e.g. IPS project properties or manifest files.
     * 
     * @param ipsProject whose properties and or settings changed
     */
    public void clearProjectSpecificCaches(IIpsProject ipsProject) {
        ipsProjectDatas.remove(ipsProject);
        ipsProject.clearCaches();
    }

    /**
     * Reads the project's data from the .ipsproject file.
     */
    private IpsProjectProperties readProjectProperties(IpsProject ipsProject) {
        IFile file = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties properties = new IpsProjectProperties(ipsProject);
        properties.setCreatedFromParsableFileContents(false);
        if (!file.exists()) {
            return properties;
        }
        Document doc;
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (CoreException e1) {
            IpsPlugin.log(new IpsStatus("Error reading project file contents " //$NON-NLS-1$
                    + file, e1));
            return properties;
        }
        try {
            doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
        } catch (SAXException e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e)); //$NON-NLS-1$
            return properties;
        } catch (IOException e) {
            IpsPlugin.log(new IpsStatus("Error accessing project file " + file, e)); //$NON-NLS-1$
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                IpsPlugin.log(new IpsStatus("Error closing input stream after reading project file " //$NON-NLS-1$
                        + file, e));
                return properties;
            }
        }
        try {
            properties = IpsProjectProperties.createFromXml(ipsProject, doc.getDocumentElement());
            properties.setCreatedFromParsableFileContents(true);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error creating properties from xml, file:  " //$NON-NLS-1$
                    + file, e));
            properties.setCreatedFromParsableFileContents(false);
        }
        // CSON: IllegalCatch
        properties.setLastPersistentModificationTimestamp(file.getModificationStamp());
        return properties;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_REFRESH) {
            if (event.getResource() == null || event.getResource() instanceof IProject) {
                forceReloadOfCachedIpsSrcFileContents((IProject)event.getResource());
            }
            return;
        } else {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
                try {
                    delta.accept(resourceDeltaVisitor);
                    IpsSrcFileChangeVisitor visitor = new IpsSrcFileChangeVisitor();
                    delta.accept(visitor);
                    if (!visitor.changedIpsSrcFiles.isEmpty()) {
                        notifyIpsSrcFileChangedListeners(visitor.changedIpsSrcFiles);
                    }
                    // CSOFF: IllegalCatch
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus("Error updating model objects in resurce changed event.", //$NON-NLS-1$
                            e));
                }
                // CSON: IllegalCatch
            }
        }
    }

    /**
     * Forces to reload the the cached IPS source file contents of a single project or the whole
     * workspace. This is done by setting {@value #INVALID_MOD_STAMP} as modification stamp in each
     * content object.
     * 
     * @param project The project that should considered or <code>null</code> if the whole workspace
     *            should be considered.
     */
    private synchronized void forceReloadOfCachedIpsSrcFileContents(IProject project) {
        HashSet<IIpsSrcFile> copyKeys = new HashSet<IIpsSrcFile>(ipsObjectsMap.keySet());
        for (IIpsSrcFile srcFile : copyKeys) {
            if (!srcFile.isDirty() && (project == null || srcFile.getIpsProject().getProject().equals(project))) {
                releaseInCache(srcFile);
            }
        }
    }

    /**
     * Releases a cached {@link IIpsSrcFile} from the ipsObjectMap cache.
     * <p>
     * Do not really remove the IpsSrcFileContent from ipsObjectMap because we want to have the same
     * IPS object (same object reference) after reloading the file. The object identity should never
     * change until we have no other references remaining. Instead of removing the file we simply
     * set the modification stamp invalid and force a reload on next access.
     * <p>
     * Only alternative would be to use a soft reference cache.
     * 
     * @param srcFile The {@link IIpsSrcFile} you want to release from the cache.
     */
    private void releaseInCache(IIpsSrcFile srcFile) {
        IpsSrcFileContent contents = ipsObjectsMap.get(srcFile);
        contents.setModificationStamp(INVALID_MOD_STAMP);
    }

    /**
     * Creates an IIpsArtefactBuilderSet if one has been registered with the provided
     * <code>builderSetId</code> at the artefact builder set extension point. Otherwise an
     * <code>EmptyBuilderSet</code> will be returned.
     */
    private IIpsArtefactBuilderSet createIpsArtefactBuilderSet(String builderSetId, IIpsProject ipsProject) {
        ArgumentCheck.notNull(builderSetId);
        ArgumentCheck.notNull(ipsProject);

        IIpsArtefactBuilderSetInfo[] infos = getIpsArtefactBuilderSetInfos();
        for (IIpsArtefactBuilderSetInfo info : infos) {
            if (info.getBuilderSetId().equals(builderSetId)) {
                IIpsArtefactBuilderSet builderSet = info.create(ipsProject);
                return builderSet;
            }
        }
        return null;
    }

    /**
     * This method is for test purposes only. Usually the builder set infos are loaded via the
     * according extension point.
     */
    public void setIpsArtefactBuilderSetInfos(IIpsArtefactBuilderSetInfo[] builderSetInfos) {
        builderSetInfoList = new ArrayList<IIpsArtefactBuilderSetInfo>(Arrays.asList(builderSetInfos));
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Since 3.10 the scope of an extension property could be limited to an instance of
     *             {@link IIpsObjectPartContainer}. Hence we need the instance to decide whether a
     *             extension property is applicable or not. Use
     *             {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     *             <p>
     *             If you are interested in all extension properties regardless of their
     *             applicability to specific objects, use
     *             {@link #getExtensionPropertyDefinitionsForClass(Class, boolean)} explicitly.
     */
    @Override
    @Deprecated
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        Set<IExtensionPropertyDefinition> props = getExtensionPropertyDefinitionsForClass(type,
                includeSupertypesAndInterfaces);
        return props.toArray(new IExtensionPropertyDefinition[props.size()]);
    }

    @Override
    public Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitionsForClass(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        return customModelExtensions.getExtensionPropertyDefinitions(type, includeSupertypesAndInterfaces);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Since 3.10 the scope of an extension property could be limited to an instance of
     *             {@link IIpsObjectPartContainer}. Hence we need the instance to decide whether a
     *             extension property is applicable or not. Use
     *             {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     */
    @Override
    @Deprecated
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {
        return customModelExtensions.getExtensionPropertyDefinition(type, propertyId, includeSupertypesAndInterfaces);
    }

    @Override
    public Map<String, IExtensionPropertyDefinition> getExtensionPropertyDefinitions(IIpsObjectPartContainer object) {
        return customModelExtensions.getExtensionPropertyDefinitions(object);
    }

    /**
     * Adds the extension property. For testing purposes. During normal execution the available
     * extension properties are discovered by extension point lookup.
     */
    public void addIpsObjectExtensionProperty(IExtensionPropertyDefinition property) {
        customModelExtensions.addIpsObjectExtensionProperty(property);
    }

    private void initDatatypesDefinedViaExtension() {
        datatypes = new HashMap<String, Datatype>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "datatypeDefinition"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        // first, get all datatypes defined by the ips-plugin itself
        // to get them at top of the list...
        for (IExtension extension : extensions) {
            if (extension.getNamespaceIdentifier().equals(IpsPlugin.PLUGIN_ID)) {
                createDatatypeDefinition(extension);
            }
        }

        // and second, get the rest.
        for (int i = 0; i < extensions.length; i++) {
            if (!extensions[i].getNamespaceIdentifier().equals(IpsPlugin.PLUGIN_ID)) {
                createDatatypeDefinition(extensions[i]);
            }
        }
    }

    private void createDatatypeDefinition(IExtension extension) {
        for (IConfigurationElement configElement : extension.getConfigurationElements()) {
            DatatypeDefinition definition = new DatatypeDefinition(extension, configElement);
            if (definition.hasDatatype()) {
                datatypes.put(definition.getDatatype().getQualifiedName(), definition.getDatatype());
            }
        }
    }

    @Override
    public ValueDatatype[] getPredefinedValueDatatypes() {
        if (datatypes == null) {
            initDatatypesDefinedViaExtension();
        }
        Collection<Datatype> c = datatypes.values();
        return c.toArray(new ValueDatatype[c.size()]);
    }

    @Override
    public boolean isPredefinedValueDatatype(String valueDatatypeId) {
        if (datatypes == null) {
            initDatatypesDefinedViaExtension();
        }
        return datatypes.containsKey(valueDatatypeId);
    }

    @Override
    public void delete(IIpsElement toDelete) {
        if (toDelete instanceof IIpsObjectPart) {
            ((IIpsObjectPart)toDelete).delete();
        }
    }

    @Override
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention(String id) {
        initChangesOverTimeNamingConventionIfNecessary();
        IChangesOverTimeNamingConvention convention = changesOverTimeNamingConventionMap.get(id);
        if (convention != null) {
            return convention;
        }
        convention = changesOverTimeNamingConventionMap.get(IChangesOverTimeNamingConvention.VAA);
        if (convention != null) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING,
                    "Unknown changes in time naming convention " + id //$NON-NLS-1$
                            + ". Using default " //$NON-NLS-1$
                            + IChangesOverTimeNamingConvention.VAA,
                    null));
            return convention;
        }
        IpsPlugin.log(new IpsStatus("Unknown changes in time naming convention " + id //$NON-NLS-1$
                + ". Default convention " //$NON-NLS-1$
                + IChangesOverTimeNamingConvention.VAA + " not found!")); //$NON-NLS-1$
        return new ChangesOverTimeNamingConvention("VAA"); //$NON-NLS-1$
    }

    @Override
    public IChangesOverTimeNamingConvention[] getChangesOverTimeNamingConvention() {
        initChangesOverTimeNamingConventionIfNecessary();
        IChangesOverTimeNamingConvention[] conventions = new IChangesOverTimeNamingConvention[changesOverTimeNamingConventionMap
                .size()];
        int i = 0;
        for (Iterator<IChangesOverTimeNamingConvention> it = changesOverTimeNamingConventionMap.values().iterator(); it
                .hasNext();) {
            conventions[i++] = it.next();
        }
        return conventions;
    }

    private void initChangesOverTimeNamingConventionIfNecessary() {
        if (changesOverTimeNamingConventionMap == null) {
            changesOverTimeNamingConventionMap = new HashMap<String, IChangesOverTimeNamingConvention>();
            IChangesOverTimeNamingConvention fips = new ChangesOverTimeNamingConvention(
                    IChangesOverTimeNamingConvention.FAKTOR_IPS);
            changesOverTimeNamingConventionMap.put(fips.getId(), fips);

            IChangesOverTimeNamingConvention vaa = new ChangesOverTimeNamingConvention(
                    IChangesOverTimeNamingConvention.VAA);
            changesOverTimeNamingConventionMap.put(vaa.getId(), vaa);

            IChangesOverTimeNamingConvention pm = new ChangesOverTimeNamingConvention(
                    IChangesOverTimeNamingConvention.PM);
            changesOverTimeNamingConventionMap.put(pm.getId(), pm);
        }
    }

    /**
     * Returns the ClassLoaderProvider for the given ips project. Uses the System class loader as
     * parent of the class loader that is provided by the returned provider.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     * 
     * @see ClassLoader#getSystemClassLoader()
     */
    public ClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        ClassLoaderProvider provider = getIpsProjectData(ipsProject).getClassLoaderProvider();
        if (provider == null) {
            // create a new classloader provider, make sure that the jars (inside the provided
            // classloader) will be copied, this fixed the problem if the classloader is used
            // to load classes for DynamicValueDatatype
            provider = new ClassLoaderProvider(ipsProject.getJavaProject(), ClassLoader.getSystemClassLoader());
            getIpsProjectData(ipsProject).setClassLoaderProvider(provider);
        }
        return provider;
    }

    /**
     * Returns the cache for all function resolvers (registered via extension point) for the given
     * IPS project.
     * 
     * @param ipsProject the project to return a cache for
     */
    @Override
    public ExtensionFunctionResolversCache getExtensionFunctionResolverCache(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        ExtensionFunctionResolversCache functionResolversCache = getIpsProjectData(ipsProject).getFunctionResolver();
        if (functionResolversCache == null) {
            functionResolversCache = new ExtensionFunctionResolversCache(ipsProject);
            getIpsProjectData(ipsProject).setFunctionResolver(functionResolversCache);
        }
        return functionResolversCache;
    }

    /**
     * Returns the cache for the validation result.
     */
    public ValidationResultCache getValidationResultCache() {
        return validationResultCache;
    }

    /**
     * Removes the content for the given IpsSrcFile.
     */
    public synchronized void removeIpsSrcFileContent(IIpsSrcFile file) {
        if (file != null) {
            ipsObjectsMap.remove(file);
        }
    }

    /**
     * Returns true if the IIpsSrcFileContents of the provided IIpsSrcFile has been cached.
     */
    public boolean isCached(IIpsSrcFile file) {
        return ipsObjectsMap.get(file) != null;
    }

    /**
     * Returns the content for the given IPS source file. If the IPS source file's corresponding
     * resource does not exist, the method returns <code>null</code>. If loadCompleteContent is
     * <code>true</code> then the complete content will be read from the source file, if
     * <code>false</code> then only the properties of the IPS object will be read.
     * 
     * @param file the file to read
     * 
     * @param loadCompleteContent <code>true</code> if the completely file should be read,
     *            <code>false</code> if only the properties will be read
     */
    public synchronized IpsSrcFileContent getIpsSrcFileContent(IIpsSrcFile file, boolean loadCompleteContent) {
        if (file == null || !file.exists()) {
            return null;
        }
        IpsSrcFileContent content = ipsObjectsMap.get(file);

        // new content
        if (content == null) {
            content = readContentFromFile(file, loadCompleteContent);
            ipsObjectsMap.put(file, content);
            return content;
        }

        IResource enclResource = file.getEnclosingResource();
        if (enclResource == null) {
            return content;
        }

        long resourceModStamp = enclResource.getModificationStamp();
        // existing, synchronized content
        if (content.getModificationStamp() == resourceModStamp) {
            return checkSynchronizedContent(content, loadCompleteContent);
        }

        // existing, but unsynchronized content
        if (loadCompleteContent) {
            content.initContentFromFile();
        } else {
            content.initRootPropertiesFromFile();
        }
        return content;
    }

    private IpsSrcFileContent readContentFromFile(IIpsSrcFile file, boolean loadCompleteContent) {
        IpsSrcFileContent content = new IpsSrcFileContent((IpsObject)file.getIpsObjectType().newObject(file));

        if (loadCompleteContent) {
            logTraceMessage("New content created", file); //$NON-NLS-1$
            content.initContentFromFile();
        } else {
            logTraceMessage("New properties read", file); //$NON-NLS-1$
            content.initRootPropertiesFromFile();
        }
        return content;
    }

    private IpsSrcFileContent checkSynchronizedContent(IpsSrcFileContent content, boolean loadCompleteContent) {
        if (loadCompleteContent) {
            if (content.isInitialized()) {
                logTraceMessage("Content returned from cache", content.getIpsSrcFile()); //$NON-NLS-1$
                return content;
            } else {
                logTraceMessage("Content initialized", content.getIpsSrcFile()); //$NON-NLS-1$
                content.initContentFromFile();
                return content;
            }
        } else {
            // only properties are needed
            if (content.areRootPropertiesAvailable()) {
                logTraceMessage("Properties returned from cache", content.getIpsSrcFile()); //$NON-NLS-1$
                return content;
            } else {
                logTraceMessage("Properties initialized", content.getIpsSrcFile()); //$NON-NLS-1$
                content.initRootPropertiesFromFile();
                return content;
            }
        }
    }

    public synchronized IpsSrcFileContent getIpsSrcFileContent(IIpsSrcFile file) {
        return getIpsSrcFileContent(file, true);
    }

    public void ipsSrcFileContentHasChanged(ContentChangeEvent event) {
        IIpsSrcFile file = event.getIpsSrcFile();
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.ipsSrcFileHasChanged(), file=" + file //$NON-NLS-1$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
        validationResultCache.removeStaleData(file);
        notifyChangeListeners(event);
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.ipsSrcFileHasChanged(), file=" //$NON-NLS-1$
                    + event.getIpsSrcFile() + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
    }

    @Override
    public void clearValidationCache() {
        getValidationResultCache().clear();
    }

    private void createIpsArtefactBuilderSetInfosIfNecessary() {
        if (builderSetInfoList == null) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            builderSetInfoList = new ArrayList<IIpsArtefactBuilderSetInfo>();
            IpsArtefactBuilderSetInfo.loadExtensions(registry, IpsPlugin.getDefault().getLog(), builderSetInfoList,
                    this);
        }
    }

    /**
     * Returns an array of IpsArtefactBuilderSetInfo objects. Each IpsArtefactBuilderSetInfo object
     * represents an IpsArtefactBuilderSet that is a registered at the corresponding extension
     * point.
     */
    @Override
    public IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos() {
        createIpsArtefactBuilderSetInfosIfNecessary();
        return builderSetInfoList.toArray(new IIpsArtefactBuilderSetInfo[builderSetInfoList.size()]);
    }

    @Override
    public IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id) {
        createIpsArtefactBuilderSetInfosIfNecessary();
        for (Object name2 : builderSetInfoList) {
            IIpsArtefactBuilderSetInfo builderSetInfo = (IIpsArtefactBuilderSetInfo)name2;
            if (builderSetInfo.getBuilderSetId().equals(id)) {
                return builderSetInfo;
            }
        }
        return null;
    }

    @Override
    public IpsObjectType[] getIpsObjectTypes() {
        return ipsObjectTypes;
    }

    @Override
    public IpsObjectType getIpsObjectType(String name) {
        for (IpsObjectType ipsObjectType : ipsObjectTypes) {
            if (ipsObjectType.getId().equals(name)) {
                return ipsObjectType;
            }
        }
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectTypeByFileExtension(String fileExtension) {
        for (IpsObjectType ipsObjectType : ipsObjectTypes) {
            if (ipsObjectType.getFileExtension().equals(fileExtension)) {
                return ipsObjectType;
            }
        }
        return null;
    }

    @Override
    public IVersionProvider<?> getVersionProvider(IIpsProject ipsProject) {
        IVersionProvider<?> verionProvider = getIpsProjectData(ipsProject).getVersionProvider();
        if (verionProvider == null) {
            verionProvider = initVersionProvider(ipsProject);
            getIpsProjectData(ipsProject).setVersionProvider(verionProvider);
        }
        return verionProvider;
    }

    private IVersionProvider<?> initVersionProvider(IIpsProject ipsProject) {
        VersionProviderExtensionPoint versionProviderExtensionPoint = new VersionProviderExtensionPoint(ipsProject);
        IVersionProvider<?> extendedVersionProvider = versionProviderExtensionPoint.getExtendedVersionProvider();
        if (extendedVersionProvider != null) {
            return extendedVersionProvider;
        } else {
            return new DefaultVersionProvider(ipsProject);
        }

    }

    private static void logTraceMessage(String text, IIpsSrcFile ipsSrcFile) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            IResource enclosingResource = ipsSrcFile.getEnclosingResource();
            System.out.println(NLS.bind("IpsModel.getIpsSrcFileContent(): {0}, file={1}, FileModStamp={2}, Thread={3}", //$NON-NLS-1$
                    new String[] { text, "" + ipsSrcFile, "" + enclosingResource.getModificationStamp(), //$NON-NLS-1$ //$NON-NLS-2$
                            Thread.currentThread().getName() }));
        }
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
    }

    /**
     * @deprecated since 3.15: this method is not supported anymore.
     */
    @Override
    @Deprecated
    public List<ITestCase> searchReferencingTestCases(IProductCmpt cmpt) throws CoreException {
        IIpsProject baseProject = cmpt.getIpsProject();
        IIpsProject[] projects = getIpsModel().getIpsProjects();
        List<ITestCase> result = new ArrayList<ITestCase>();

        result.addAll(getReferencingTestCases(baseProject, cmpt.getQualifiedName()));
        for (IIpsProject project : projects) {
            if (project.isReferencing(baseProject)) {
                result.addAll(getReferencingTestCases(project, cmpt.getQualifiedName()));
            }
        }

        return result;
    }

    /**
     * @deprecated since 3.15: this method is not supported anymore.
     */
    @Deprecated
    private List<ITestCase> getReferencingTestCases(IIpsProject project, String objectName) throws CoreException {
        List<ITestCase> result = new ArrayList<ITestCase>();
        List<ITestCase> testCases = project.getAllTestCases();
        for (ITestCase testCase : testCases) {
            String[] references = testCase.getReferencedProductCmpts();
            for (String refName : references) {
                if (refName.equals(objectName)) {
                    result.add(testCase);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public IIpsObjectPathContainer getIpsObjectPathContainer(IIpsProject ipsProject,
            String containerTypeId,
            String optionalPath) {

        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(containerTypeId);
        ArgumentCheck.notNull(optionalPath);

        IpsProjectData data = getIpsProjectData(ipsProject);
        return data.getIpsObjectPathContainer(containerTypeId, optionalPath);
    }

    @Override
    public <T> T executeModificationsWithSingleEvent(SingleEventModification<T> modifications) throws CoreException {
        boolean successful = false;
        IIpsSrcFile ipsSrcFile = modifications.getIpsSrcFile();
        IpsSrcFileContent content = getIpsSrcFileContent(ipsSrcFile);
        try {
            stopBroadcastingChangesMadeByCurrentThread();
            successful = modifications.execute();
        } catch (CoreException e) {
            throw e;
        } finally {
            if (successful) {
                ipsSrcFile.markAsClean();
            }
            resumeBroadcastingChangesMadeByCurrentThread();
            if (successful) {
                if (content != null) {
                    content.ipsObjectChanged(modifications.modificationEvent());
                } else {
                    ipsSrcFileContentHasChanged(modifications.modificationEvent());
                }
            }
        }
        return modifications.getResult();
    }

    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("The IPS Model cannot be deleted."); //$NON-NLS-1$
    }

    /**
     * Returns the {@link IIpsSrcFile ips source files} of the markers that are configured in the
     * given {@link IIpsProject}. This method only handles the caching in the project data. Always
     * call {@link IIpsProject#getMarkerEnums()} directly.
     * 
     * @see IIpsProject#getMarkerEnums()
     */
    public LinkedHashSet<IIpsSrcFile> getMarkerEnums(IpsProject ipsProject) {
        return getIpsProjectData(ipsProject).getMarkerEnums();
    }

    private final class RunnableChangeListenerImplementation implements Runnable {
        private final ContentChangeEvent event;

        private RunnableChangeListenerImplementation(ContentChangeEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            for (ContentsChangeListener listener : changeListeners) {
                if (!event.getIpsSrcFile().exists()) {
                    break;
                }
                try {
                    if (TRACE_MODEL_CHANGE_LISTENERS) {
                        System.out.println("IpsModel.notfiyChangeListeners(): Start notifying listener: "//$NON-NLS-1$
                                + listener);
                    }
                    listener.contentsChanged(event);
                    if (TRACE_MODEL_CHANGE_LISTENERS) {
                        System.out.println("IpsModel.notfiyChangeListeners(): Finished notifying listener: "//$NON-NLS-1$
                                + listener);
                    }
                    // CSOFF: IllegalCatch
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus("Error notifying IPS model change listener", //$NON-NLS-1$
                            e));
                }
                // CSON: IllegalCatch
            }
        }
    }

    private final class RunnableModificationStatusChangeListenerImplementation implements Runnable {
        private final ModificationStatusChangedEvent event;

        private RunnableModificationStatusChangeListenerImplementation(ModificationStatusChangedEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            List<IModificationStatusChangeListener> copy = new CopyOnWriteArrayList<IModificationStatusChangeListener>(
                    modificationStatusChangeListeners);
            for (IModificationStatusChangeListener listener : copy) {
                try {
                    if (TRACE_MODEL_CHANGE_LISTENERS) {
                        System.out.println("IpsModel.notfiyChangeListeners(): Start notifying listener: "//$NON-NLS-1$
                                + listener);
                    }
                    listener.modificationStatusHasChanged(event);
                    if (TRACE_MODEL_CHANGE_LISTENERS) {
                        System.out.println(
                                "IpsModel.notifyModificationStatusChangeListener(): Finished notifying listener: "//$NON-NLS-1$
                                        + listener);
                    }
                    // CSOFF: IllegalCatch
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus("Error notifying IPS model ModificationStatusChangeListeners", //$NON-NLS-1$
                            e));
                }
                // CSON: IllegalCatch
            }
        }
    }

    private final class ContentsChangeListenerImplementation implements ContentsChangeListener {
        private final Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents;

        private ContentsChangeListenerImplementation(Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents) {
            this.changedSrcFileEvents = changedSrcFileEvents;
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            ContentChangeEvent newEvent = null;
            ContentChangeEvent previousEvent = changedSrcFileEvents.get(event.getIpsSrcFile());
            if (previousEvent == null) {
                newEvent = event;
            } else {
                newEvent = ContentChangeEvent.mergeChangeEvents(event, previousEvent);
            }
            changedSrcFileEvents.put(event.getIpsSrcFile(), newEvent);
        }

    }

    private class IpsSrcFileChangeVisitor implements IResourceDeltaVisitor {

        private Map<IIpsSrcFile, IResourceDelta> changedIpsSrcFiles = new HashMap<IIpsSrcFile, IResourceDelta>(5);
        private Set<String> fileExtensionsOfInterest;

        public IpsSrcFileChangeVisitor() {
            fileExtensionsOfInterest = resourceDeltaVisitor.getFileExtensionsOfInterest();
        }

        @Override
        public boolean visit(final IResourceDelta delta) {
            IResource resource = delta.getResource();
            if (resource == null || resource.getType() != IResource.FILE) {
                return true;
            }
            if (fileExtensionsOfInterest.contains(((IFile)resource).getFileExtension())) {
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    IIpsElement ipsElement = getIpsElement(resource);
                    if (ipsElement instanceof IIpsSrcFile && ((IIpsSrcFile)ipsElement).isContainedInIpsRoot()) {
                        changedIpsSrcFiles.put((IIpsSrcFile)ipsElement, delta);
                    }
                } else {
                    final IIpsElement ipsElement = findIpsElement(resource);
                    if (ipsElement instanceof IIpsSrcFile && ((IIpsSrcFile)ipsElement).isContainedInIpsRoot()) {
                        IpsSrcFile srcFile = (IpsSrcFile)ipsElement;
                        changedIpsSrcFiles.put(srcFile, delta);
                    }
                }
            }
            return false;
        }
    }

}

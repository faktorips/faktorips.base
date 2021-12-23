/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static java.util.function.Predicate.not;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaProject;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.ICustomModelExtensions;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IModificationStatusChangeListener;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.builder.DependencyGraph;
import org.faktorips.devtools.model.internal.builder.EmptyBuilderSet;
import org.faktorips.devtools.model.internal.datatype.DatatypeDefinition;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileOffRoot;
import org.faktorips.devtools.model.internal.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.model.internal.ipsproject.ChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.plugin.MultiLanguageSupport;
import org.faktorips.devtools.model.plugin.extensions.CachingSupplier;
import org.faktorips.devtools.model.util.IpsProjectUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of <code>IIpsModel</code>.
 * 
 * @see IIpsModel
 */
public class IpsModel extends IpsElement implements IIpsModel {

    public static final boolean TRACE_MODEL_MANAGEMENT = Boolean
            .valueOf(Platform.getDebugOption("org.faktorips.devtools.model/trace/modelmanagement")).booleanValue(); //$NON-NLS-1$

    public static final boolean TRACE_MODEL_CHANGE_LISTENERS = Boolean
            .valueOf(Platform.getDebugOption("org.faktorips.devtools.model/trace/modelchangelisteners")) //$NON-NLS-1$
            .booleanValue();

    public static final boolean TRACE_VALIDATION = Boolean
            .valueOf(Platform.getDebugOption("org.faktorips.devtools.model/trace/validation")) //$NON-NLS-1$
            .booleanValue();

    /**
     * We must use a value different from {@link IResource#NULL_STAMP} because otherwise files which
     * do not exist in workspace (like {@link LibraryIpsSrcFile}) would remain cached forever.
     * <p>
     * Described in FIPS-5745
     */
    private static final int INVALID_MOD_STAMP = -42;

    private static IpsModel theInstance = Abstractions.isEclipseRunning() ? new EclipseIpsModel() : new IpsModel();

    /** set of model change listeners that are notified about model changes */
    private CopyOnWriteArraySet<ContentsChangeListener> changeListeners = new CopyOnWriteArraySet<>();

    private final Set<IIpsSrcFilesChangeListener> ipsSrcFilesChangeListeners = new CopyOnWriteArraySet<>();

    /** set of modification status change listeners */
    private Set<IModificationStatusChangeListener> modificationStatusChangeListeners = new HashSet<>(
            100);

    /**
     * a map that contains per thread if changes should be broadcasted to the registered listeners
     * or squeezed.
     */
    private Map<Thread, Integer> listenerNoticicationLevelMap = new HashMap<>();

    /**
     * A map containing the datatypes (value) by id (key). The map is initialized with null to point
     * at the lazy loading mechanism
     */
    private Supplier<Map<String, Datatype>> datatypes = CachingSupplier.caching(this::initDatatypesDefinedViaExtension);

    /**
     * A map containing the project for every name.
     */
    private Map<String, IpsProject> projectMap = new ConcurrentHashMap<>();

    private Supplier<List<IIpsArtefactBuilderSetInfo>> builderSetInfoList = CachingSupplier
            .caching(this::createIpsArtefactBuilderSetInfosIfNecessary);

    /** map containing all changes in time naming conventions by id. */
    private Map<String, IChangesOverTimeNamingConvention> changesOverTimeNamingConventionMap = null;

    /** map containing IpsSrcFileContents as values and IpsSrcFiles as keys. */
    private HashMap<IIpsSrcFile, IpsSrcFileContent> ipsObjectsMap = new HashMap<>(1000);

    /** validation result cache */
    private ValidationResultCache validationResultCache = new ValidationResultCache();

    private IpsObjectType[] ipsObjectTypes;

    private final CustomModelExtensions customModelExtensions;

    /**
     * A map containing project data per project.
     */
    private final Map<IIpsProject, IpsProjectData> ipsProjectDatas = new ConcurrentHashMap<>(
            3, 0.9f, 2);

    private IpsObjectPathContainerFactory ipsObjectPathContainerFactory = IpsObjectPathContainerFactory
            .newFactoryBasedOnExtensions();

    private final IMultiLanguageSupport multiLanguageSupport = new MultiLanguageSupport();

    private IpsModel() {
        super(null, "IpsModel"); //$NON-NLS-1$
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.Constructor(): IpsModel created."); //$NON-NLS-1$
        }
        customModelExtensions = new CustomModelExtensions(this);
        initIpsObjectTypes();
    }

    protected Set<IIpsSrcFile> getIpsSrcFilesInternal() {
        return ipsObjectsMap.keySet();
    }

    /**
     * Re-initializes the model (the singleton instance will be reset).
     *
     * @deprecated <strong><em>Should only be called in test cases to ensure a clean
     *             environment.</em></strong>
     */
    @Deprecated
    public static void reInit() {
        if (Abstractions.isEclipseRunning()) {
            ((EclipseIpsModel)theInstance).stopListeningToResourceChanges();
            theInstance = new EclipseIpsModel();
            ((EclipseIpsModel)theInstance).startListeningToResourceChanges();
        } else {
            theInstance = new IpsModel();
        }
    }

    /**
     * Returns the {@link IpsModel} singleton instance.
     *
     * @see IIpsModel#get
     * @deprecated <strong><em>This method should only be called when explicitly depending on
     *             implementation details, otherwise use {@link IIpsModel#get}!</em></strong>
     */
    @Deprecated
    public static final IpsModel get() {
        return theInstance;
    }

    /**
     * Provides access to operations related to multi-language support.
     */
    @Override
    public IMultiLanguageSupport getMultiLanguageSupport() {
        return multiLanguageSupport;
    }

    @Override
    public ICustomModelExtensions getCustomModelExtensions() {
        return customModelExtensions;
    }

    private void initIpsObjectTypes() {
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.initIpsObjectType: start."); //$NON-NLS-1$
        }
        List<IpsObjectType> types = new ArrayList<>();
        types.add(IpsObjectType.POLICY_CMPT_TYPE);
        types.add(IpsObjectType.PRODUCT_CMPT_TYPE);
        types.add(IpsObjectType.PRODUCT_CMPT);
        types.add(IpsObjectType.PRODUCT_TEMPLATE);
        types.add(IpsObjectType.ENUM_TYPE);
        types.add(IpsObjectType.ENUM_CONTENT);
        types.add(IpsObjectType.TABLE_STRUCTURE);
        types.add(IpsObjectType.TABLE_CONTENTS);
        types.add(IpsObjectType.TEST_CASE_TYPE);
        types.add(IpsObjectType.TEST_CASE);
        if (Abstractions.isEclipseRunning()) {
            ExtensionPoints extensionPoints = new ExtensionPoints(IpsModelActivator.PLUGIN_ID);
            IExtension[] extensions = extensionPoints.getExtension(ExtensionPoints.IPS_OBJECT_TYPE);
            for (IExtension extension : extensions) {
                List<IpsObjectType> additionalTypes = createIpsObjectTypes(extension);
                for (IpsObjectType objType : additionalTypes) {
                    addIpsObjectTypeIfNotDuplicate(types, objType);
                }
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
                IpsLog.log(new IpsStatus("Can't register IpsObjectType " + newType //$NON-NLS-1$
                        + " as it has the same file extension as the type " + exisingType)); //$NON-NLS-1$
                return;
            }
        }
        types.add(newType);
    }

    private List<IpsObjectType> createIpsObjectTypes(IExtension extension) {
        IpsObjectType type = null;
        List<IpsObjectType> validTypes = new ArrayList<>();
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (IConfigurationElement configElement : configElements) {
            if (!"ipsobjecttype".equalsIgnoreCase(configElement.getName())) { //$NON-NLS-1$
                String text = "Illegal IPS object type definition" + extension.getUniqueIdentifier() //$NON-NLS-1$
                        + ". Expected Config Element <ipsobjectytpe> was " //$NON-NLS-1$
                        + configElement.getName();
                IpsLog.log(new IpsStatus(text));
                continue;
            }
            type = ExtensionPoints.createExecutableExtension(extension, configElement, "class", //$NON-NLS-1$
                    IpsObjectType.class);

            if (type == null) {
                String text = "Illegal IPS object type definition " + extension.getUniqueIdentifier(); //$NON-NLS-1$
                IpsLog.log(new IpsStatus(text));
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
        return ipsProjectDatas.computeIfAbsent(ipsProject, p -> new IpsProjectData(p, ipsObjectPathContainerFactory));
    }

    @Override
    public void runAndQueueChangeEvents(ICoreRunnable action, IProgressMonitor monitor) {

        if (changeListeners.isEmpty() && modificationStatusChangeListeners.isEmpty()) {
            try {
                getWorkspace().run(action, monitor);
            } catch (CoreRuntimeException e) {
                IpsLog.log(e);
                throw e;
            }
            return;
        }
        List<ContentsChangeListener> listeners = new ArrayList<>(changeListeners);
        final Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents = new HashMap<>();
        ContentsChangeListener batchListener = event -> collect(changedSrcFileEvents, event);
        changeListeners.clear();
        addChangeListener(batchListener);

        HashSet<IModificationStatusChangeListener> copyOfCurrentModifyListeners = new HashSet<>(
                modificationStatusChangeListeners);
        final Set<IIpsSrcFile> modifiedSrcFiles = new LinkedHashSet<>(0);
        IModificationStatusChangeListener batchModifiyListener = event -> modifiedSrcFiles.add(event.getIpsSrcFile());
        modificationStatusChangeListeners.clear();
        addModifcationStatusChangeListener(batchModifiyListener);

        try {
            runSafe(action, monitor, modifiedSrcFiles);
        } finally {
            // restore change listeners
            removeChangeListener(batchListener);
            changeListeners = new CopyOnWriteArraySet<>(listeners);

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

    private void collect(final Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents, ContentChangeEvent event) {
        ContentChangeEvent newEvent = null;
        ContentChangeEvent previousEvent = changedSrcFileEvents.get(event.getIpsSrcFile());
        if (previousEvent == null) {
            newEvent = event;
        } else {
            newEvent = ContentChangeEvent.mergeChangeEvents(event, previousEvent);
        }
        changedSrcFileEvents.put(event.getIpsSrcFile(), newEvent);
    }

    protected void runSafe(ICoreRunnable action,
            IProgressMonitor monitor,
            final Set<IIpsSrcFile> modifiedSrcFiles) {
        try {
            getWorkspace().run(action, monitor);
        } catch (CoreRuntimeException e) {
            for (IIpsSrcFile ipsSrcFile : modifiedSrcFiles) {
                ipsSrcFile.discardChanges();
            }
            IpsLog.logAndShowErrorDialog(e);
        }
    }

    @Override
    public AWorkspace getWorkspace() {
        return Abstractions.getWorkspace();
    }

    @Override
    public IIpsProject createIpsProject(AProject project) throws CoreRuntimeException {
        try {
            if (Abstractions.isEclipseRunning()) {
                IProject eclipseProject = project.unwrap();
                if (eclipseProject.getNature(IIpsProject.NATURE_ID) != null) {
                    return getIpsProject(project);
                }
                IIpsProject ipsProject = getIpsProject(project);
                IpsProjectUtil.addNature(eclipseProject, IIpsProject.NATURE_ID);

                IIpsArtefactBuilderSetInfo[] infos = getIpsArtefactBuilderSetInfos();
                if (infos.length > 0) {
                    IIpsProjectProperties props = ipsProject.getProperties();
                    props.setBuilderSetId(infos[0].getBuilderSetId());
                    ipsProject.setProperties(props);
                }

                return ipsProject;
            } else {
                PlainJavaProject plainJavaProject = (PlainJavaProject)project;
                if (plainJavaProject.isIpsProject()) {
                    return getIpsProject(project);
                }

                IIpsProject ipsProject = getIpsProject(project);
                IIpsProjectProperties props = ipsProject.getProperties();
                ipsProject.setProperties(props);

                return ipsProject;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IIpsProject[] getIpsProjects() {
        return Abstractions.getWorkspace()
                .getRoot().getProjects().stream()
                .filter(AProject::isIpsProject)
                .map(AProject::getName)
                .map(this::getIpsProject)
                .toArray(IIpsProject[]::new);
    }

    @Override
    public IIpsProject[] getIpsModelProjects() throws CoreRuntimeException {
        IIpsProject[] allIpsProjects = getIpsProjects();
        List<IIpsProject> modelProjects = new ArrayList<>(allIpsProjects.length);
        for (IIpsProject ipsProject : allIpsProjects) {
            if (ipsProject.isModelProject()) {
                modelProjects.add(ipsProject);
            }
        }
        return modelProjects.toArray(new IIpsProject[modelProjects.size()]);
    }

    @Override
    public IIpsProject[] getIpsProductDefinitionProjects() throws CoreRuntimeException {
        IIpsProject[] allIpsProjects = getIpsProjects();
        List<IIpsProject> productDefinitionProjects = new ArrayList<>(allIpsProjects.length);
        for (IIpsProject ipsProject : allIpsProjects) {
            if (ipsProject.isProductDefinitionProject()) {
                productDefinitionProjects.add(ipsProject);
            }
        }
        return productDefinitionProjects.toArray(new IIpsProject[productDefinitionProjects.size()]);
    }

    @Override
    public Set<AProject> getNonIpsProjects() throws CoreRuntimeException {
        return Abstractions.getWorkspace()
                .getRoot().getProjects().stream()
                .filter(not(AProject::isIpsProject))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
        return projectMap.computeIfAbsent(name, this::createIpsProject);
    }

    private IpsProject createIpsProject(String name) {
        if (Abstractions.isEclipseRunning()) {
            return new IpsProject.EclipseIpsProject(this, name);
        }
        return new IpsProject(this, name);
    }

    @Override
    public IIpsProject getIpsProject(AProject project) {
        return getIpsProject(project.getName());
    }

    /**
     * Returns the workspace root. Overridden method.
     */
    @Override
    public AResource getCorrespondingResource() {
        return Abstractions.getWorkspace().getRoot();
    }

    @Override
    public boolean exists() {
        return getCorrespondingResource() != null && getCorrespondingResource().exists();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreRuntimeException {
        return getIpsProjects();
    }

    @Override
    public IIpsElement getIpsElement(AResource resource) {
        ArgumentCheck.notNull(resource);
        if (resource.getType() == AResourceType.WORKSPACE) {
            return this;
        }
        if (resource.getType() == AResourceType.PROJECT) {
            return getIpsProject(resource.getName());
        }
        IIpsProject ipsProject = getIpsProject(resource.getProject().getName());
        Path relativePath = resource.getProjectRelativePath();
        IIpsPackageFragmentRoot root = ipsProject.findIpsPackageFragmentRoot(relativePath.subpath(0, 1).toString());
        if (root == null) {
            return getExternalIpsSrcFile(resource);
        }
        if (relativePath.getNameCount() == 1) {
            return root;
        }
        StringBuilder folderName = new StringBuilder();
        for (int i = 1; i < relativePath.getNameCount() - 1; i++) {
            if (i > 1) {
                folderName.append(IIpsPackageFragment.SEPARATOR);
            }
            folderName.append(relativePath.subpath(i, i + 1));
        }

        if (resource.getType() == AResourceType.FOLDER) {
            if (relativePath.getNameCount() > 2) {
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
    private IIpsElement getExternalIpsSrcFile(AResource resource) {
        if (resource.getType() == AResourceType.FILE && resource.exists()
                && getIpsObjectTypeByFileExtension(((AFile)resource).getExtension()) != null) {
            return new IpsSrcFileOffRoot((AFile)resource);
        }
        return null;
    }

    @Override
    public IIpsElement findIpsElement(AResource resource) {
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
     * Tells the model to stop broadcasting any changes made to IPS objects by the current thread.
     * By default changes are broadcasted until this method is called. To restart broadcasting
     * changes the method resumeBroadcastingChangesMadeByCurrentThread() has to be called.
     * <p>
     * <strong>Note</strong> that these two methods have a "nested transaction behavior". That means
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
     * Tells the model to resume broadcasting any changes made to IPS objects by the current thread.
     * <p>
     * <strong>Note</strong> that these two methods have a "nested transaction behavior". That means
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
     * Returns <code>true</code> if the model is currently broadcasting changes made to an IPS
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
            modificationStatusChangeListeners = new HashSet<>(1);
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
        IIpsModelExtensions.get().getWorkspaceInteractions()
                .runInDisplayThreadSync(new RunnableModificationStatusChangeListenerImplementation(event));
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
        Runnable notifier = new RunnableChangeListenerImplementation(event);
        IIpsModelExtensions.get().getWorkspaceInteractions().runInDisplayThreadAsyncIfNotCurrentDisplay(notifier);
    }

    @Override
    public boolean addIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener) {
        return ipsSrcFilesChangeListeners.add(listener);
    }

    @Override
    public boolean removeIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener) {
        return ipsSrcFilesChangeListeners.remove(listener);
    }

    protected void forEachIpsSrcFilesChangeListener(Consumer<? super IIpsSrcFilesChangeListener> consumer) {
        ipsSrcFilesChangeListeners.forEach(consumer);
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
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreRuntimeException {
        List<IIpsPackageFragmentRoot> result = new ArrayList<>();
        IIpsProject[] projects = getIpsProjects();
        for (IIpsProject project : projects) {
            ((IpsProject)project).getSourceIpsFragmentRoots(result);
        }
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    /**
     * Returns the value datatype identified by the given qualified name or null, if the IPS project
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
     * Returns the datatype identified by the given qualified name or null, if the IPS project does
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
        IpsProjectData ipsProjectData = getIpsProjectData(project);
        LinkedHashMap<String, Datatype> projectTypes = ipsProjectData.getProjectDatatypesMap();

        IIpsProjectProperties props = getIpsProjectProperties(project);
        String[] datatypeIds = props.getPredefinedDatatypesUsed();
        Map<String, Datatype> datatypeMap = datatypes.get();
        for (String datatypeId : datatypeIds) {
            Datatype datatype = datatypeMap.get(datatypeId);
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

        IIpsProjectProperties data = getIpsProjectProperties(project);
        if (!builderSet.getId().equals(data.getBuilderSetId())) {
            return registerBuilderSet(project);
        }

        if (reinit) {
            initBuilderSet(builderSet, project, data);
        }
        return builderSet;
    }

    private IIpsArtefactBuilderSet registerBuilderSet(IIpsProject project) {
        IIpsProjectProperties data = getIpsProjectProperties(project);
        IIpsArtefactBuilderSet builderSet = createIpsArtefactBuilderSet(data.getBuilderSetId(), project);
        if (builderSet == null || !initBuilderSet(builderSet, project, data)) {
            EmptyBuilderSet emptyBuilderSet = new EmptyBuilderSet();
            try {
                emptyBuilderSet.initialize(new IpsArtefactBuilderSetConfig(new HashMap<String, Object>()));
            } catch (CoreRuntimeException e) {
                IpsLog.log(e);
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
                IpsLog.log(new IpsStatus("There is no builder set info registered with the id: " //$NON-NLS-1$
                        + builderSet.getId()));
                return false;
            }
            IIpsArtefactBuilderSetConfig builderSetConfig = properties.getBuilderSetConfig().create(ipsProject,
                    builderSetInfo);
            builderSet.initialize(builderSetConfig);
            return true;
        } catch (CoreRuntimeException e) {
            IpsLog.log(new IpsStatus("An exception occurred while trying to initialize" //$NON-NLS-1$
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
    public IDependencyGraph getDependencyGraph(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, this);
        IDependencyGraph graph = getIpsProjectData(ipsProject).getDependencyGraph();
        if (graph == null) {
            graph = IIpsModelExtensions.get().getDependencyGraphPersistenceManager().getDependencyGraph(ipsProject);
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
     * Returns the dependency graph objects that are currently held by this model. This method
     * doesn't guarantee to return the dependency graph objects for all IpsProjects within the
     * workspace but only for those whom have already been instantiated.
     * <p>
     * This method is not part of the published interface.
     */
    public IDependencyGraph[] getCachedDependencyGraphs() {
        List<IDependencyGraph> graphs = new ArrayList<>();
        for (IpsProjectData projectData : ipsProjectDatas.values()) {
            if (projectData.getDependencyGraph() != null) {
                graphs.add(projectData.getDependencyGraph());
            }
        }
        return graphs.toArray(new IDependencyGraph[graphs.size()]);
    }

    /**
     * Returns the properties (stored in the .ipsproject file) for the given IPS project. If an
     * error occurs while accessing the .ipsproject file or the file does not exist an error is
     * logged and an empty IPS project data instance is returned.
     */
    public IpsProjectProperties getIpsProjectProperties(IIpsProject ipsProject) {
        AFile propertyFile = ipsProject.getIpsProjectPropertiesFile();
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
    private IpsProjectProperties readProjectProperties(IIpsProject ipsProject) {
        AFile file = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties properties = new IpsProjectProperties(ipsProject);
        properties.setCreatedFromParsableFileContents(false);
        if (!file.exists()) {
            return properties;
        }
        Document doc;
        InputStream is;
        try {
            is = file.getContents();
        } catch (CoreRuntimeException e1) {
            IpsLog.log(new IpsStatus("Error reading project file contents " //$NON-NLS-1$
                    + file, e1));
            return properties;
        }
        try {
            doc = XmlUtil.getDefaultDocumentBuilder().parse(is);
        } catch (SAXException e) {
            IpsLog.log(new IpsStatus("Error parsing project file " + file, e)); //$NON-NLS-1$
            return properties;
        } catch (IOException e) {
            IpsLog.log(new IpsStatus("Error accessing project file " + file, e)); //$NON-NLS-1$
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                IpsLog.log(new IpsStatus("Error closing input stream after reading project file " //$NON-NLS-1$
                        + file, e));
                return properties;
            }
        }
        try {
            properties = IpsProjectProperties.createFromXml(ipsProject, doc.getDocumentElement());
            properties.setCreatedFromParsableFileContents(true);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsLog.log(new IpsStatus("Error creating properties from xml, file:  " //$NON-NLS-1$
                    + file, e));
            properties.setCreatedFromParsableFileContents(false);
        }
        // CSON: IllegalCatch
        properties.setLastPersistentModificationTimestamp(file.getModificationStamp());
        return properties;
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
    protected void releaseInCache(IIpsSrcFile srcFile) {
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
        builderSetInfoList = () -> new ArrayList<>(Arrays.asList(builderSetInfos));
    }

    @Override
    public Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitionsForClass(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        return customModelExtensions.getExtensionPropertyDefinitions(type, includeSupertypesAndInterfaces);
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

    private Map<String, Datatype> initDatatypesDefinedViaExtension() {
        Map<String, Datatype> datatypesMap = new HashMap<>();
        if (Abstractions.isEclipseRunning()) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint point = registry.getExtensionPoint(IpsModelActivator.PLUGIN_ID, "datatypeDefinition"); //$NON-NLS-1$
            IExtension[] extensions = point.getExtensions();

            // first, get all datatypes defined by the ips-plugin itself
            // to get them at top of the list...
            for (IExtension extension : extensions) {
                if (extension.getNamespaceIdentifier().equals(IpsModelActivator.PLUGIN_ID)) {
                    createDatatypeDefinition(datatypesMap, extension);
                }
            }

            // and second, get the rest.
            for (IExtension extension : extensions) {
                if (!extension.getNamespaceIdentifier().equals(IpsModelActivator.PLUGIN_ID)) {
                    createDatatypeDefinition(datatypesMap, extension);
                }
            }
        } else {
            // same order as in the extensions
            // TODO Über ModelExtensions laden
            Arrays.asList(
                    Datatype.STRING,
                    Datatype.INTEGER,
                    Datatype.LONG,
                    Datatype.BOOLEAN,
                    new DateDatatype(),
                    Datatype.GREGORIAN_CALENDAR,
                    Datatype.DECIMAL,
                    Datatype.MONEY,
                    Datatype.DOUBLE,
                    Datatype.PRIMITIVE_BOOLEAN,
                    Datatype.PRIMITIVE_INT,
                    Datatype.PRIMITIVE_LONG,
                    Datatype.BIG_DECIMAL,
                    new LocalDateDatatype(),
                    new LocalTimeDatatype(),
                    new LocalDateTimeDatatype(),
                    new MonthDayDatatype())
                    .forEach(d -> datatypesMap.put(d.getName(), d));
        }
        return datatypesMap;
    }

    private void createDatatypeDefinition(Map<String, Datatype> datatypesMap, IExtension extension) {
        for (IConfigurationElement configElement : extension.getConfigurationElements()) {
            DatatypeDefinition definition = new DatatypeDefinition(extension, configElement);
            if (definition.hasDatatype()) {
                datatypesMap.put(definition.getDatatype().getQualifiedName(), definition.getDatatype());
            }
        }
    }

    @Override
    public ValueDatatype[] getPredefinedValueDatatypes() {
        Collection<Datatype> c = datatypes.get().values();
        return c.toArray(new ValueDatatype[c.size()]);
    }

    @Override
    public boolean isPredefinedValueDatatype(String valueDatatypeId) {
        return datatypes.get().containsKey(valueDatatypeId);
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
            IpsLog.log(new IpsStatus(IStatus.WARNING,
                    "Unknown changes in time naming convention " + id //$NON-NLS-1$
                            + ". Using default " //$NON-NLS-1$
                            + IChangesOverTimeNamingConvention.VAA,
                    null));
            return convention;
        }
        IpsLog.log(new IpsStatus("Unknown changes in time naming convention " + id //$NON-NLS-1$
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
        for (IChangesOverTimeNamingConvention iChangesOverTimeNamingConvention : changesOverTimeNamingConventionMap
                .values()) {
            conventions[i++] = iChangesOverTimeNamingConvention;
        }
        return conventions;
    }

    private void initChangesOverTimeNamingConventionIfNecessary() {
        if (changesOverTimeNamingConventionMap == null) {
            changesOverTimeNamingConventionMap = new HashMap<>();
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
     * Returns the ClassLoaderProvider for the given IPS project. Uses the System class loader as
     * parent of the class loader that is provided by the returned provider.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     * 
     * @see ClassLoader#getSystemClassLoader()
     */
    public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        IClassLoaderProvider provider = getIpsProjectData(ipsProject).getClassLoaderProvider();
        if (provider == null) {
            // create a new classloader provider, make sure that the jars (inside the provided
            // classloader) will be copied, this fixed the problem if the classloader is used
            // to load classes for DynamicValueDatatype
            provider = IIpsModelExtensions.get().getClassLoaderProviderFactory().getClassLoaderProvider(ipsProject);
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
        if (file == null) {
            return null;
        }

        IpsSrcFileContent content = ipsObjectsMap.get(file);
        if (content == null) {
            if (file.exists()) {
                // new content
                content = readContentFromFile(file, loadCompleteContent);
                ipsObjectsMap.put(file, content);
                return content;
            } else {
                return null;
            }
        }

        AResource enclResource = file.getEnclosingResource();
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

    @Override
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

    private List<IIpsArtefactBuilderSetInfo> createIpsArtefactBuilderSetInfosIfNecessary() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        List<IIpsArtefactBuilderSetInfo> tmpList = new ArrayList<>();
        IpsArtefactBuilderSetInfo.loadExtensions(registry, IpsLog.get(), tmpList,
                this);
        return tmpList;
    }

    /**
     * Returns an array of IpsArtefactBuilderSetInfo objects. Each IpsArtefactBuilderSetInfo object
     * represents an IpsArtefactBuilderSet that is a registered at the corresponding extension
     * point.
     */
    @Override
    public IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos() {
        List<IIpsArtefactBuilderSetInfo> list = builderSetInfoList.get();
        return list.toArray(new IIpsArtefactBuilderSetInfo[list.size()]);
    }

    @Override
    public IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id) {
        for (Object name2 : builderSetInfoList.get()) {
            IIpsArtefactBuilderSetInfo builderSetInfo = (IIpsArtefactBuilderSetInfo)name2;
            if (builderSetInfo.getBuilderSetId().equals(id)) {
                return builderSetInfo;
            }
        }
        return null;
    }

    @Override
    public IpsObjectType[] getIpsObjectTypes() {
        return Arrays.copyOf(ipsObjectTypes, ipsObjectTypes.length);
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
            verionProvider = IIpsModelExtensions.get().getVersionProvider(ipsProject);
            getIpsProjectData(ipsProject).setVersionProvider(verionProvider);
        }
        return verionProvider;
    }

    private static void logTraceMessage(String text, IIpsSrcFile ipsSrcFile) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            AResource enclosingResource = ipsSrcFile.getEnclosingResource();
            System.out.println(
                    MessageFormat.format("IpsModel.getIpsSrcFileContent(): {0}, file={1}, FileModStamp={2}, Thread={3}", //$NON-NLS-1$
                            text, "" + ipsSrcFile, "" + enclosingResource.getModificationStamp(), //$NON-NLS-1$ //$NON-NLS-2$
                            Thread.currentThread().getName()));
        }
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
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

    /**
     * This method executes the logic that is implemented in the provided
     * {@link SingleEventModification} and makes sure that only the {@link ContentChangeEvent} that
     * is provided by the {@link SingleEventModification} is fired. No events are fired during the
     * method execution.
     * 
     * @throws CoreRuntimeException delegates the exceptions from the
     *             {@link SingleEventModification#execute() execute()} method of the
     *             {@link SingleEventModification}
     */
    public <T> T executeModificationsWithSingleEvent(SingleEventModification<T> modifications)
            throws CoreRuntimeException {
        boolean successful = false;
        IIpsSrcFile ipsSrcFile = modifications.getIpsSrcFile();
        IpsSrcFileContent content = getIpsSrcFileContent(ipsSrcFile);
        try {
            stopBroadcastingChangesMadeByCurrentThread();
            successful = modifications.execute();
        } catch (CoreRuntimeException e) {
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
    public void delete() throws CoreRuntimeException {
        throw new UnsupportedOperationException("The IPS Model cannot be deleted."); //$NON-NLS-1$
    }

    /**
     * Returns the {@link IIpsSrcFile IPS source files} of the markers that are configured in the
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
                    IpsLog.log(new IpsStatus("Error notifying IPS model change listener", //$NON-NLS-1$
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
            List<IModificationStatusChangeListener> copy = new CopyOnWriteArrayList<>(
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
                    IpsLog.log(new IpsStatus("Error notifying IPS model ModificationStatusChangeListeners", //$NON-NLS-1$
                            e));
                }
                // CSON: IllegalCatch
            }
        }
    }

    public static class EclipseIpsModel extends IpsModel implements IResourceChangeListener {

        /**
         * Resource delta visitor used to generate IPS source file contents changed events and
         * trigger a build after changes to the IPS project properties file.
         */
        private ResourceDeltaVisitor resourceDeltaVisitor;

        public EclipseIpsModel() {
            super();
            // has to be done after the IPS object types are initialized!
            resourceDeltaVisitor = new ResourceDeltaVisitor(this);
        }

        public void startListeningToResourceChanges() {
            ((IWorkspace)getWorkspace().unwrap()).addResourceChangeListener(this,
                    IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
                            | IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_REFRESH);
        }

        public void stopListeningToResourceChanges() {
            ((IWorkspace)getWorkspace().unwrap()).removeResourceChangeListener(this);
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (event.getType() == IResourceChangeEvent.PRE_REFRESH) {
                if (event.getResource() == null || event.getResource() instanceof IProject) {
                    forceReloadOfCachedIpsSrcFileContents((IProject)event.getResource());
                }
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
                        IpsLog.log(new IpsStatus("Error updating model objects in resurce changed event.", //$NON-NLS-1$
                                e));
                    }
                    // CSON: IllegalCatch
                }
            }
        }

        /**
         * Forces to reload the the cached IPS source file contents of a single project or the whole
         * workspace. This is done by setting {@value #INVALID_MOD_STAMP} as modification stamp in
         * each content object.
         * 
         * @param project The project that should considered or <code>null</code> if the whole
         *            workspace should be considered.
         */
        private synchronized void forceReloadOfCachedIpsSrcFileContents(IProject project) {
            HashSet<IIpsSrcFile> copyKeys = new HashSet<>(getIpsSrcFilesInternal());
            for (IIpsSrcFile srcFile : copyKeys) {
                if (!srcFile.isDirty()
                        && (project == null || srcFile.getIpsProject().getProject().unwrap().equals(project))) {
                    releaseInCache(srcFile);
                }
            }
        }

        private void notifyIpsSrcFileChangedListeners(final Map<IIpsSrcFile, IResourceDelta> changedIpsSrcFiles) {
            forEachIpsSrcFilesChangeListener(
                    listener -> listener.ipsSrcFilesChanged(new IpsSrcFilesChangedEvent(changedIpsSrcFiles)));
        }

        private class IpsSrcFileChangeVisitor implements IResourceDeltaVisitor {

            private Map<IIpsSrcFile, IResourceDelta> changedIpsSrcFiles = new HashMap<>(5);
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
                    AResource aResource = wrap(resource).as(AResource.class);
                    if (delta.getKind() == IResourceDelta.REMOVED) {
                        IIpsElement ipsElement = getIpsElement(aResource);
                        if (ipsElement instanceof IIpsSrcFile && ((IIpsSrcFile)ipsElement).isContainedInIpsRoot()) {
                            changedIpsSrcFiles.put((IIpsSrcFile)ipsElement, delta);
                        }
                    } else {
                        final IIpsElement ipsElement = findIpsElement(aResource);
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

}

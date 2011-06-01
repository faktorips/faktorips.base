/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

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
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.GenericValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.builder.EmptyBuilderSet;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.internal.model.ipsproject.ChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.internal.model.ipsproject.ClassLoaderProvider;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsContainerBasedOnJdtClasspathContainer;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentDefaultSortDefinition;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ICustomModelExtensions;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;

/**
 * Implementation of <tt>IIpsModel</tt>.
 * 
 * @see IIpsModel
 * 
 * @author Jan Ortmann
 */
public class IpsModel extends IpsElement implements IIpsModel, IResourceChangeListener {

    public final static boolean TRACE_MODEL_MANAGEMENT;

    public final static boolean TRACE_MODEL_CHANGE_LISTENERS;

    public final static boolean TRACE_VALIDATION;

    static {
        TRACE_MODEL_MANAGEMENT = Boolean.valueOf(
                Platform.getDebugOption("org.faktorips.devtools.core/trace/modelmanagement")).booleanValue(); //$NON-NLS-1$
        TRACE_MODEL_CHANGE_LISTENERS = Boolean.valueOf(
                Platform.getDebugOption("org.faktorips.devtools.core/trace/modelchangelisteners")) //$NON-NLS-1$
                .booleanValue();
        TRACE_VALIDATION = Boolean.valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/validation")) //$NON-NLS-1$
                .booleanValue();
    }

    /**
     * resource delta visitor used to generate ips sourcefile contents changed events and trigger a
     * build after changes to the ips project properties file.
     */
    private ResourceDeltaVisitor resourceDeltaVisitor;

    /** set of model change listeners that are notified about model changes */
    private Set<ContentsChangeListener> changeListeners = new HashSet<ContentsChangeListener>(100);

    private Set<IIpsSrcFilesChangeListener> ipsSrcFilesChangeListeners = new HashSet<IIpsSrcFilesChangeListener>(10);

    /** set of modification status change listeners */
    private Set<IModificationStatusChangeListener> modificationStatusChangeListeners = new HashSet<IModificationStatusChangeListener>(
            100);

    /**
     * a map that contains per thread if changes should be broadcasted to the registered listeners
     * or squeezed.
     */
    private Map<Thread, Integer> listenerNoticicationLevelMap = new HashMap<Thread, Integer>();

    /**
     * A map containing the dataypes (value) by id (key).
     */
    private Map<String, Datatype> datatypes = null; // lazy load

    /**
     * A map containing a code generation helper (value) per datatype (key)
     */
    private Map<Datatype, DatatypeHelper> datatypeHelpersMap = null;

    /**
     * A map containing the project for every name.
     */
    private Map<String, IpsProject> projectMap = Collections.synchronizedMap(new HashMap<String, IpsProject>());

    /**
     * A map containing the data for each ips project. The name of the project is used as the key
     * and the value is an instance of IpsProjectData.
     */
    private Map<String, IpsProjectProperties> projectPropertiesMap = Collections
            .synchronizedMap(new HashMap<String, IpsProjectProperties>());

    /**
     * a map containing a set of datatypes per ips project. The map's key is the project name.
     */
    private Map<String, LinkedHashMap<String, Datatype>> projectDatatypesMap = Collections
            .synchronizedMap(new HashMap<String, LinkedHashMap<String, Datatype>>());

    /**
     * a map containing a map per ips project. The map's key is the project name. The maps contained
     * in the map, contain the datatypes as keys and the datatype helper as values.
     */
    private Map<String, Map<ValueDatatype, DatatypeHelper>> projectDatatypeHelpersMap = Collections
            .synchronizedMap(new HashMap<String, Map<ValueDatatype, DatatypeHelper>>());

    private Map<IIpsProject, IIpsArtefactBuilderSet> projectToBuilderSetMap = Collections
            .synchronizedMap(new HashMap<IIpsProject, IIpsArtefactBuilderSet>());

    private List<IIpsArtefactBuilderSetInfo> builderSetInfoList = null;

    /** map containing all changes in time naming conventions by id. */
    private Map<String, IChangesOverTimeNamingConvention> changesOverTimeNamingConventionMap = null;

    private Map<IIpsProject, DependencyGraph> dependencyGraphForProjectsMap = new HashMap<IIpsProject, DependencyGraph>();

    /** map containing ClassLoaderProviders per IpsProject */
    private Map<IIpsProject, ClassLoaderProvider> classLoaderProviderMap = new HashMap<IIpsProject, ClassLoaderProvider>();

    /** map containing IpsSrcFileContents as values and IpsSrcFiles as keys. */
    private HashMap<IIpsSrcFile, IpsSrcFileContent> ipsObjectsMap = new HashMap<IIpsSrcFile, IpsSrcFileContent>(1000);

    /** validation result cache */
    private ValidationResultCache validationResultCache = new ValidationResultCache();

    private IpsObjectType[] ipsObjectTypes;

    private CustomModelExtensions customModelExtensions;

    /** cache sort order */
    private Map<IIpsPackageFragment, IIpsPackageFragmentSortDefinition> sortOrderCache = new HashMap<IIpsPackageFragment, IIpsPackageFragmentSortDefinition>();

    private Map<IIpsPackageFragmentSortDefinition, Long> lastIpsSortOrderModifications = new HashMap<IIpsPackageFragmentSortDefinition, Long>();

    private Map<IIpsProject, IIpsObjectPathContainer> ipsObjectPathContainers = new HashMap<IIpsProject, IIpsObjectPathContainer>();

    public IpsModel() {
        super(null, "IpsModel"); //$NON-NLS-1$
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.Constructor(): IpsModel created."); //$NON-NLS-1$
        }
        customModelExtensions = new CustomModelExtensions(this);
        initIpsObjectTypes();
        // has to be done after the ips object types are initialized!
        resourceDeltaVisitor = new ResourceDeltaVisitor();
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
            if (!configElements[i].getName().equalsIgnoreCase("ipsobjecttype")) { //$NON-NLS-1$
                String text = "Illegal ips object type definition" + extension.getUniqueIdentifier() //$NON-NLS-1$
                        + ". Expected Config Element <ipsobjectytpe> was " //$NON-NLS-1$
                        + configElements[i].getName();
                IpsPlugin.log(new IpsStatus(text));
                continue;
            }
            type = ExtensionPoints
                    .createExecutableExtension(extension, configElements[i], "class", IpsObjectType.class); //$NON-NLS-1$

            if (type == null) {
                String text = "Illegal ips object type definition " + extension.getUniqueIdentifier(); //$NON-NLS-1$
                IpsPlugin.log(new IpsStatus(text));
            } else {
                validTypes.add(type);
            }
        }
        return validTypes;
    }

    public void startListeningToResourceChanges() {
        getWorkspace().addResourceChangeListener(this, //
                IResourceChangeEvent.PRE_CLOSE | //
                        IResourceChangeEvent.PRE_DELETE | //
                        IResourceChangeEvent.POST_CHANGE | //
                        IResourceChangeEvent.PRE_REFRESH);
    }

    public void stopListeningToResourceChanges() {
        getWorkspace().removeResourceChangeListener(this);
    }

    @Override
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {

        runAndQueueChangeEvents(action, getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
    }

    @Override
    public void runAndQueueChangeEvents(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor) throws CoreException {

        if (changeListeners.size() == 0 && modificationStatusChangeListeners.size() == 0) {
            getWorkspace().run(action, rule, flags, monitor);
            return;
        }
        List<ContentsChangeListener> listeners = new ArrayList<ContentsChangeListener>(changeListeners);
        final Map<IIpsSrcFile, ContentChangeEvent> changedSrcFileEvents = new HashMap<IIpsSrcFile, ContentChangeEvent>();
        ContentsChangeListener batchListener = new ContentsChangeListener() {

            @Override
            public void contentsChanged(ContentChangeEvent event) {
                ContentChangeEvent newEvent = null;
                ContentChangeEvent previousEvent = changedSrcFileEvents.get(event.getIpsSrcFile());
                if (previousEvent == null) {
                    newEvent = event;
                } else {
                    newEvent = mergeChangeEvent(event, previousEvent);
                }
                changedSrcFileEvents.put(event.getIpsSrcFile(), newEvent);
            }

            private ContentChangeEvent mergeChangeEvent(ContentChangeEvent ce1, ContentChangeEvent ce2) {
                if (ce1.getEventType() == ce2.getEventType()) {
                    if (ce1.getPart() != null && ce1.getPart().equals(ce2.getPart())) {
                        // same event type and part, thus no new event type needed
                        return ce1;
                    }
                }
                // different event types, return WholeContentChangedEvent
                return ContentChangeEvent.newWholeContentChangedEvent(ce1.getIpsSrcFile());
            }
        };
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
            getWorkspace().run(action, rule, flags, monitor);
        } finally {
            // restore change listeners
            removeChangeListener(batchListener);
            changeListeners = new HashSet<ContentsChangeListener>(listeners);

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
    public IIpsProject[] getIpsProjects() throws CoreException {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IIpsProject[] ipsProjects = new IIpsProject[projects.length];
        int counter = 0;
        for (IProject project : projects) {
            if (project.isOpen() && project.hasNature(IIpsProject.NATURE_ID)) {
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
            return null;
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
     * <strong>Note<strong> that these to method have a "nested transaction behaviour". That means
     * broadcasting resumes only if the resume method has been called as many times as the stop
     * method. This allows to implement method that stop/resume broadcasting to call other method
     * that use these methods without resuming broadcasting to early.
     */
    public void stopBroadcastingChangesMadeByCurrentThread() {
        Integer level = listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level == null) {
            level = new Integer(1);
        } else {
            level = new Integer(level.intValue() + 1);
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
     * <strong>Note<strong> that these to method have a "nested transaction behaviour". That means
     * broadcasting resumes only if the resume method has been called as many times as the stop
     * method. This allows to implement method that stop/resume broadcasting to call other method
     * that use these methods without resuming broadcasting to early.
     */
    public void resumeBroadcastingChangesMadeByCurrentThread() {
        Integer level = listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level != null && level.intValue() > 0) {
            level = new Integer(level.intValue() - 1);
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
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                List<IModificationStatusChangeListener> copy = new CopyOnWriteArrayList<IModificationStatusChangeListener>(
                        modificationStatusChangeListeners); // copy do avoid
                // concurrent
                // modifications while
                // iterating
                for (IModificationStatusChangeListener listener : copy) {
                    try {
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out.println("IpsModel.notfiyChangeListeners(): Start notifying listener: "//$NON-NLS-1$
                                    + listener);
                        }
                        listener.modificationStatusHasChanged(event);
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out
                                    .println("IpsModel.notifyModificationStatusChangeListener(): Finished notifying listener: "//$NON-NLS-1$
                                            + listener);
                        }
                    } catch (Exception e) {
                        IpsPlugin.log(new IpsStatus("Error notifying IPS model ModificationStatusChangeListeners", //$NON-NLS-1$
                                e));
                    }
                }
            }
        });
    }

    @Override
    public void addChangeListener(ContentsChangeListener listener) {
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.addChangeListeners(): " + listener);//$NON-NLS-1$
        }
        changeListeners.add(listener);
    }

    @Override
    public void removeChangeListener(ContentsChangeListener listener) {
        boolean wasRemoved = changeListeners.remove(listener);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.removeChangeListeners(): " + listener + ", was removed=" + wasRemoved);//$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void notifyChangeListeners(final ContentChangeEvent event) {
        if (!isBroadcastingChangesForCurrentThread()) {
            return;
        }
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.notfiyChangeListeners(): " + changeListeners.size() + " listeners");//$NON-NLS-1$ //$NON-NLS-2$
        }
        final Runnable notifier = new Runnable() {
            @Override
            public void run() {
                List<ContentsChangeListener> copy = new CopyOnWriteArrayList<ContentsChangeListener>(changeListeners); // copy
                // do
                // avoid
                // concurrent
                // modifications while iterating
                for (ContentsChangeListener listener : copy) {
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
                    } catch (Exception e) {
                        IpsPlugin.log(new IpsStatus("Error notifying IPS model change listener", //$NON-NLS-1$
                                e));
                    }
                }
            }
        };
        if (PlatformUI.isWorkbenchRunning()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(notifier);
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
        final Runnable notifier = new Runnable() {
            @Override
            public void run() {
                List<IIpsSrcFilesChangeListener> copy = new CopyOnWriteArrayList<IIpsSrcFilesChangeListener>(
                        ipsSrcFilesChangeListeners);

                for (IIpsSrcFilesChangeListener listener : copy) {
                    listener.ipsSrcFilesChanged(new IpsSrcFilesChangedEvent(changedIpsSrcFiles));
                }
            }
        };
        if (PlatformUI.isWorkbenchRunning()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(notifier);
        } else {
            notifier.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IIpsModel;
    }

    @Override
    public String toString() {
        return "IpsModel";//$NON-NLS-1$
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

    // TODO Jan: getDatatypesDefinedInProjectPropeties would be a better name
    public Map<String, Datatype> getDatatypesDefinedInProjectProperties(IIpsProject ipsProject) {
        reinitIpsProjectPropertiesIfNecessary((IpsProject)ipsProject);
        Map<String, Datatype> map = projectDatatypesMap.get(ipsProject.getName());
        if (map == null) {
            initDatatypesDefinedInProjectProperties(ipsProject);
            map = projectDatatypesMap.get(ipsProject.getName());
        }
        return map;
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
        IIpsArtefactBuilderSet builderSet = projectToBuilderSetMap.get(project);
        if (builderSet == null) {
            return registerBuilderSet(project);
        }

        IpsProjectProperties data = getIpsProjectProperties((IpsProject)project);
        if (!builderSet.getId().equals(data.getBuilderSetId())) {
            return registerBuilderSet(project);
        }

        if (reinit) {
            initBuilderSet(builderSet, project, data);
        }
        return builderSet;
    }

    private IIpsArtefactBuilderSet registerBuilderSet(IIpsProject project) {
        IpsProjectProperties data = getIpsProjectProperties((IpsProject)project);
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
        projectToBuilderSetMap.put(project, builderSet);
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
     * @throws CoreException will be thrown if an error occurs while trying to validated the
     *             provided IpsProject.
     * @throws NullPointerException if the argument is null
     */
    // TODO the resource change listener method of this IpsModel needs to update
    // the dependencyGraphForProjectsMap
    public DependencyGraph getDependencyGraph(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject, this);
        DependencyGraph graph = dependencyGraphForProjectsMap.get(ipsProject);
        if (graph == null) {
            graph = IpsPlugin.getDefault().getDependencyGraphPersistenceManager().getDependencyGraph(ipsProject);
            if (graph != null) {
                dependencyGraphForProjectsMap.put(ipsProject, graph);
                return graph;
            }
            if (ipsProject.exists()) {
                graph = new DependencyGraph(ipsProject);
                dependencyGraphForProjectsMap.put(ipsProject, graph);
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
    public DependencyGraph[] getCachedDependencyGraphs() {
        Collection<DependencyGraph> graphs = dependencyGraphForProjectsMap.values();
        return graphs.toArray(new DependencyGraph[graphs.size()]);
    }

    /**
     * Returns the datatype helper for the given value datatype or <code>null</code> if no helper is
     * defined for the value datatype.
     */
    public DatatypeHelper getDatatypeHelper(IIpsProject ipsProject, ValueDatatype datatype) {
        reinitIpsProjectPropertiesIfNecessary((IpsProject)ipsProject);
        Map<ValueDatatype, DatatypeHelper> map = projectDatatypeHelpersMap.get(ipsProject.getName());
        if (map == null) {
            initDatatypesDefinedInProjectProperties(ipsProject);
            map = projectDatatypeHelpersMap.get(ipsProject.getName());
        }
        return map.get(datatype);
    }

    /**
     * Returns the properties (stored in the .ipsproject file) for the given ips project. If an
     * error occurs while accessing the .ipsproject file or the file does not exist an error is
     * logged and an empty ips project data instance is returned.
     */
    public IpsProjectProperties getIpsProjectProperties(IpsProject ipsProject) {
        IFile propertyFile = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties data = projectPropertiesMap.get(ipsProject.getName());
        if (data != null
                && propertyFile.exists()
                && !new Long(ipsProject.getIpsProjectPropertiesFile().getModificationStamp()).equals(data
                        .getLastPersistentModificationTimestamp())) {
            clearIpsProjectPropertiesCache(ipsProject);
            data = null;
        }
        if (data == null) {
            data = readProjectData(ipsProject);
            projectPropertiesMap.put(ipsProject.getName(), data);
        }
        return data;
    }

    private void reinitIpsProjectPropertiesIfNecessary(IpsProject ipsProject) {
        getIpsProjectProperties(ipsProject);
    }

    private void clearIpsProjectPropertiesCache(IpsProject ipsProject) {
        projectDatatypesMap.remove(ipsProject.getName());
        projectDatatypeHelpersMap.remove(ipsProject.getName());
        projectPropertiesMap.remove(ipsProject.getName());
        projectToBuilderSetMap.remove(ipsProject);
    }

    /**
     * Reads the project's data from the .ipsproject file.
     */
    private IpsProjectProperties readProjectData(IpsProject ipsProject) {
        IFile file = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties data = new IpsProjectProperties();
        data.setCreatedFromParsableFileContents(false);
        if (!file.exists()) {
            return data;
        }
        Document doc;
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (CoreException e1) {
            IpsPlugin.log(new IpsStatus("Error reading project file contents " //$NON-NLS-1$
                    + file, e1));
            return data;
        }
        try {
            doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e));//$NON-NLS-1$
            return data;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error closing input stream after reading project file " //$NON-NLS-1$
                        + file, e));
                return data;
            }
        }
        try {
            data = IpsProjectProperties.createFromXml(ipsProject, doc.getDocumentElement());
            data.setCreatedFromParsableFileContents(true);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error creating properties from xml, file:  " //$NON-NLS-1$
                    + file, e));
            data.setCreatedFromParsableFileContents(false);
        }
        data.setLastPersistentModificationTimestamp(new Long(file.getModificationStamp()));
        return data;
    }

    /**
     * Intializes the datatypes and their helpers for the project.
     */
    private void initDatatypesDefinedInProjectProperties(IIpsProject project) {
        if (datatypes == null) {
            initDatatypesDefinedViaExtension();
        }
        LinkedHashMap<String, Datatype> projectTypes = new LinkedHashMap<String, Datatype>();
        Map<ValueDatatype, DatatypeHelper> projectHelperMap = new HashMap<ValueDatatype, DatatypeHelper>();
        projectDatatypesMap.put(project.getName(), projectTypes);
        projectDatatypeHelpersMap.put(project.getName(), projectHelperMap);

        IpsProjectProperties props = getIpsProjectProperties((IpsProject)project);
        String[] datatypeIds = props.getPredefinedDatatypesUsed();
        for (String datatypeId : datatypeIds) {
            Datatype datatype = datatypes.get(datatypeId);
            if (datatype == null) {
                continue;
            }
            projectTypes.put(datatypeId, datatype);
            if (datatype.isValueDatatype()) {
                ValueDatatype valueDatatype = (ValueDatatype)datatype;
                DatatypeHelper helper = datatypeHelpersMap.get(valueDatatype);
                if (helper != null) {
                    projectHelperMap.put(valueDatatype, helper);
                }
            }
        }
        List<Datatype> definedDatatypes = props.getDefinedDatatypes();
        for (Datatype datatype : definedDatatypes) {
            projectTypes.put(datatype.getQualifiedName(), datatype);
            if (datatype instanceof GenericValueDatatype) {
                GenericValueDatatype valueDatatype = (GenericValueDatatype)datatype;
                projectHelperMap.put(valueDatatype, new GenericValueDatatypeHelper(valueDatatype));
            }
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_REFRESH) {
            clearIpsObjectCache(event.getResource());
            return;
        }
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                delta.accept(resourceDeltaVisitor);
                IpsSrcFileChangeVisitor visitor = new IpsSrcFileChangeVisitor();
                delta.accept(visitor);
                notifyIpsSrcFileChangedListeners(visitor.changedIpsSrcFiles);
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model objects in resurce changed event.", //$NON-NLS-1$
                        e));
            }
        }
    }

    /**
     * Clearing the cache of a single project or the entire cache if the project is null;
     * 
     * @param iResource The project that cache should be cleared.
     */
    synchronized private void clearIpsObjectCache(IResource iResource) {
        if (iResource == null) {
            ipsObjectsMap.clear();
        } else {
            HashSet<IIpsSrcFile> copyKeys = new HashSet<IIpsSrcFile>(ipsObjectsMap.keySet());
            for (IIpsSrcFile srcFile : copyKeys) {
                if (srcFile.getIpsProject().getProject().equals(iResource)) {
                    ipsObjectsMap.remove(srcFile);
                }
            }
        }
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

    @Override
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces) {

        Set<IExtensionPropertyDefinition> props = customModelExtensions.getExtensionPropertyDefinitions(type,
                includeSupertypesAndInterfaces);
        return props.toArray(new IExtensionPropertyDefinition[props.size()]);
    }

    @Override
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {

        return customModelExtensions.getExtensionPropertyDefinition(type, propertyId, includeSupertypesAndInterfaces);
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
        datatypeHelpersMap = new HashMap<Datatype, DatatypeHelper>();
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
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if (!configElements[i].getName().equalsIgnoreCase("datatypeDefinition")) { //$NON-NLS-1$
                String text = "Illegal datatype definition " + extension.getUniqueIdentifier()//$NON-NLS-1$
                        + ". Expected Config Element <datatypeDefinition> was " //$NON-NLS-1$
                        + configElements[i].getName();
                IpsPlugin.log(new IpsStatus(text));
                continue;
            }
            Object datatypeObj = ExtensionPoints.createExecutableExtension(extension, configElements[i],
                    "datatypeClass", Datatype.class); //$NON-NLS-1$
            if (datatypeObj == null) {
                continue;
            }
            Datatype datatype = (Datatype)datatypeObj;
            datatypes.put(datatype.getQualifiedName(), datatype);
            Object dtHelperObj = ExtensionPoints.createExecutableExtension(extension, configElements[i],
                    "helperClass", DatatypeHelper.class); //$NON-NLS-1$
            if (dtHelperObj == null) {
                continue;
            }
            DatatypeHelper dtHelper = (DatatypeHelper)dtHelperObj;
            dtHelper.setDatatype(datatype);
            datatypeHelpersMap.put(datatype, dtHelper);
        }
    }

    /**
     * Adds the datatype helper and it's datatype to the available once. For testing purposes.
     * During normal execution the available datatypes are discovered by extension point lookup.
     */
    public void addDatatypeHelper(DatatypeHelper helper) {
        Datatype datatype = helper.getDatatype();
        datatypes.put(datatype.getQualifiedName(), datatype);
        datatypeHelpersMap.put(helper.getDatatype(), helper);
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
            IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Unknown changes in time naming convention " + id //$NON-NLS-1$
                    + ". Using default " //$NON-NLS-1$
                    + IChangesOverTimeNamingConvention.VAA, null));
            return convention;
        }
        IpsPlugin.log(new IpsStatus("Unknown changes in time naming convention " + id //$NON-NLS-1$
                + ". Default convention " //$NON-NLS-1$
                + IChangesOverTimeNamingConvention.VAA + " not found!")); //$NON-NLS-1$
        return new ChangesOverTimeNamingConvention("VAA");//$NON-NLS-1$
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
        ClassLoaderProvider provider = classLoaderProviderMap.get(ipsProject);
        if (provider == null) {
            // create a new classloader provider, make sure that the jars (inside the provided
            // classloader) will be copied, this fixed the problem if the classloader is used
            // to load classes for DynamicValueDatatype
            provider = new ClassLoaderProvider(ipsProject.getJavaProject(), ClassLoader.getSystemClassLoader(),
                    ipsProject.getReadOnlyProperties().isJavaProjectContainsClassesForDynamicDatatypes(), true);
            classLoaderProviderMap.put(ipsProject, provider);
        }
        return provider;
    }

    /**
     * Returns the cache for the validation result.
     */
    public ValidationResultCache getValidationResultCache() {
        return validationResultCache;
    }

    /**
     * Returns the content for the given IpsSrcFile.
     */
    synchronized public void removeIpsSrcFileContent(IIpsSrcFile file) {
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
     * Returns the content for the given ips src file. If the ips source file's corresponding
     * resource does not exist, the method returns <code>null</code>. If loadCompleteContent is
     * <code>true</code> then the complete content will be read from the source file, if
     * <code>false</code> then only the properties of the ips object will be read.
     * 
     * @param file the file to read
     * 
     * @param loadCompleteContent <code>true</code> if the completely file should be read,
     *            <code>false</code> if only the properties will be read
     */
    synchronized public IpsSrcFileContent getIpsSrcFileContent(IIpsSrcFile file, boolean loadCompleteContent) {
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
        if (enclResource == null || !enclResource.exists()) {
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
            logTraceMessage("New content created", file);//$NON-NLS-1$
            content.initContentFromFile();
        } else {
            logTraceMessage("New properties read", file);//$NON-NLS-1$
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

    synchronized public IpsSrcFileContent getIpsSrcFileContent(IIpsSrcFile file) {
        return getIpsSrcFileContent(file, true);
    }

    /**
     * Returns <code>true</code> if the ips source file' content is in sync with the enclosing
     * resource storing it's contents.
     */
    public synchronized boolean isInSyncWithEnclosingResource(IIpsSrcFile file) {
        IResource enclResource = file.getEnclosingResource();
        if (enclResource == null || !enclResource.exists()) {
            return false;
        }
        IpsSrcFileContent content = ipsObjectsMap.get(file);
        if (content == null) {
            return true;
        }
        return content.getModificationStamp() == enclResource.getModificationStamp();
    }

    public void ipsSrcFileContentHasChanged(ContentChangeEvent event) {
        IIpsSrcFile file = event.getIpsSrcFile();
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.ipsSrcFileHasChanged(), file=" + file //$NON-NLS-1$
                    + ", Thead: " + Thread.currentThread().getName());//$NON-NLS-1$
        }
        validationResultCache.removeStaleData(file);
        notifyChangeListeners(event);
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.ipsSrcFileHasChanged(), file=" //$NON-NLS-1$
                    + event.getIpsSrcFile() + ", Thead: " + Thread.currentThread().getName());//$NON-NLS-1$
        }
    }

    public void ipsSrcFileModificationStatusHasChanged(ContentChangeEvent event) {
        notifyChangeListeners(event);
    }

    /**
     * ResourceDeltaVisitor to generate IPS model change events.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        private Set<String> fileExtensionsOfInterest = new HashSet<String>(20);

        public ResourceDeltaVisitor() {
            IpsObjectType[] types = getIpsObjectTypes();
            for (IpsObjectType type : types) {
                fileExtensionsOfInterest.add(type.getFileExtension());
            }
            fileExtensionsOfInterest.add(IpsProject.PROPERTY_FILE_EXTENSION);
        }

        @Override
        public boolean visit(final IResourceDelta delta) {
            IResource resource = delta.getResource();
            try {
                if (resource == null || resource.getType() != IResource.FILE) {
                    return true;
                }
                if (!fileExtensionsOfInterest.contains(((IFile)resource).getFileExtension())) {
                    return false;
                }
                IIpsProject ipsProject = getIpsProject(resource.getProject());
                if (resource.equals(((IpsProject)ipsProject).getIpsProjectPropertiesFile())) {
                    validationResultCache.clear();
                    return false;
                }
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    IIpsElement ipsElement = getIpsElement(resource);
                    if (ipsElement instanceof IIpsSrcFile) {
                        removeIpsSrcFileContent((IIpsSrcFile)ipsElement);
                        return false;
                    }
                }

                final IIpsElement element = findIpsElement(resource);
                if (!(element instanceof IIpsSrcFile)) { // this includes element==null!
                    return true;
                }
                IpsSrcFile srcFile = (IpsSrcFile)element;
                IpsSrcFileContent content = getIpsSrcFileContent(srcFile);
                boolean isInSync = content == null
                        || content.wasModStampCreatedBySave(srcFile.getEnclosingResource().getModificationStamp());
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out
                            .println("IpsModel.ResourceDeltaVisitor.visit(): Received notification of IpsSrcFile change/delete on disk with modStamp " //$NON-NLS-1$
                                    + resource.getModificationStamp() + ", Sync status=" + isInSync + ", " //$NON-NLS-1$ //$NON-NLS-2$
                                    + srcFile + " Thread: " + Thread.currentThread().getName());//$NON-NLS-1$
                }
                if (!isInSync) {
                    ipsSrcFileContentHasChanged(ContentChangeEvent.newWholeContentChangedEvent(srcFile));
                }
                return true;
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model objects after resource " //$NON-NLS-1$
                        + resource + " changed.", e));//$NON-NLS-1$
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out
                            .println("IpsModel.ResourceDeltaVisitor.visit(): Error updating model objects after resource changed, resource="//$NON-NLS-1$
                                    + resource);
                }
            }
            return true;
        }
    }

    @Override
    public void clearValidationCache() {
        getValidationResultCache().removeStaleData(null);
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

    private static void logTraceMessage(String text, IIpsSrcFile ipsSrcFile) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            IResource enclosingResource = ipsSrcFile.getEnclosingResource();
            System.out.println(NLS.bind("IpsModel.getIpsSrcFileContent(): {0}, file={1}, FileModStamp={2}, Thread={3}",//$NON-NLS-1$
                    new String[] { text, "" + ipsSrcFile, "" + enclosingResource.getModificationStamp(),//$NON-NLS-1$ //$NON-NLS-2$
                            Thread.currentThread().getName() }));
        }
    }

    /**
     * Add the key/value pair to the cache: key = IIpsPackageFragment; value =
     * IIpsPackageFragmentSortDefinition
     * 
     * @param fragment Key of the hash table entry. The fragment is part of the sortDefinition.
     * @param sortDefinition Value of the hash table entry.
     */
    private void addSortDefinition(IIpsPackageFragment fragment,
            IIpsPackageFragmentSortDefinition sortDefinition,
            Long lastModification) {

        sortOrderCache.put(fragment, sortDefinition);
        lastIpsSortOrderModifications.put(sortDefinition, lastModification);
    }

    /**
     * Get a IIpsPackageFragmentSortDefinition for a given IIpsPackageFragment. Returns the object
     * from the cache if the file exists and didn't change, otherwise update sort order from the
     * file system.
     * 
     * @param fragment Key of the hash table entry. The fragment is part of the sortDefinition.
     * @return A IIpsPackageFragmentSortDefinition implementation. THe return value should always be
     *         not <code>null</code>.
     */
    public IIpsPackageFragmentSortDefinition getSortDefinition(IIpsPackageFragment fragment) {
        IIpsPackageFragmentSortDefinition sortDef = sortOrderCache.get(fragment);
        boolean sortDefModified = false;
        if (sortDef != null) {
            sortDefModified = checkCachedSortDefinitionModification(fragment, sortDef);
        }
        if (sortDefModified || sortDef == null) {
            sortDef = addNewOrUpdatedSortOrder(fragment);
        }
        return sortDef;
    }

    private boolean checkCachedSortDefinitionModification(IIpsPackageFragment fragment,
            IIpsPackageFragmentSortDefinition sortDef) {

        IFile file = fragment.getSortOrderFile();
        if (file == null || !(file.exists())) {
            // remove deleted sort orders
            // TODO Distinguish between DefaultSortOrder and deleted files!.
            sortOrderCache.remove(fragment);
            lastIpsSortOrderModifications.remove(sortDef);
            return true;
        }

        Long lastModification = lastIpsSortOrderModifications.get(sortDef);
        if (lastModification == null) {
            throw new IllegalStateException();
        }
        if (!(lastModification.equals(file.getModificationStamp()))) {
            // update current fragment
            sortOrderCache.remove(fragment);
            lastIpsSortOrderModifications.remove(sortDef);
            return true;
        }
        return false;
    }

    private IIpsPackageFragmentSortDefinition addNewOrUpdatedSortOrder(IIpsPackageFragment fragment) {
        IIpsPackageFragmentSortDefinition sortDef = new IpsPackageFragmentDefaultSortDefinition();
        Long lastModification = 0l;

        IFile file = fragment.getSortOrderFile();
        try {
            if (file != null && file.exists()) {
                sortDef = ((IpsPackageFragment)fragment).loadSortDefinition();
                if (sortDef != null) {
                    lastModification = file.getModificationStamp();
                }
            }
        } catch (CoreException e) {
            sortDef = new IpsPackageFragmentDefaultSortDefinition();
            lastModification = 0l;
            IpsPlugin.log(e);
        }

        addSortDefinition(fragment, sortDef, lastModification);
        return sortDef;
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
    }

    @Override
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
    // TODO the implementation currently supports only JDT classpath container!!!!
    public IIpsObjectPathContainer getIpsObjectPathContainer(IIpsProject ipsProject, String containerKind) {
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(containerKind);
        if (containerKind.equals(IpsContainerBasedOnJdtClasspathContainer.KIND)) {
            IIpsObjectPathContainer container = ipsObjectPathContainers.get(ipsProject);
            if (container == null) {
                container = new IpsContainerBasedOnJdtClasspathContainer();
                ipsObjectPathContainers.put(ipsProject, container);
            }
            return container;
        }
        return null;
    }

    private class IpsSrcFileChangeVisitor implements IResourceDeltaVisitor {

        private Map<IIpsSrcFile, IResourceDelta> changedIpsSrcFiles = new HashMap<IIpsSrcFile, IResourceDelta>(5);
        private Set<String> fileExtensionsOfInterest;

        public IpsSrcFileChangeVisitor() {
            fileExtensionsOfInterest = resourceDeltaVisitor.fileExtensionsOfInterest;
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
                    if (ipsElement instanceof IIpsSrcFile) {
                        changedIpsSrcFiles.put((IIpsSrcFile)ipsElement, delta);
                        return false;
                    }
                }

                final IIpsElement element = findIpsElement(resource);
                if (element instanceof IIpsSrcFile) { // this includes element==null!
                    IpsSrcFile srcFile = (IpsSrcFile)element;
                    changedIpsSrcFiles.put(srcFile, delta);
                    return false;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
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

}

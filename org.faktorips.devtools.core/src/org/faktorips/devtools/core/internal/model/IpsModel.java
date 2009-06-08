/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
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
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentDefaultSortDefinition;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;

/**
 * Implementation of IpsModel.
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

    // resource delta visitor used to generate ips sourcefile contents changed events and trigger a
    // build after
    // changes to the ips project properties file.
    private ResourceDeltaVisitor resourceDeltaVisitor;

    // set of model change listeners that are notified about model changes
    private Set changeListeners = new HashSet(100);

    // set of modification status change listeners
    private Set modificationStatusChangeListeners = new HashSet(100);

    // a map that contains per thread if changes should be broadcasted to the registered listeners
    // or squeezed.
    private Map listenerNoticicationLevelMap = new HashMap();

    /*
     * A map containing the dataypes (value) by id (key).
     */
    private Map datatypes = null; // lazy load

    /*
     * A map containing a code generation helper (value) per datatype (key)
     */
    private Map datatypeHelpersMap = null;

    /*
     * A map containing the data for each ips project. The name of the project is used as the key
     * and the value is an instance of IpsProjectData.
     */
    private Map projectPropertiesMap = Collections.synchronizedMap(new HashMap());

    // a map containing a set of datatypes per ips project. The map's key is the
    // project name.
    private Map<String, LinkedHashMap<String, Datatype>> projectDatatypesMap = Collections
            .synchronizedMap(new HashMap<String, LinkedHashMap<String, Datatype>>());

    // a map containing a map per ips project. The map's key is the project name.
    // The maps contained in the map, contain the datatypes as keys and the
    // datatype helper as values.
    private Map<String, Map<ValueDatatype, DatatypeHelper>> projectDatatypeHelpersMap = Collections
            .synchronizedMap(new HashMap<String, Map<ValueDatatype, DatatypeHelper>>());

    private Map projectToBuilderSetMap = Collections.synchronizedMap(new HashMap());

    private List builderSetInfoList = null;

    // extension properties (as list) per ips object (or part) type, e.g.
    // IAttribute.
    private Map typeExtensionPropertiesMap = null; // null as long as they

    // map containing all changes in time naming conventions by id.
    private Map changesOverTimeNamingConventionMap = null;

    private Map dependencyGraphForProjectsMap = new HashMap();

    // map containing ClassLoaderProviders per IpsProject
    private Map classLoaderProviderMap = new HashMap();

    // map containing IpsSrcFileContents as values and IpsSrcFiles as keys.
    private HashMap ipsObjectsMap = new HashMap(1000);

    // validation result cache
    private ValidationResultCache validationResultCache = new ValidationResultCache();

    private IpsObjectType[] ipsObjectTypes;

    // cache sort order
    private Map sortOrderCache = new HashMap();
    private Map lastIpSortOrderModifications = new HashMap();

    public IpsModel() {
        super(null, "IpsModel"); //$NON-NLS-1$
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.Constructor(): IpsModel created."); //$NON-NLS-1$
        }
        initIpsObjectTypes();
        resourceDeltaVisitor = new ResourceDeltaVisitor(); // has to be done after the ips object
        // types are initialized!
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
        for (int i = 0; i < extensions.length; i++) {
            IpsObjectType type = createIpsObjectType(extensions[i]);
            if (type != null) {
                types.add(type);
            }
        }
        IpsObjectType[] typesArray = types.toArray(new IpsObjectType[types.size()]);
        ipsObjectTypes = typesArray;
        IpsObjectType.ALL_TYPES = typesArray;
        if (TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsModel.initIpsObjectType: finished."); //$NON-NLS-1$
        }
    }

    private IpsObjectType createIpsObjectType(IExtension extension) {
        IpsObjectType type = null;
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if (!configElements[i].getName().equalsIgnoreCase("ipsobjecttype")) { //$NON-NLS-1$
                String text = "Illegal ips object type definition" //$NON-NLS-1$
                        + extension.getUniqueIdentifier() + ". Expected Config Element <ipsobjectytpe> was " //$NON-NLS-1$
                        + configElements[i].getName();
                IpsPlugin.log(new IpsStatus(text));
                continue;
            }
            type = (IpsObjectType)ExtensionPoints.createExecutableExtension(extension, configElements[i],
                    "class", IpsObjectType.class); //$NON-NLS-1$
        }
        if (type == null) {
            String text = "Illegal ips object type definition" + extension.getUniqueIdentifier(); //$NON-NLS-1$
            IpsPlugin.log(new IpsStatus(text));
        }
        return type;
    }

    public void startListeningToResourceChanges() {
        getWorkspace().addResourceChangeListener(this);
    }

    public void stopListeningToResourceChanges() {
        getWorkspace().removeResourceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {

        runAndQueueChangeEvents(action, getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
    }

    /**
     * {@inheritDoc}
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor) throws CoreException {

        if (changeListeners.size() == 0 && modificationStatusChangeListeners.size() == 0) {
            getWorkspace().run(action, rule, flags, monitor);
            return;
        }
        List listeners = new ArrayList(changeListeners);
        final Set changedSrcFiles = new LinkedHashSet();
        ContentsChangeListener batchListener = new ContentsChangeListener() {

            public void contentsChanged(ContentChangeEvent event) {
                changedSrcFiles.add(event.getIpsSrcFile());
            }

        };
        changeListeners.clear();
        this.addChangeListener(batchListener);

        HashSet copyOfCurrentModifyListeners = new HashSet(modificationStatusChangeListeners);
        final Set modifiedSrcFiles = new LinkedHashSet(0);
        IModificationStatusChangeListener batchModifiyListener = new IModificationStatusChangeListener() {

            public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
                modifiedSrcFiles.add(event.getIpsSrcFile());
            }

        };
        modificationStatusChangeListeners.clear();
        this.addModifcationStatusChangeListener(batchModifiyListener);

        try {
            getWorkspace().run(action, rule, flags, monitor);
        } finally {
            // restore change listeners
            this.removeChangeListener(batchListener);
            changeListeners = new HashSet(listeners);

            // notify about changes
            for (Iterator it = changedSrcFiles.iterator(); it.hasNext();) {
                IIpsSrcFile file = (IIpsSrcFile)it.next();
                ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(file);
                notifyChangeListeners(event);
            }

            removeModificationStatusChangeListener(batchModifiyListener);
            modificationStatusChangeListeners = copyOfCurrentModifyListeners;
            for (Iterator it = modifiedSrcFiles.iterator(); it.hasNext();) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)it.next();
                ModificationStatusChangedEvent event = new ModificationStatusChangedEvent(ipsSrcFile);
                notifyModificationStatusChangeListener(event);
            }
        }

    }

    public IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public IIpsProject[] getIpsProjects() throws CoreException {

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IIpsProject[] ipsProjects = new IIpsProject[projects.length];
        int counter = 0;
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].isOpen() && projects[i].hasNature(IIpsProject.NATURE_ID)) {
                ipsProjects[counter] = getIpsProject(projects[i].getName());
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public IIpsModel getIpsModel() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject(String name) {
        return new IpsProject(this, name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject(IProject project) {
        return new IpsProject(this, project.getName());
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsModel.gif"); //$NON-NLS-1$
    }

    /**
     * Returns the workspace root. Overridden method.
     */
    public IResource getCorrespondingResource() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsProjects();
    }

    /**
     * {@inheritDoc}
     */
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
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoot(segments[0]);
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

    /**
     * {@inheritDoc}
     */
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
        Integer level = (Integer)listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level == null) {
            level = new Integer(1);
        } else {
            level = new Integer(level.intValue() + 1);
        }
        listenerNoticicationLevelMap.put(Thread.currentThread(), level);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out
                    .println("IpsModel.stopBroadcastingChangesMadeByCurrentThread(): Thread=" + Thread.currentThread() + ", new level=" + level); //$NON-NLS-1$ //$NON-NLS-2$
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
        Integer level = (Integer)listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level != null && level.intValue() > 0) {
            level = new Integer(level.intValue() - 1);
        }
        listenerNoticicationLevelMap.put(Thread.currentThread(), level);
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out
                    .println("IpsModel.restartBroadcastingChangesMadeByCurrentThread(): Thread=" + Thread.currentThread() + ", new level=" + level); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Returns <code>true</code> if the model is currently broadcasting changes made to an ips
     * object by the current thread.
     */
    public boolean isBroadcastingChangesForCurrentThread() {
        Integer level = (Integer)listenerNoticicationLevelMap.get(Thread.currentThread());
        if (level == null || level.intValue() == 0) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void addModifcationStatusChangeListener(IModificationStatusChangeListener listener) {
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.addModificationStatusChangeListener(): " + listener); //$NON-NLS-1$
        }
        if (this.modificationStatusChangeListeners == null) {
            modificationStatusChangeListeners = new HashSet(1);
        }
        modificationStatusChangeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeModificationStatusChangeListener(IModificationStatusChangeListener listener) {
        if (modificationStatusChangeListeners != null) {
            boolean wasRemoved = modificationStatusChangeListeners.remove(listener);
            if (TRACE_MODEL_CHANGE_LISTENERS) {
                System.out
                        .println("IpsModel.removeModificationStatusChangeListener(): " + listener + ", was removed=" + wasRemoved); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public void notifyModificationStatusChangeListener(final ModificationStatusChangedEvent event) {
        if (modificationStatusChangeListeners.size() == 0 || !isBroadcastingChangesForCurrentThread()) {
            return;
        }
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out
                    .println("IpsModel.notifyModificationStatusChangeListener(): " + modificationStatusChangeListeners.size() + " listeners"); //$NON-NLS-1$  //$NON-NLS-2$
        }
        Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                List copy = new ArrayList(modificationStatusChangeListeners); // copy do avoid
                // concurrent
                // modifications while
                // iterating
                for (Iterator it = copy.iterator(); it.hasNext();) {
                    try {
                        IModificationStatusChangeListener listener = (IModificationStatusChangeListener)it.next();
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out
                                    .println("IpsModel.notfiyChangeListeners(): Start notifying listener: " + listener); //$NON-NLS-1$
                        }
                        listener.modificationStatusHasChanged(event);
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out
                                    .println("IpsModel.notifyModificationStatusChangeListener(): Finished notifying listener: " + listener); //$NON-NLS-1$
                        }
                    } catch (Exception e) {
                        IpsPlugin.log(new IpsStatus("Error notifying IPS model ModificationStatusChangeListeners", //$NON-NLS-1$
                                e));
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addChangeListener(ContentsChangeListener listener) {
        if (TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println("IpsModel.addChangeListeners(): " + listener); //$NON-NLS-1$
        }
        changeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
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
            System.out.println("IpsModel.notfiyChangeListeners(): " + changeListeners.size() + " listeners"); //$NON-NLS-1$  //$NON-NLS-2$
        }
        final Runnable notifier = new Runnable() {
            public void run() {
                List copy = new ArrayList(changeListeners); // copy do avoid concurrent
                // modifications while iterating
                for (Iterator it = copy.iterator(); it.hasNext();) {
                    if (!event.getIpsSrcFile().exists()) {
                        break;
                    }
                    try {
                        ContentsChangeListener listener = (ContentsChangeListener)it.next();
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out
                                    .println("IpsModel.notfiyChangeListeners(): Start notifying listener: " + listener); //$NON-NLS-1$
                        }
                        listener.contentsChanged(event);
                        if (TRACE_MODEL_CHANGE_LISTENERS) {
                            System.out
                                    .println("IpsModel.notfiyChangeListeners(): Finished notifying listener: " + listener); //$NON-NLS-1$
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

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        return o instanceof IIpsModel;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "IpsModel"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        IIpsProject[] projects = getIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            ((IpsProject)projects[i]).getSourceIpsFragmentRoots(result);
        }
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    /**
     * Adds the value datatypes defined for the IPS project to the set of datatypes.
     */
    public void getValueDatatypes(IIpsProject ipsProject, Set<Datatype> datatypes) {
        Map<String, Datatype> map = getDatatypesDefinedInProject(ipsProject);
        for (Datatype datatype : map.values()) {
            if (datatype.isValueDatatype()) {
                datatypes.add(datatype);
            }
        }
        return;
    }

    /**
     * Returns the value datatype identified by the given qualified name or null, if the ips project
     * does not contain such a datatype.
     */
    public ValueDatatype getValueDatatype(IIpsProject ipsProject, String qName) {
        Datatype datatype = getDefinedDatatype(ipsProject, qName);
        if (datatype != null && datatype.isValueDatatype()) {
            return (ValueDatatype)datatype;
        }
        return null;
    }

    /**
     * Returns the datatype identified by the given qualified name or null, if the ips project does
     * not contain such a datatype.
     */
    public Datatype getDefinedDatatype(IIpsProject ipsProject, String qName) {
        Map<String, Datatype> map = getDatatypesDefinedInProject(ipsProject);
        return map.get(qName);
    }

    /**
     * Adds the datatypes defined for the IPS project to the set of datatypes.
     */
    public void getDatatypesDefinedInProject(IIpsProject ipsProject,
            boolean valuetypesOnly,
            boolean includePrimitives,
            Set<Datatype> datatypes) {
        Map<String, Datatype> map = getDatatypesDefinedInProject(ipsProject);
        for (Datatype datatype : map.values()) {
            if (!valuetypesOnly || datatype.isValueDatatype()) {
                if (includePrimitives || !datatype.isPrimitive()) {
                    datatypes.add(datatype);
                }
            }
        }
        return;
    }

    public Map<String, Datatype> getDatatypesDefinedInProject(IIpsProject ipsProject) {
        reinitIpsProjectPropertiesIfNecessary((IpsProject)ipsProject);
        Map<String, Datatype> map = projectDatatypesMap.get(ipsProject.getName());
        if (map == null) {
            initDatatypes(ipsProject);
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
        IIpsArtefactBuilderSet builderSet = (IIpsArtefactBuilderSet)projectToBuilderSetMap.get(project);
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
            return new EmptyBuilderSet();
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
                IpsPlugin.log(new IpsStatus(
                        "There is no builder set info registered with the id: " + builderSet.getId())); //$NON-NLS-1$
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
        DependencyGraph graph = (DependencyGraph)dependencyGraphForProjectsMap.get(ipsProject);
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
        Collection graphs = dependencyGraphForProjectsMap.values();
        return (DependencyGraph[])graphs.toArray(new DependencyGraph[graphs.size()]);
    }

    /**
     * Returns the datatype helper for the given value datatype or <code>null</code> if no helper is
     * defined for the value datatype.
     */
    public DatatypeHelper getDatatypeHelper(IIpsProject ipsProject, ValueDatatype datatype) {
        reinitIpsProjectPropertiesIfNecessary((IpsProject)ipsProject);
        Map map = (Map)projectDatatypeHelpersMap.get(ipsProject.getName());
        if (map == null) {
            initDatatypes(ipsProject);
            map = (Map)projectDatatypeHelpersMap.get(ipsProject.getName());
        }
        return (DatatypeHelper)map.get(datatype);
    }

    /**
     * Returns the properties (stored in the .ipsproject file) for the given ips project. If an
     * error occurs while accessing the .ipsproject file or the file does not exist an error is
     * logged and an empty ips project data instance is returned.
     */
    public IpsProjectProperties getIpsProjectProperties(IpsProject ipsProject) {
        IFile propertyFile = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties data = (IpsProjectProperties)projectPropertiesMap.get(ipsProject.getName());
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
            doc = IpsPlugin.getDefault().newDocumentBuilder().parse(is);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e)); //$NON-NLS-1$
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

    /*
     * Intializes the datatypes and their helpers for the project.
     */
    private void initDatatypes(IIpsProject project) {
        if (datatypes == null) {
            initDatatypesDefinedViaExtension();
        }
        LinkedHashMap<String, Datatype> projectTypes = new LinkedHashMap<String, Datatype>();
        Map<ValueDatatype, DatatypeHelper> projectHelperMap = new HashMap<ValueDatatype, DatatypeHelper>();
        projectDatatypesMap.put(project.getName(), projectTypes);
        projectDatatypeHelpersMap.put(project.getName(), projectHelperMap);

        IpsProjectProperties props = getIpsProjectProperties((IpsProject)project);
        String[] datatypeIds = props.getPredefinedDatatypesUsed();
        for (int i = 0; i < datatypeIds.length; i++) {
            Datatype datatype = (Datatype)datatypes.get(datatypeIds[i]);
            if (datatype == null) {
                continue;
            }
            projectTypes.put(datatypeIds[i], datatype);
            if (datatype.isValueDatatype()) {
                ValueDatatype valueDatatype = (ValueDatatype)datatype;
                DatatypeHelper helper = (DatatypeHelper)datatypeHelpersMap.get(valueDatatype);
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

    /**
     * {@inheritDoc}
     */
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                delta.accept(resourceDeltaVisitor);
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model objects in resurce changed event.", //$NON-NLS-1$
                        e));
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
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getBuilderSetId().equals(builderSetId)) {
                IIpsArtefactBuilderSet builderSet = infos[i].create(ipsProject);
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
        builderSetInfoList = new ArrayList(Arrays.asList(builderSetInfos));
    }

    /**
     * {@inheritDoc}
     */
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class type,
            boolean includeSupertypesAndInterfaces) {
        if (typeExtensionPropertiesMap == null) {
            initExtensionPropertiesFromConfiguration();
        }
        Set result = new HashSet();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, result);
        IExtensionPropertyDefinition[] properties = (IExtensionPropertyDefinition[])result
                .toArray(new IExtensionPropertyDefinition[result.size()]);

        // Sort extension property definitions by id to avoid random arrangement at return
        Arrays.sort(properties, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (!(o1 instanceof IExtensionPropertyDefinition) || !(o2 instanceof IExtensionPropertyDefinition)) {
                    return 0;
                }
                IExtensionPropertyDefinition def1 = (IExtensionPropertyDefinition)o1;
                IExtensionPropertyDefinition def2 = (IExtensionPropertyDefinition)o2;
                return def1.getPropertyId().compareTo(def2.getPropertyId());
            }

        });
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {
        Set props = new HashSet();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, props);
        for (Iterator it = props.iterator(); it.hasNext();) {
            IExtensionPropertyDefinition prop = (IExtensionPropertyDefinition)it.next();
            if (prop.getPropertyId().equals(propertyId)) {
                return prop;
            }
        }
        return null;
    }

    /*
     * Same as above but with collection parameter result.
     */
    private void getIpsObjectExtensionProperties(Class type, boolean includeSupertypesAndInterfaces, Set result) {
        List props = (List)typeExtensionPropertiesMap.get(type);
        if (props != null) {
            result.addAll(props);
        }
        if (!includeSupertypesAndInterfaces) {
            return;
        }
        if (type.getSuperclass() != null) {
            getIpsObjectExtensionProperties(type.getSuperclass(), true, result);
        }
        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            getIpsObjectExtensionProperties(interfaces[i], true, result);
        }
    }

    private void initExtensionPropertiesFromConfiguration() {
        typeExtensionPropertiesMap = new HashMap();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "objectExtensionProperty"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {
            IExtensionPropertyDefinition property = createExtensionProperty(extensions[i]);
            if (property != null) {
                List props = (ArrayList)typeExtensionPropertiesMap.get(property.getExtendedType());
                if (props == null) {
                    props = new ArrayList();
                    typeExtensionPropertiesMap.put(property.getExtendedType(), props);
                }
                props.add(property);
            }
        }
        sortExtensionProperties();
    }

    private IExtensionPropertyDefinition createExtensionProperty(IExtension extension) {
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        if (configElements.length != 1 || !configElements[0].getName().equalsIgnoreCase("property")) { //$NON-NLS-1$
            IpsPlugin.log(new IpsStatus("Illegal definition of external property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier()));
            return null;
        }
        IConfigurationElement element = configElements[0];
        Object propertyInstance = null;
        try {
            propertyInstance = element.createExecutableExtension("class"); //$NON-NLS-1$
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + ". Reason: Can't instantiate " //$NON-NLS-1$
                    + element.getAttribute("class"), e)); //$NON-NLS-1$
            return null;
        }
        if (!(propertyInstance instanceof ExtensionPropertyDefinition)) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + element.getAttribute("class") + " does not derived from " //$NON-NLS-1$ //$NON-NLS-2$
                    + ExtensionPropertyDefinition.class));
            return null;
        }
        ExtensionPropertyDefinition extProperty = (ExtensionPropertyDefinition)propertyInstance;
        extProperty.setPropertyId(extension.getUniqueIdentifier());
        extProperty.setName(extension.getLabel());
        extProperty.setDefaultValue(element.getAttribute("defaultValue")); //$NON-NLS-1$
        extProperty.setPosition(element.getAttribute("position")); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(element.getAttribute("order"))) { //$NON-NLS-1$
            extProperty.setSortOrder(Integer.parseInt(element.getAttribute("order"))); //$NON-NLS-1$
        }
        String extType = element.getAttribute("extendedType"); //$NON-NLS-1$
        try {
            extProperty.setExtendedType(extProperty.getClass().getClassLoader().loadClass(extType));
        } catch (ClassNotFoundException e) {
            IpsPlugin.log(new IpsStatus("Extended type " + extType //$NON-NLS-1$
                    + " not found for extension property " //$NON-NLS-1$
                    + extProperty.getPropertyId(), e));
            return null;
        }
        return extProperty;
    }

    private void sortExtensionProperties() {
        Collection typeLists = typeExtensionPropertiesMap.values();
        for (Iterator it = typeLists.iterator(); it.hasNext();) {
            List propList = (List)it.next();
            Collections.sort(propList);
        }
    }

    /**
     * Adds the extension property. For testing purposes. During normal execution the available
     * extension properties are discovered by extension point lookup.
     */
    public void addIpsObjectExtensionProperty(IExtensionPropertyDefinition property) {
        if (typeExtensionPropertiesMap == null) {
            typeExtensionPropertiesMap = new HashMap();
        }
        List props = (List)typeExtensionPropertiesMap.get(property.getExtendedType());
        if (props == null) {
            props = new ArrayList();
            typeExtensionPropertiesMap.put(property.getExtendedType(), props);
        }
        props.add(property);
        Collections.sort(props);
    }

    private void initDatatypesDefinedViaExtension() {
        datatypes = new HashMap();
        datatypeHelpersMap = new HashMap();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "datatypeDefinition"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        // first, get all datatypes defined by the ips-plugin itself
        // to get them at top of the list...
        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i].getNamespaceIdentifier().equals(IpsPlugin.PLUGIN_ID)) {
                createDatatypeDefinition(extensions[i]);
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
                String text = "Illegal datatype definition " //$NON-NLS-1$
                        + extension.getUniqueIdentifier() + ". Expected Config Element <datatypeDefinition> was " //$NON-NLS-1$
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

    /**
     * {@inheritDoc}
     */
    public ValueDatatype[] getPredefinedValueDatatypes() {
        if (datatypes == null) {
            this.initDatatypesDefinedViaExtension();
        }
        Collection c = datatypes.values();
        return (ValueDatatype[])c.toArray(new ValueDatatype[c.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPredefinedValueDatatype(String valueDatatypeId) {
        if (datatypes == null) {
            this.initDatatypesDefinedViaExtension();
        }
        return datatypes.containsKey(valueDatatypeId);
    }

    /**
     * {@inheritDoc}
     */
    public void delete(IIpsElement toDelete) {
        if (toDelete instanceof IIpsObjectPart) {
            ((IIpsObjectPart)toDelete).delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention(String id) {

        initChangesOverTimeNamingConventionIfNecessary();
        IChangesOverTimeNamingConvention convention = (IChangesOverTimeNamingConvention)changesOverTimeNamingConventionMap
                .get(id);
        if (convention != null) {
            return convention;
        }
        convention = (IChangesOverTimeNamingConvention)changesOverTimeNamingConventionMap
                .get(IChangesOverTimeNamingConvention.VAA);
        if (convention != null) {
            IpsPlugin.log(new IpsStatus(IpsStatus.WARNING, "Unknown changes in time naming convention " + id //$NON-NLS-1$
                    + ". Using default " //$NON-NLS-1$
                    + IChangesOverTimeNamingConvention.VAA, null));
            return convention;
        }
        IpsPlugin.log(new IpsStatus("Unknown changes in time naming convention " + id //$NON-NLS-1$
                + ". Default convention " //$NON-NLS-1$
                + IChangesOverTimeNamingConvention.VAA + " not found!")); //$NON-NLS-1$
        return new ChangesOverTimeNamingConvention("VAA"); //$NON-NLS-1$
    }

    public IChangesOverTimeNamingConvention[] getChangesOverTimeNamingConvention() {
        initChangesOverTimeNamingConventionIfNecessary();
        IChangesOverTimeNamingConvention[] conventions = new IChangesOverTimeNamingConvention[changesOverTimeNamingConventionMap
                .size()];
        int i = 0;
        for (Iterator it = changesOverTimeNamingConventionMap.values().iterator(); it.hasNext();) {
            conventions[i++] = (IChangesOverTimeNamingConvention)it.next();
        }
        return conventions;
    }

    private void initChangesOverTimeNamingConventionIfNecessary() {
        if (changesOverTimeNamingConventionMap == null) {
            changesOverTimeNamingConventionMap = new HashMap();
            IChangesOverTimeNamingConvention vaa = new ChangesOverTimeNamingConvention(
                    IChangesOverTimeNamingConvention.VAA);
            changesOverTimeNamingConventionMap.put(vaa.getId(), vaa);
            IChangesOverTimeNamingConvention pm = new ChangesOverTimeNamingConvention(
                    IChangesOverTimeNamingConvention.PM);
            changesOverTimeNamingConventionMap.put(pm.getId(), pm);
        }
    }

    /**
     * Returns the ClassLoaderProvider for the given ips project.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public ClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        ClassLoaderProvider provider = (ClassLoaderProvider)classLoaderProviderMap.get(ipsProject);
        if (provider == null) {
            // create a new classloader provider, make sure that the jars (inside the provided
            // classloader) will be copied, this fixed the problem if the classloader is used
            // to load classes for DynamicValueDatatype
            provider = new ClassLoaderProvider(ipsProject.getJavaProject(), ipsProject.getReadOnlyProperties()
                    .isJavaProjectContainsClassesForDynamicDatatypes(), true);
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
        if (file == null) {
            return null;
        }
        IResource enclResource = file.getEnclosingResource();
        if (enclResource == null || !enclResource.exists()) {
            return null;
        }
        IpsSrcFileContent content = (IpsSrcFileContent)ipsObjectsMap.get(file);
        long resourceModStamp = enclResource.getModificationStamp();

        // new content
        if (content == null) {
            content = readContentFromFile(file, loadCompleteContent);
            ipsObjectsMap.put(file, content);
            return content;
        }

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
        IpsSrcFileContent content = (IpsSrcFileContent)ipsObjectsMap.get(file);
        if (content == null) {
            return true;
        }
        return content.getModificationStamp() == enclResource.getModificationStamp();
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

    public void ipsSrcFileModificationStatusHasChanged(ContentChangeEvent event) {
        notifyChangeListeners(event);
    }

    /**
     * ResourceDeltaVisitor to generate ips model change events.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        private Set fileExtensionsOfInterest = new HashSet(20);

        public ResourceDeltaVisitor() {
            IpsObjectType[] types = getIpsObjectTypes();
            for (int i = 0; i < types.length; i++) {
                fileExtensionsOfInterest.add(types[i].getFileExtension());
            }
            fileExtensionsOfInterest.add(IpsProject.PROPERTY_FILE_EXTENSION);
        }

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
                                    + resource.getModificationStamp() + ", Sync status=" + isInSync //$NON-NLS-1$
                                    + ", " //$NON-NLS-1$
                                    + srcFile + " Thread: " //$NON-NLS-1$
                                    + Thread.currentThread().getName());
                }
                if (!isInSync) {
                    ipsSrcFileContentHasChanged(ContentChangeEvent.newWholeContentChangedEvent(srcFile));
                }
                return true;
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model objects after resource " //$NON-NLS-1$
                        + resource + " changed.", e)); //$NON-NLS-1$
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out
                            .println("IpsModel.ResourceDeltaVisitor.visit(): Error updating model objects after resource changed, resource=" //$NON-NLS-1$
                                    + resource);
                }
            }
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearValidationCache() {
        getValidationResultCache().removeStaleData(null);
    }

    private void createIpsArtefactBuilderSetInfosIfNecessary() {
        if (builderSetInfoList == null) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            builderSetInfoList = new ArrayList();
            IpsArtefactBuilderSetInfo.loadExtensions(registry, IpsPlugin.getDefault().getLog(), builderSetInfoList,
                    this);
        }
    }

    /**
     * Returns an array of IpsArtefactBuilderSetInfo objects. Each IpsArtefactBuilderSetInfo object
     * represents an IpsArtefactBuilderSet that is a registered at the corresponding extension
     * point.
     */
    public IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos() {
        createIpsArtefactBuilderSetInfosIfNecessary();
        return (IIpsArtefactBuilderSetInfo[])builderSetInfoList
                .toArray(new IIpsArtefactBuilderSetInfo[builderSetInfoList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id) {
        createIpsArtefactBuilderSetInfosIfNecessary();
        for (Iterator it = builderSetInfoList.iterator(); it.hasNext();) {
            IIpsArtefactBuilderSetInfo builderSetInfo = (IIpsArtefactBuilderSetInfo)it.next();
            if (builderSetInfo.getBuilderSetId().equals(id)) {
                return builderSetInfo;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType[] getIpsObjectTypes() {
        return ipsObjectTypes;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType(String name) {
        for (int i = 0; i < ipsObjectTypes.length; i++) {
            if (ipsObjectTypes[i].getId().equals(name)) {
                return ipsObjectTypes[i];
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectTypeByFileExtension(String fileExtension) {
        for (int i = 0; i < ipsObjectTypes.length; i++) {
            if (ipsObjectTypes[i].getFileExtension().equals(fileExtension)) {
                return ipsObjectTypes[i];
            }
        }
        return null;
    }

    private static void logTraceMessage(String text, IIpsSrcFile ipsSrcFile) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            IResource enclosingResource = ipsSrcFile.getEnclosingResource();
            System.out
                    .println(NLS
                            .bind(
                                    "IpsModel.getIpsSrcFileContent(): {0}, file={1}, FileModStamp={2}, Thread={3}", //$NON-NLS-1$
                                    new String[] {
                                            text,
                                            "" + ipsSrcFile, "" + enclosingResource.getModificationStamp(), Thread.currentThread().getName() })); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Add the key/value pair to the cache: key = IIpsPackageFragment; value =
     * IIpsPackageFragmentSortDefinition
     * 
     * @param fragment Key of the hash table entry. The fragment is part of the sortDefinition.
     * @param sortDefinition Value of the hash table entry.
     * @param lastModification
     */
    private void addSortDefinition(IIpsPackageFragment fragment,
            IIpsPackageFragmentSortDefinition sortDefinition,
            Long lastModification) {
        sortOrderCache.put(fragment, sortDefinition);
        lastIpSortOrderModifications.put(sortDefinition, lastModification);
    }

    /**
     * Get a IIpsPackageFragmentSortDefinition for a given IIpsPackageFragment. Returns the object
     * from the cache if the file exists and didn't change, otherwise update sort order from the
     * filesystem.
     * 
     * @param fragment Key of the hash table entry. The fragment is part of the sortDefinition.
     * @return A IIpsPackageFragmentSortDefinition implementation. THe return value should always be
     *         not <code>null</code>.
     */
    public IIpsPackageFragmentSortDefinition getSortDefinition(IIpsPackageFragment fragment) {

        // SortDefinitions are cached in IpsModel
        IIpsPackageFragmentSortDefinition sortDef = (IIpsPackageFragmentSortDefinition)sortOrderCache.get(fragment);

        if (sortDef != null) {
            // sortdefinition cached, check modification
            IFile file = fragment.getSortOrderFile();

            if (file != null && file.exists()) {
                Long lastModification = (Long)lastIpSortOrderModifications.get(sortDef);

                if (!lastModification.equals(new Long(file.getModificationStamp()))) {
                    // update current fragment
                    sortOrderCache.remove(fragment);
                    lastIpSortOrderModifications.remove(sortDef);
                    sortDef = null;
                }
            } else {
                // remove deleted sort orders
                // TODO Distinguish between DefaultSortOrder and deleted files!.
                sortOrderCache.remove(fragment);
                lastIpSortOrderModifications.remove(sortDef);
                sortDef = null;
            }
        }

        if (sortDef == null) {
            // add new or updated sort order
            IFile file = fragment.getSortOrderFile();
            Long lastModification;

            try {
                if (file != null && file.exists()) {
                    sortDef = ((IpsPackageFragment)fragment).loadSortDefinition();
                }

                // use default sort order if no sort definition is set.
                if (sortDef == null) {
                    sortDef = new IpsPackageFragmentDefaultSortDefinition();
                    lastModification = new Long(0);
                } else {
                    lastModification = new Long(file.getModificationStamp());
                }
            } catch (CoreException e) {
                sortDef = new IpsPackageFragmentDefaultSortDefinition();
                lastModification = new Long(0);
                IpsPlugin.log(e);
            }

            this.addSortDefinition(fragment, sortDef, lastModification);
        }

        return sortDef;
    }
}

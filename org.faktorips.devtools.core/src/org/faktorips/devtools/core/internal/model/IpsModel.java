package org.faktorips.devtools.core.internal.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.GenericValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;

/**
 * Implementation of IpsModel.
 */
public class IpsModel extends IpsElement implements IIpsModel, IResourceChangeListener {

    private List changeListeners;
    
    /*
     * A map containing the dataypes (value) by id (key).
     */
    private Map datatypes = null; // lazy load

    /*
     * A map containing a code generation helper (value) per datatype (key)
     */
    private Map datatypeHelpersMap = null;

    /*
     * A map containing the data for each ips project. The name of the project is used as the key and 
     * the value is an instance of IpsProjectData.
     */
    private Map projectPropertiesMap = new HashMap();
    
    // a map containing a set of datatypes per ips project. The map's key is the project name.
    private HashMap projectDatatypesMap = new HashMap();

    // a map containing a map per ips project. The map's key is the project name.
    // The maps contained in the map, contain the datatype as keys and the datatype helper as
    // values.
    private HashMap projectDatatypeHelpersMap = new HashMap();

    // a map that contains the table of contents (value) for each ips package fragment root's (key)
    // product component
    // registry.
    // The map is maintained in the model implementation so that the contents is cached as there is
    // only one model
    // instance.
    private Map packFrgmtRootRegistryTocMap = new HashMap();

    // the artefact builder sets that are registered with the artefact builder set extension point
    private List availableBuilderSets;
    
    // extension properties (as list) per ips object (or part) type, e.g. IAttribute.
    private Map typeExtensionPropertiesMap = null; // null as long as they haven't been looked up.
    

    IpsModel() {
        super(null, "IpsModel");
    }

    public void startListeningToResourceChanges() {
        getWorkspace().addResourceChangeListener(this);
    }

    public void stopListeningToResourceChanges() {
        getWorkspace().removeResourceChangeListener(this);
    }

    /**
     * Overridden.
     */
    public IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Overridden.
     */
    public IIpsProject[] getIpsProjects() throws CoreException {

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IIpsProject[] pdProjects = new IIpsProject[projects.length];
        int counter = 0;
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].hasNature(IIpsProject.NATURE_ID)) {
                pdProjects[i] = getIpsProject(projects[i].getName());
                counter++;
            }
        }
        if (counter == pdProjects.length) {
            return pdProjects;
        }
        IIpsProject[] shrinked = new IIpsProject[counter];
        System.arraycopy(pdProjects, 0, shrinked, 0, shrinked.length);
        return shrinked;
    }

    /**
     * Overridden.
     */
    public IIpsModel getIpsModel() {
        return this;
    }

    /**
     * Returns the product component registry's table of contents for the indicated ips package
     * fragment root.
     * 
     * @throws CoreException if an error occurs while accessing the toc file.
     */
    public MutableClRuntimeRepositoryToc getRuntimeRepositoryToc(IpsPackageFragmentRoot root)
            throws CoreException {
        MutableClRuntimeRepositoryToc toc = (MutableClRuntimeRepositoryToc)packFrgmtRootRegistryTocMap
                .get(root);
        if (toc == null) {
            toc = new MutableClRuntimeRepositoryToc();
            IFile tocFile = root.getTocFileInOutputFolder();
            if (tocFile.exists()) {
                InputStream is = tocFile.getContents(true);
                DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
                try {
                    Document doc = builder.parse(is);
                    toc.initFromXml(doc.getDocumentElement(), getClass().getClassLoader());
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus("Error parsing toc contents.", e));
                }
            }
            packFrgmtRootRegistryTocMap.put(root, toc);
        }
        return toc;
    }

    /**
     * Overridden.
     */
    public IIpsProject getIpsProject(String name) {
        return new IpsProject(this, name);
    }

    /**
     * Overridden.
     */
    public IIpsProject getIpsProject(IProject project) {
        return new IpsProject(this, project.getName());
    }

    /**
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsModel.gif");
    }

    /**
     * Returns the workspace root. Overridden method.
     */
    public IResource getCorrespondingResource() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Overridden.
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsProjects();
    }

    /**
     * Overridden.
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
        IIpsPackageFragment pdFolder = root.getIpsPackageFragment(folderName.toString());
        return pdFolder.getIpsSrcFile(resource.getName());
    }

    /**
     * Overridden.
     */
    public void addChangeListener(ContentsChangeListener listener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(1);
        }
        changeListeners.add(listener);
    }

    /**
     * Overridden.
     */
    public void removeChangeListener(ContentsChangeListener listener) {
        if (changeListeners != null) {
            changeListeners.remove(listener);
        }
    }

    void notifyChangeListeners(final ContentChangeEvent event) {
        if (changeListeners == null) {
            return;
        }
        Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                for (Iterator it = changeListeners.iterator(); it.hasNext();) {
                    try {
                        ContentsChangeListener listener = (ContentsChangeListener)it.next();
                        listener.contentsChanged(event);
                    } catch (Exception e) {
                        IpsPlugin.log(new IpsStatus("Error notifying IPS model change listener", e));
                    }
                }
            }
        });
    }

    /**
     * Overridden.
     */
    public boolean equals(Object o) {
        return o instanceof IIpsModel;
    }

    /**
     * Overridden.
     */
    public String toString() {
        return "IpsModel";
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsModel#getAllSourcePckFragmentRoots()
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        IIpsProject[] projects = getIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            ((IpsProject)projects[i]).getSourcePdPckFragmentRoots(result);
        }
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsModel#findIpsSrcFile(org.eclipse.jdt.core.ICompilationUnit)
     */
    public IIpsSrcFile findIpsSrcFile(ICompilationUnit cu) throws CoreException {
        IIpsProject ipsProject = this.getIpsProject(cu.getJavaProject().getProject());
        if (ipsProject != null && ipsProject.exists()) {
            return ipsProject.findIpsSrcFile(cu);
        }
        return null;
    }

    /**
     * Adds the value datatypes defined for the IPS project to the set of datatypes.
     */
    public void getValueDatatypes(IIpsProject ipsProject, Set datatypes) {
        Set set = (Set)projectDatatypesMap.get(ipsProject.getName());
        if (set == null) {
            getDatatypes(ipsProject);
            set = (Set)projectDatatypesMap.get(ipsProject.getName());
        }
        datatypes.addAll(set);
        return;
    }

    public IIpsArtefactBuilderSet getCurrentIpsArtefactBuilderSet(IIpsProject project)
            throws CoreException {

        ArgumentCheck.notNull(project, this);
        IpsProjectProperties data = getIpsProjectProperties((IpsProject)project);
        return getIpsArtefactBuilderSet(data.getBuilderSetId());
    }

    private IIpsArtefactBuilderSet getIpsArtefactBuilderSet(String id) {
        IIpsArtefactBuilderSet[] sets = getAvailableArtefactBuilderSets();
        for (int i = 0; i < sets.length; i++) {
            if (sets[i].getId().equals(id)) {
                return sets[i];
            }
        }
        return null;
    }

    /**
     * Returns the datatype helper for the given value datatype or <code>null</code> if no helper
     * is defined for the value datatype.
     */
    public DatatypeHelper getDatatypeHelper(IIpsProject ipsProject, ValueDatatype datatype) {
        Map map = (Map)projectDatatypeHelpersMap.get(ipsProject.getName());
        if (map == null) {
            getDatatypes(ipsProject);
            map = (Map)projectDatatypeHelpersMap.get(ipsProject.getName());
        }
        return (DatatypeHelper)map.get(datatype);
    }
    
    /**
     * Returns the properties (stored in the .ipsproject file) for the given ips project.
     * If an error occurs while accessing the .ipsproject file or the file does not exist
     * an error is logged and an empty ips project data instance is returned.
     */
    public IpsProjectProperties getIpsProjectProperties(IpsProject ipsProject) {
    	if (projectPropertiesMap==null) {
    		projectPropertiesMap = new HashMap();
    	}
    	IpsProjectProperties data = (IpsProjectProperties)projectPropertiesMap.get(ipsProject.getName()); 
    	if (data==null) {
        	data = readProjectData(ipsProject);
    		projectPropertiesMap.put(ipsProject.getName(), data);
    	}
		return data;
    }

    /**
     * Reads the project's data from the .ipsproject file.
     */
    private IpsProjectProperties readProjectData(IpsProject ipsProject) {
    	IFile file = ipsProject.getIpsProjectPropertiesFile();
    	IpsProjectProperties data = new IpsProjectProperties();
    	if (!file.exists()) {
    		return data;
    	}
    	Document doc;
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (CoreException e1) {
            IpsPlugin.log(new IpsStatus("Error reading project file contents " + file, e1));
            return data;
        }
        try {
            doc = IpsPlugin.getDefault().newDocumentBuilder().parse(is);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e));
            return data;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus(
                        "Error closing input stream after reading project file " + file, e));
                return data;
            }
        }
        return IpsProjectProperties.createFromXml(ipsProject, doc.getDocumentElement());
    }

    /*
     * Intializes the datatypes and their helpers for the project.
     */
    private void getDatatypes(IIpsProject project) {
        if (datatypes==null) {
        	initDatatypeDefintionsFromConfiguration();
        }
        Set types = new HashSet();
        Map helperMap = new HashMap();
        projectDatatypesMap.put(project.getName(), types);
        projectDatatypeHelpersMap.put(project.getName(), helperMap);

        IpsProjectProperties props = getIpsProjectProperties((IpsProject)project);
        String[] datatypeIds = props.getPredefinedDatatypesUsed();
        for (int i = 0; i < datatypeIds.length; i++) {
            Datatype datatype = (Datatype)datatypes.get(datatypeIds[i]);
            if (datatype==null) {
                return;
            }
            types.add(datatype);
            DatatypeHelper helper = (DatatypeHelper)datatypeHelpersMap.get(datatype);
            if (helper!=null) {
                helperMap.put(datatype, helper);
            }
        }
        DynamicValueDatatype[] dynamicTypes = props.getDefinedDatatypes();
        for (int i = 0; i < dynamicTypes.length; i++) {
            types.add(dynamicTypes[i]);
            helperMap.put(dynamicTypes[i], new GenericValueDatatypeHelper(dynamicTypes[i]));
        }
    }

    /**
     * Overridden method.
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                delta.accept(new ResourceDeltaVisitor());
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus(
                        "Error updating model objects in resurce changed event.", e));
            }
        }
    }

    private void retrieveRegisteredArteFactBuilderSets() {

        availableBuilderSets = new ArrayList();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "artefactbuilderset");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            if (configElements.length > 0) {
                IConfigurationElement element = configElements[0];
                if (element.getName().equals("builderSet")) {
                    Object builderInstance = null;
                    try {
                        builderInstance = element.createExecutableExtension("class");
                    } catch (CoreException e) {
                        IpsPlugin.log(new IpsStatus("Unable to create the artefact builder set: "
                                + element.getAttribute("qualifiedClassName"), e));
                        // TODO: qualifiedClassName korrekt?
                        continue;
                    }
                    if (!(builderInstance instanceof IIpsArtefactBuilderSet)) {
                        IpsPlugin.log(new IpsStatus("The class that has been registered for the "
                                + "artefact builder set doesn't implement the "
                                + IIpsArtefactBuilderSet.class + " interface: "
                                + extension.getUniqueIdentifier()));
                        continue;
                    }

                    IIpsArtefactBuilderSet arteFactBuilderSet = (IIpsArtefactBuilderSet)builderInstance;
                    arteFactBuilderSet.setId(extension.getUniqueIdentifier());
                    arteFactBuilderSet
                            .setLabel(StringUtils.isEmpty(extension.getLabel()) ? extension
                                    .getUniqueIdentifier() : extension.getLabel());
                    try {
                        arteFactBuilderSet.initialize();
                    } catch (CoreException e) {
                        IpsPlugin.log(new IpsStatus(
                                "An exception occured while trying to initialize"
                                        + " the artefact builder set: "
                                        + extension.getUniqueIdentifier(), e));
                        continue;
                    }
                    availableBuilderSets.add(arteFactBuilderSet);
                }
            }
        }
    }

    /**
     * Returns the artefact builder sets that have been registered at the according extension point
     * of this plugin or have been explicitly set via the setAvailableArtefactBuilderSets() method. 
     * The extensions are reloaded as long as the availableArtefactBuilderSets property of this class
     * doesn't have a value. A reload of the extensions can be enforced by setting the property to
     * null via the setAvailableArtefactBuilderSets() method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsModel#getArtefactBuilderSets()
     */
    public IIpsArtefactBuilderSet[] getAvailableArtefactBuilderSets() {
        if (availableBuilderSets == null) {
            retrieveRegisteredArteFactBuilderSets();
        }
        return (IIpsArtefactBuilderSet[])availableBuilderSets
                .toArray(new IIpsArtefactBuilderSet[availableBuilderSets.size()]);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsModel#setAvailableArtefactBuilderSets(org.faktorips.devtools.core.model.IIpsArtefactBuilderSet[])
     */
    public void setAvailableArtefactBuilderSets(IIpsArtefactBuilderSet[] sets){
        if(sets == null) {
            availableBuilderSets = null;
            return;
        }
        availableBuilderSets = new ArrayList(sets.length);
        for (int i = 0; i < sets.length; i++) {
            availableBuilderSets.add(sets[i]);
        }
    }
    
    /**
     * ResourceDeltaVisitor to update any model objects.
     */

    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) {
            IResource resource = delta.getResource();
            try {
                if (resource == null || resource.getType() != IResource.FILE) {
                    return true;
                }
                IIpsProject ipsProject = getIpsProject(resource.getProject());
                if (checkProjectPropertiesFileModification(ipsProject, resource)) {
                    return false;
                }
                if (checkTocFileModifications(ipsProject, resource)) {
                    return false;
                }
                IIpsElement element = getIpsElement(resource);
                if (element == null) {
                    return true;
                }
                if (!(element instanceof IIpsSrcFile)) {
                    return true;
                }
                IIpsSrcFile srcFile = (IIpsSrcFile)element;
                IpsPlugin.getDefault().getManager().removeSrcFileContents(srcFile);
                return true;
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model objects after resource "
                        + resource + " changed.", e));
            }
            return true;
        }
    }

    /*
     * Checks if project's properties file was changed. Removes data from cache and returns
     * true, otherwise false.
     */
    private boolean checkProjectPropertiesFileModification(IIpsProject ipsProject,
            IResource resource) {
        if (resource.equals(((IpsProject)ipsProject).getIpsProjectPropertiesFile())) {
            projectDatatypesMap.remove(ipsProject.getName());
            projectDatatypeHelpersMap.remove(ipsProject.getName());
            projectPropertiesMap.remove(ipsProject.getName());
            return true;
        }
        return false;
    }

    /*
     * Checks if the changed resource is a toc file. If so, removes it from cache and returns true,
     * otherwise false.
     */
    private boolean checkTocFileModifications(IIpsProject ipsProject, IResource resource)
            throws CoreException {
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < srcRoots.length; i++) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoots[i];
            if (resource.equals(root.getTocFileInOutputFolder())) {
                packFrgmtRootRegistryTocMap.remove(root);
                return true;
            }
        }
        return false;
    }

    /**
     * Overridden.
     */
    public ExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class type, boolean includeSupertypesAndInterfaces) {
        if (typeExtensionPropertiesMap == null) {
            initExtensionPropertiesFromConfiguration();
        }
        ArrayList result = new ArrayList();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, result);
        return (ExtensionPropertyDefinition[])result.toArray(new ExtensionPropertyDefinition[result.size()]);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsModel#getExtensionPropertyDefinition(java.lang.Class, java.lang.String, boolean)
     */
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(
            Class type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {
        List props = new ArrayList();
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
    private void getIpsObjectExtensionProperties(Class type, boolean includeSupertypesAndInterfaces, List result) {
        List props = (List)typeExtensionPropertiesMap.get(type);
        if (props!=null) {
            result.addAll(props);
        }
        if (!includeSupertypesAndInterfaces) {
            return;
        }
        if (type.getSuperclass()!=null) {
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
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "objectExtensionProperty");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {
            IExtensionPropertyDefinition property = createExtensionProperty(extensions[i]);
            if (property!=null) {
                List props = (ArrayList)typeExtensionPropertiesMap.get(property.getExtendedType());
                if (props==null) {
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
        if (configElements.length != 1 || !configElements[0].getName().equalsIgnoreCase("property")) {
            IpsPlugin.log(new IpsStatus("Illegal definition of external property " + extension.getUniqueIdentifier()));
            return null;
        } 
        IConfigurationElement element = configElements[0];
        Object propertyInstance = null;
        try {
            propertyInstance = element.createExecutableExtension("class");
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " + extension.getUniqueIdentifier()
                    + ". Reason: Can't instantiate "+ element.getAttribute("class"), e));
            return null;
        }
        if (!(propertyInstance instanceof ExtensionPropertyDefinition)) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " + extension.getUniqueIdentifier()
                    + element.getAttribute("class") + " does not derived from " + ExtensionPropertyDefinition.class));
            return null;
        }
        ExtensionPropertyDefinition extProperty = (ExtensionPropertyDefinition)propertyInstance;
        extProperty.setPropertyId(extension.getUniqueIdentifier());
        extProperty.setDisplayName(extension.getLabel());
        extProperty.setDefaultValue(element.getAttribute("defaultValue"));
        if (StringUtils.isNotEmpty(element.getAttribute("sortOrder"))) {
            extProperty.setSortOrder(Integer.parseInt(element.getAttribute("sortOrder")));
        }
        String extType = element.getAttribute("extendedType");
        try {
            extProperty.setExtendedType(extProperty.getClass().getClassLoader().loadClass(extType));
        } catch (ClassNotFoundException e) {
            IpsPlugin.log(new IpsStatus("Extended type " + extType + " not found for extension property " + extProperty.getPropertyId(), e));
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
     * Adds the extension property. For testing purposes. During normal execution
     * the available extension properties are discovered by extension point lookup. 
     */
    public void addIpsObjectExtensionProperty(IExtensionPropertyDefinition property) {
        if (typeExtensionPropertiesMap == null) {
            typeExtensionPropertiesMap = new HashMap();
        }
        List props = (List)typeExtensionPropertiesMap.get(property.getExtendedType());
        if (props==null) {
            props = new ArrayList();
            typeExtensionPropertiesMap.put(property.getExtendedType(), props);
        }
        props.add(property);
        Collections.sort(props);
    }

    private void initDatatypeDefintionsFromConfiguration() {
    	datatypes = new HashMap();
    	datatypeHelpersMap = new HashMap(); 
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID,"datatypeDefinition");
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            createDatatypeDefinition(extensions[i]);
        }
    }
    
    private void createDatatypeDefinition(IExtension extension) {
    	IConfigurationElement[] configElements = extension.getConfigurationElements();
    	for (int i = 0; i < configElements.length; i++) {
            if (!configElements[i].getName().equalsIgnoreCase("datatypeDefinition")) {
            	String text = "Illegal datatype definition " + extension.getUniqueIdentifier()
            		+ ". Expected Config Element <datatypeDefinition> was " + configElements[i].getName();  
                IpsPlugin.log(new IpsStatus(text));
                continue;
            } 
            Object datatypeObj = createExecutableExtension(extension, configElements[i], "datatypeClass", Datatype.class);
            if (datatypeObj==null) {
            	continue;
            }
            Datatype datatype = (Datatype)datatypeObj;
            datatypes.put(datatype.getQualifiedName(), datatype);
            Object dtHelperObj = createExecutableExtension(extension, configElements[i], "helperClass", DatatypeHelper.class);
            if (dtHelperObj==null) {
            	continue;
            }
            DatatypeHelper dtHelper = (DatatypeHelper)dtHelperObj;
            dtHelper.setDatatype(datatype);
            datatypeHelpersMap.put(datatype, dtHelper);
		}
    }
    
    /**
     * Adds the datatype helper and it's datatype to the available once. For testing purposes. During normal execution
     * the available datatypes are discovered by extension point lookup. 
     */
    public void addDatatypeHelper(DatatypeHelper helper) {
    	Datatype datatype = helper.getDatatype();
    	datatypes.put(datatype.getQualifiedName(), datatype);
        datatypeHelpersMap.put(helper.getDatatype(), helper);
    }
    
    /*
     * Wrapper around IConfigurationElement.createExecutableExtension(propertyName) with 
     * detaied logging. If the exectuable extension couldn't be created, the reason is logged,
     * no exception is thrown. The returned object is of the expected type. 
     */
    private Object createExecutableExtension(
    		IExtension extension, 
    		IConfigurationElement element, 
    		String propertyName, 
    		Class expectedType) {
        Object object = null;
        try {
        	object = element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Unable to create extension " + extension.getUniqueIdentifier()
                    + ". Reason: Can't instantiate "+ element.getAttribute(propertyName), e));
            return null;
        }
        if (!(expectedType.isAssignableFrom(object.getClass()))) {
            IpsPlugin.log(new IpsStatus("Unable to create extension " + extension.getUniqueIdentifier()
                    + "Reason: " + element.getAttribute(propertyName) + " is not of type " + expectedType));
            return null;
        }
    	return object;
    }
    
}
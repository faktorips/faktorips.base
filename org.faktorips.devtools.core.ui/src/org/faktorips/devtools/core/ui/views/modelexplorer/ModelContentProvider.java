/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.abstraction.AAbstraction;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.ArgumentCheck;

/**
 * Content provider for the model explorer tree. The returned lists of package fragments are
 * dependent on the current layout style indicated by the <code>isFlatLayout</code> flag.
 */
public class ModelContentProvider implements ITreeContentProvider {

    protected static final Object[] EMPTY_ARRAY = new Object[0];

    private LayoutStyle layoutStyle;

    private ModelExplorerConfiguration configuration;

    private boolean excludeNoIpsProjects;

    private IpsProjectChildrenProvider projectChildrenProvider = new IpsProjectChildrenProvider();
    private IpsObjectPathContainerChildrenProvider ipsObjectPathContainerChildrenProvider = new IpsObjectPathContainerChildrenProvider();

    /**
     * Constructs a new <code>ModelContentProvider</code> using the given configuration and the
     * given layout style.
     */
    public ModelContentProvider(ModelExplorerConfiguration config, LayoutStyle layoutStyle) {
        ArgumentCheck.notNull(layoutStyle);
        configuration = config;
        this.layoutStyle = layoutStyle;
    }

    public LayoutStyle getLayoutStyle() {
        return layoutStyle;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return filter(getUnfilteredChildren(parentElement));
    }

    /**
     * Returns the array of children of the given parentElement without filtering out children of a
     * specific type or with a specific name.
     */
    // CSOFF: CyclomaticComplexity
    protected Object[] getUnfilteredChildren(Object parentElement) {
        if (parentElement instanceof IIpsElement) {
            if (parentElement instanceof IPolicyCmptTypeAttribute) {
                return EMPTY_ARRAY;
            }
            if (parentElement instanceof IEnumAttribute) {
                return EMPTY_ARRAY;
            }

            try {
                if (parentElement instanceof IIpsProject) {
                    return getProjectContent((IIpsProject)parentElement);
                } else if (parentElement instanceof IIpsPackageFragmentRoot) {
                    return getPackageFragmentRootContent((IIpsPackageFragmentRoot)parentElement);
                } else if (parentElement instanceof IIpsPackageFragment) {
                    return getPackageFragmentContent((IIpsPackageFragment)parentElement);
                } else if (parentElement instanceof IIpsSrcFile) {
                    if (configuration.shouldDisplayChildrenFor(((IIpsSrcFile)parentElement).getIpsObjectType())) {
                        IIpsObject ipsObject = ((IIpsSrcFile)parentElement).getIpsObject();
                        return getChildren(ipsObject);
                    } else {
                        return EMPTY_ARRAY;
                    }
                } else {
                    return ((IIpsElement)parentElement).getChildren();
                }
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        } else if (parentElement instanceof AResource) {
            return getUnfilteredChildren(((AResource)parentElement).unwrap());
        } else if (parentElement instanceof IResource) {
            if (parentElement instanceof IAdaptable) {
                IWorkbenchAdapter adapter = ((IAdaptable)parentElement)
                        .getAdapter(IWorkbenchAdapter.class);

                if (adapter != null) {
                    // filter out java classpath entries and outputlocations
                    // (used for folders in IpsProjects)
                    if (parentElement instanceof IFolder) {
                        return getNonJavaResourcesAndNonActiveIpsArchives((IFolder)parentElement);
                    }
                }
            }
        } else if (parentElement instanceof IIpsObjectPathContainer) {
            try {
                return ipsObjectPathContainerChildrenProvider.getChildren((IIpsObjectPathContainer)parentElement);
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        }

        return EMPTY_ARRAY;
    }
    // CSON: CyclomaticComplexity

    /**
     * <p>
     * Returns an array containing all (non-IPS) folders in the given project and all package
     * fragment roots that exist as folders in the filesystem.
     * </p>
     * <p>
     * When calling <code>IpsProject#getIpsPackageFragmentRoots()</code> the project retrieves all
     * entries from the IpsObjectPath and returns them as handles without checking if the underlying
     * resources actually exist. Thus the filtering of package fragment roots is necessary to
     * prevent these handles from being displayed in the tree.
     * </p>
     * <p>
     * This problem does not occur with <code>IResource</code>s.
     * </p>
     */
    private Object[] getProjectContent(IIpsProject project) {
        return projectChildrenProvider.getChildren(project);
    }

    /**
     * Returns the resources in the given folder. Filters the resources using the javaproject's
     * classpath entries and output locations the folder is contained in. Java Resources and IPS
     * archives will not be returned.
     * <p>
     * If the given folder does not contain any children an empty array is returned. If the
     * <code>IProject</code> returned by <code>IResource#getProject()</code> is null or does not
     * have java nature, an empty array is returned.
     */
    private Object[] getNonJavaResourcesAndNonActiveIpsArchives(IFolder folder) {
        try {

            IProject project = folder.getProject();
            IResource[] children = folder.members();
            if (project == null) {
                return EMPTY_ARRAY;
            }
            if (!project.hasNature(JavaCore.NATURE_ID)) {
                return EMPTY_ARRAY;
            }
            IJavaProject javaProject = JavaCore.create(project);
            // javaProject.open(null);

            List<IResource> childResources = new ArrayList<>();
            for (IResource child : children) {
                if (isIpsArchiveFromIpsObjectPath(project, child)) {
                    // filter out ips archive files which are specified in the ipsobjectpath
                    continue;
                }
                if (!isJavaResource(javaProject, child)) {
                    childResources.add(child);
                }
            }

            return childResources.toArray(new IResource[childResources.size()]);

        } catch (JavaModelException e) {
            IpsPlugin.log(e);
            return EMPTY_ARRAY;

        } catch (CoreException e) {
            IpsPlugin.log(e);
            return EMPTY_ARRAY;
        }
    }

    /*
     * Returns <code>true</code> if the given resource is a ips archive file which is specified in
     * the ips object path, otherwise return <code>false</code>.
     */
    private boolean isIpsArchiveFromIpsObjectPath(IProject project, IResource resource) {
        if (!(resource instanceof IFile)) {
            return false;
        }

        try {
            if (project.hasNature(IIpsProject.NATURE_ID)) {
                // check if one of the archive entries in the ips object path is the given file
                IIpsProject ipsProject = IIpsModel.get().getIpsProject(project.getName());
                IIpsArchiveEntry[] archiveEntries = ipsProject.getIpsObjectPath().getArchiveEntries();
                for (IIpsArchiveEntry archiveEntrie : archiveEntries) {
                    IPath archivePath = PathMapping.toEclipsePath(archiveEntrie.getArchiveLocation());
                    IFile archiveFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(archivePath);
                    if (resource.equals(archiveFile)) {
                        return true;
                    }
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return false;
    }

    /**
     * Examines the given <code>JavaProject</code> and its relation to the given
     * <code>IResource</code>. Returns true if the given resource corresponds to a classpath entry
     * of the java project. Returns true if the given resource corresponds to a folder that is
     * either the java projects default output location or the output location of one of the
     * project's classpath entries. False otherwise.
     */
    public boolean isJavaResource(IJavaProject jProject, IResource resource) {
        if (isJavaOutput(jProject, resource)) {
            return true;
        }
        if (jProject.isOnClasspath(resource)) {
            return true;
        }
        return false;
    }

    public boolean isJavaOutput(IJavaProject jProject, IResource resource) {
        try {
            IPath outputPath = jProject.getOutputLocation();
            if (resource.getFullPath().equals(outputPath)) {
                return true;
            }
            IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
            for (IClasspathEntry entrie : entries) {
                if (resource.getFullPath().equals(entrie.getOutputLocation())) {
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
     * In flat layout style this method returns all <code>IpsPackageFragment</code>s contained in
     * the given <code>IpsPackageFragmentRoot</code>. In hierarchical layout the
     * child-packagefragments of the defaultpackage are returned. The defaultpackage of the given
     * <code>IpsPackageFragmentRoot</code> is contained in the returned array if it contains files
     * (has children).
     */
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) {
        if (layoutStyle == LayoutStyle.FLAT) {
            IIpsPackageFragment[] fragments = root.getIpsPackageFragments();
            if (fragments.length == 1) {
                if (!hasChildren(fragments[0])) {
                    // Hide defaultpackagefragment if it is empty and there are
                    // no other packagefragments
                    return EMPTY_ARRAY;
                }
            }
            // filter out empty packagefragments if their IFolders do not contain files and at the
            // same time contain subfolders (subpackages) (this prevents empty or newly created
            // packagefragments from being hidden in the view)
            List<IIpsPackageFragment> filteredFragments = new ArrayList<>();
            for (IIpsPackageFragment fragment : fragments) {
                if (hasChildren(fragment) || fragment.getChildIpsPackageFragments().length == 0) {
                    filteredFragments.add(fragment);
                }
            }
            return filteredFragments.toArray();
        } else {
            // display the default package only if it contains files
            // display productcmptss in defaultpackage, files in root
            Object[] children = root.getDefaultIpsPackageFragment().getChildIpsPackageFragments();
            if (hasChildren(root.getDefaultIpsPackageFragment())) {
                return concatenate(new Object[] { root.getDefaultIpsPackageFragment() }, children);
            } else {
                return children;
            }
        }
    }

    /*
     * This method returns all files and <code>IpsPackageFragment</code>s contained in the given
     * <code>IpsPackageFragment</code>. If the given <code>IpsPackageFragment</code> is the
     * defaultPackageFragment of its <code>IpsPackageFragmentRoot</code>, only the contained files
     * are returned.
     * 
     * @throws IpsException
     */
    private Object[] getPackageFragmentContent(IIpsPackageFragment fragment) {
        if (fragment.isDefaultPackage()) {
            return getFileContent(fragment);
        } else {
            return concatenate(getFolderContent(fragment), getFileContent(fragment));
        }
    }

    /*
     * Returns an empty array in flat layout style. In hierarchical layout returns all
     * <code>IpsPacakgeFragment</code>s that correspond to a subfolder of the given packagefragments
     * underlying folder.
     * 
     * @throws IpsException
     */
    private Object[] getFolderContent(IIpsPackageFragment fragment) {
        // in hierarchical layout display childpackagefragments as children
        if (layoutStyle == LayoutStyle.HIERACHICAL) {
            return fragment.getChildIpsPackageFragments();
        } else {
            return EMPTY_ARRAY;
        }
    }

    /**
     * Returns all files contained in the given <code>IpsPackageFragment</code>. This includes
     * IpsElements as well as general files.
     */
    protected Object[] getFileContent(IIpsPackageFragment fragment) {
        IIpsElement[] files = fragment.getChildren();

        List<IIpsElement> pcts = new ArrayList<>();
        for (IIpsElement file2 : files) {
            if (file2 instanceof IIpsSrcFile) {
                IFile file = ((IIpsSrcFile)file2).getCorrespondingFile().unwrap();
                if (file != null && !file.isSynchronized(IResource.DEPTH_ZERO)) {
                    try {
                        file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
                    } catch (CoreException e) {
                        throw new IpsException(e);
                    }
                }
                pcts.add(file2);
            }
        }
        /*
         * Add nonIPS files after reading IpsElements. This array of objects cannot contain any
         * folders, as all folders contained in an IpsPackageFragmentRoot are interpreted as
         * IpsPackageFragments.
         */
        Object[] filesNonIps = fragment.getNonIpsResources();
        return concatenate(pcts.toArray(), filesNonIps);
    }

    /**
     * Returns a new object array containing all {@link IIpsElement IIpsElements} and
     * {@link IResource IResources} of the given array that are allowed by the configuration. Hidden
     * {@link IResource IResources} (files and folders starting with ".") and class-files are not
     * returned. An exception to this rule is the ".ipsproject"-file.
     */
    // CSOFF: CyclomaticComplexity
    private Object[] filter(Object[] elements) {
        List<Object> filtered = new ArrayList<>();

        for (Object element : elements) {
            if (element instanceof AAbstraction) {
                element = ((AAbstraction)element).unwrap();
            }

            if (element instanceof IIpsElement) {
                if (configuration.isAllowedIpsElement((IIpsElement)element)) {
                    filtered.add(element);
                }
            } else if (element instanceof IResource) {
                IResource resource = (IResource)element;

                // filter out hidden files and folders, except the ".ipsproject"-file and
                // ".sortorder"-file
                if (resource instanceof IFile | resource instanceof IFolder) {
                    if (resource.getName().indexOf(".") == 0) { //$NON-NLS-1$
                        IIpsProject project = IIpsModel.get()
                                .getIpsProject(Wrappers.wrap(resource.getProject()).as(AProject.class));

                        if ((!resource.equals(project.getIpsProjectPropertiesFile().unwrap()))
                                && resource.getName().compareTo(IIpsPackageFragment.SORT_ORDER_FILE_NAME) != 0) {
                            continue;
                        }
                    }
                }
                if (resource instanceof IFile) {
                    if (resource.getName().endsWith(".class")) { //$NON-NLS-1$
                        continue;
                    }
                }
                if (configuration.isAllowedResource(resource)) {
                    filtered.add(resource);
                }
            } else if (element instanceof IIpsObjectPathContainer) {
                filtered.add(element);
            } else if (element instanceof ReferencedIpsProjectViewItem) {
                filtered.add(element);
            }
        }

        return filtered.toArray();
    }
    // CSON: CyclomaticComplexity

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement parent;
            if (element instanceof IIpsPackageFragment) {
                // LayoutStyle#getParent() never returns the default package
                parent = layoutStyle.getParent((IIpsPackageFragment)element);
            } else {
                parent = ((IIpsElement)element).getParent();
            }

            // skip srcfiles in the object hierarchy, as in getChildren()
            if (parent != null) {
                if (parent instanceof IIpsSrcFile) {
                    parent = parent.getParent();
                }
            }

            return parent;

        } else if (element instanceof IResource) {
            /*
             * If the given element is the underlying resource of an IpsElement, the (IPS-)parent
             * must be returned; e.g. an IpsPackageFragment for a ProductCmpt. Thus
             * (IResource)element).getParent() alone is not sufficient.
             */
            IResource parentResource = ((IResource)element).getParent();
            IIpsElement parentIpsElement = IIpsModel.get()
                    .getIpsElement(Wrappers.wrap(parentResource).as(AResource.class));
            if (parentIpsElement != null) {
                return parentIpsElement;
            } else {
                return parentResource;
            }
        }

        return null;
    }

    /**
     * For IIpsElements this method returns true if the element contains at least one child, that is
     * of a type allowed by the ModelExplorerConfiguration.
     */
    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length >= 1;
    }

    /**
     * Returns all projects currently managed by the IpsModel. This includes java projects as well
     * as other types of projects. An empty array is returned if no such projects are found, or if
     * the given inputElement is not an <code>IIpsModel</code>. {@inheritDoc}
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IIpsModel) {
            try {
                IIpsModel model = (IIpsModel)inputElement;
                if (excludeNoIpsProjects) {
                    return model.getIpsProjects();
                } else {
                    // return all kind of projects (ips- and no ips projects)
                    Object[] nonIpsProjects = model.getNonIpsProjects().stream().map(p -> (IProject)p.unwrap())
                            .collect(Collectors.toList())
                            .toArray();
                    return concatenate(model.getIpsProjects(), nonIpsProjects);
                }
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        } else {
            return EMPTY_ARRAY;
        }
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }

    public void setLayoutStyle(LayoutStyle newStyle) {
        ArgumentCheck.notNull(newStyle);
        layoutStyle = newStyle;
    }

    /**
     * Concatenates arr1 and arr2 to a new <code>Object[]</code> with length
     * <code>arr1.length+arr2.length</code>.
     */
    protected static Object[] concatenate(Object[] arr1, Object[] arr2) {
        int length1 = arr1.length;
        int length2 = arr2.length;
        Object[] result = new Object[length1 + length2];
        System.arraycopy(arr1, 0, result, 0, length1);
        System.arraycopy(arr2, 0, result, length1, length2);

        return result;
    }

    /**
     * Set <code>true</code> to exlude no ips projects or <code>false</code> to show no ips
     * projects.
     */
    public void setExcludeNoIpsProjects(boolean excludeNoIpsProjects) {
        this.excludeNoIpsProjects = excludeNoIpsProjects;
    }
}

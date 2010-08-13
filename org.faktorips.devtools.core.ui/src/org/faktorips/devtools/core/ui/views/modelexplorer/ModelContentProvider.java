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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.ArgumentCheck;

/**
 * Class for calculation the content of the model explorer tree. The returned lists of package
 * fragments are dependant on the current layout style indicated by the <code>isFlatLayout</code>
 * flag.
 * 
 * @author Stefan Widmaier
 */
public class ModelContentProvider implements ITreeContentProvider {

    protected static final Object[] EMPTY_ARRAY = new Object[0];

    private LayoutStyle layoutStyle;

    private ModelExplorerConfiguration configuration;

    private boolean excludeNoIpsProjects;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        return filter(getUnfilteredChildren(parentElement));
    }

    /**
     * Returns the array of children of the given parentElement without filtering out children of a
     * specific type or with a specific name.
     */
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
                    if (IpsObjectType.TABLE_CONTENTS.equals(((IIpsSrcFile)parentElement).getIpsObjectType())) {
                        return EMPTY_ARRAY;
                    }
                    IIpsObject ipsObject = ((IIpsSrcFile)parentElement).getIpsObject();
                    return getChildren(ipsObject);
                } else {
                    return ((IIpsElement)parentElement).getChildren();
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }

        } else if (parentElement instanceof IResource) {
            if (parentElement instanceof IAdaptable) {
                IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)parentElement)
                        .getAdapter(IWorkbenchAdapter.class);

                if (adapter != null) {
                    // filter out java classpath entries and outputlocations
                    // (used for folders in IpsProjects)
                    if (parentElement instanceof IFolder) {
                        return getNonJavaResourcesAndNonActiveIpsArchives((IFolder)parentElement);
                    }
                }
            }
        }

        return EMPTY_ARRAY;
    }

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
    private Object[] getProjectContent(IIpsProject project) throws CoreException {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List<IIpsPackageFragmentRoot> existingRoots = new ArrayList<IIpsPackageFragmentRoot>();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.exists()) {
                existingRoots.add(root);
            }
        }

        Object[] result = concatenate(existingRoots.toArray(), project.getNonIpsResources());
        return result;
    }

    /*
     * Filters the contents of the given <code>IFolder</code> using the javaproject's classpath
     * entries and outputlocations the folder is contained in. If the given folder does not contain
     * any children an empty array is returned. If the <code>IProject</code> returned by
     * <code>IResource#getProject()</code> is null or does not have java nature, an empty array is
     * returned.<br> And filter ips archive files which are specified in the ips object path.
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

            List<IResource> childResources = new ArrayList<IResource>();
            for (int i = 0; i < children.length; i++) {
                if (isIpsArchiveFromIpsObjectPath(project, children[i])) {
                    // filter out ips archive files which are specified in the ipsobjectpath
                    continue;
                }
                if (!isJavaResource(javaProject, children[i])) {
                    childResources.add(children[i]);
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
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
                IIpsArchiveEntry[] archiveEntries = ipsProject.getIpsObjectPath().getArchiveEntries();
                for (IIpsArchiveEntry archiveEntrie : archiveEntries) {
                    // TODO pk archivelocation not valid for external files 25-09-2008
                    IPath archivePath = archiveEntrie.getArchivePath();
                    IFile archiveFile = ResourcesPlugin.getWorkspace().getRoot().getFile(archivePath);
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
     * Examins the given <code>JavaProject</code> and its relation to the given
     * <code>IResource</code>. Returns true if the given resource corresponds to a classpath entry
     * of the javaproject. Returns true if the given resource corresponds to a folder that is either
     * the javaprojects default output location or the output location of one of the projects
     * classpathentries. False otherwise.
     */
    public boolean isJavaResource(IJavaProject jProject, IResource resource) {
        try {
            IPath outputPath = jProject.getOutputLocation();
            IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
            if (resource.getFullPath().equals(outputPath)) {
                return true;
            }

            for (IClasspathEntry entrie : entries) {
                if (resource.getFullPath().equals(entrie.getOutputLocation())) {
                    return true;
                }
            }

            if (jProject.isOnClasspath(resource)) {
                return true;
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
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) throws CoreException {
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
            List<IIpsPackageFragment> filteredFragments = new ArrayList<IIpsPackageFragment>();
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
     * @throws CoreException
     */
    private Object[] getPackageFragmentContent(IIpsPackageFragment fragment) throws CoreException {
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
     * @throws CoreException
     */
    private Object[] getFolderContent(IIpsPackageFragment fragment) throws CoreException {
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
    protected Object[] getFileContent(IIpsPackageFragment fragment) throws CoreException {
        IIpsElement[] files = fragment.getChildren();

        List<IIpsElement> pcts = new ArrayList<IIpsElement>();
        for (IIpsElement file2 : files) {
            if (file2 instanceof IIpsSrcFile) {
                IFile file = ((IIpsSrcFile)file2).getCorrespondingFile();
                if (file != null && !file.isSynchronized(IResource.DEPTH_ZERO)) {
                    file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
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

    /*
     * Returns a new object array containig all <code>IIpsElement</code>s and
     * <code>IResource</code>s of the given array that are allowed by the configuration. Hidden
     * <code>IResource</code>s (files and folders starting with ".") and class-files are not
     * returned. An exception to this rule is the ".ipsproject"-file.
     */
    private Object[] filter(Object[] elements) {
        List<Object> filtered = new ArrayList<Object>();

        for (Object element : elements) {

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
                        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(resource.getProject());

                        if ((!resource.equals(project.getIpsProjectPropertiesFile()))
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
            }

        }

        return filtered.toArray();
    }

    /**
     * {@inheritDoc}
     */
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
            IIpsElement parentIpsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement(parentResource);
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
                    // return only ips projects and closed projects
                    return concatenate(model.getIpsProjects(), getClosedProjects(model));
                } else {
                    // return all kind of projects (ips- and no ips projects)
                    return concatenate(model.getIpsProjects(), model.getNonIpsProjects());
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        } else {
            return EMPTY_ARRAY;
        }
    }

    /*
     * Returns all closed projects or an empty array if no project is closed.
     */
    private IProject[] getClosedProjects(IIpsModel model) throws CoreException {
        IResource[] nonIpsProjects = model.getNonIpsProjects();
        List<IResource> closedProjects = new ArrayList<IResource>();
        for (int i = 0; i < nonIpsProjects.length; i++) {
            if (nonIpsProjects[i] instanceof IProject && !((IProject)nonIpsProjects[i]).isOpen()) {
                closedProjects.add(nonIpsProjects[i]);
            }
        }
        return closedProjects.toArray(new IProject[closedProjects.size()]);
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

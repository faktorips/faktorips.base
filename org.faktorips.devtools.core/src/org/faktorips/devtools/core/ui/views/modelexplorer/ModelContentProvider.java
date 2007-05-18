package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.faktorips.devtools.core.model.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * Class for calculation the content of the ModelExplorer tree. The returned Lists of
 * PackageFragments are dependant on the current layout style indicated by the
 * <code>isFlatLayout</code> flag.
 * 
 * @author Stefan Widmaier
 */
public class ModelContentProvider implements ITreeContentProvider {

    protected static final Object[] EMPTY_ARRAY = new Object[0];

    protected boolean isFlatLayout = false;

    private ModelExplorerConfiguration configuration;

    private boolean excludeNoIpsProjects;

    /**
     * Constructs a ModelContentProvider using the given Configuration and the given layout style.
     */
    public ModelContentProvider(ModelExplorerConfiguration config, boolean flatLayout) {
        configuration = config;
        isFlatLayout = flatLayout;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        return filter(getUnfilteredChildren(parentElement));
    }
    
    /**
     * Returns the array of children of the given parentElement without filtering out children of a specific type
     * or with a specific name.
     */
    protected Object[] getUnfilteredChildren(Object parentElement) {
        if (parentElement instanceof IIpsElement) {
            if (parentElement instanceof IAttribute || parentElement instanceof IProductCmpt) {
                return EMPTY_ARRAY;
            }
            try {
                if (parentElement instanceof IIpsProject) {
                    return getProjectContent((IIpsProject)parentElement);
                } else if (parentElement instanceof IIpsPackageFragmentRoot) {
                    return getPackageFragmentRootContent((IIpsPackageFragmentRoot)parentElement);
                } else if (parentElement instanceof IIpsPackageFragment) {
                    return getPackageFragmentContent((IIpsPackageFragment)parentElement);
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
     * Returns an array containing all (non-IPS) folders in the given project and all
     * packageFragmentRoots that exist as folders in the filesystem.
     * <p>
     * When calling <code>IpsProject#getIpsPackageFragmentRoots()</code> the project retrieves all
     * entries from the IpsObjectPath and returns them as handles without checking if the underlying
     * resources actually exist. Thus the filtering of packagefragment roots is necessary to prevent
     * these handles from being displayed in the tree.
     * <p>
     * This problem does not occur with <code>IResource</code>s.
     */
    private Object[] getProjectContent(IIpsProject project) throws CoreException {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List existingRoots = new ArrayList();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].exists()) {
                existingRoots.add(roots[i]);
            }
        }
        Object[] result= concatenate(existingRoots.toArray(), project.getNonIpsResources());
        return result
        ;
    }

    /*
     * Filters the contents of the given <code>IFolder</code> using the javaproject's classpath
     * entries and outputlocations the folder is contained in. If the given folder does not contain
     * any children an empty array is returned. If the <code>IProject</code> returned by
     * <code>IResource#getProject()</code> is null or does not have java nature, an empty array is
     * returned.<br>
     * And filter ips archive files which are specified in the ips object path.
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

            List childResources = new ArrayList();
            for (int i = 0; i < children.length; i++) {
                if (isIpsArchiveFromIpsObjectPath(project, (IResource)children[i])){
                    // filter out ips archive files which are specified in the ipsobjectpath
                    continue;
                }
                if (!isJavaResource(javaProject, (IResource)children[i])) {
                    childResources.add(children[i]);
                }
            }
            return childResources.toArray();
        } catch (JavaModelException e) {
            IpsPlugin.log(e);
            return EMPTY_ARRAY;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return EMPTY_ARRAY;
        }
    }

    /*
     * Returns <code>true</code> if the given resource is a ips archive file which is specified
     * in the ips object path, otherwise return <code>false</code>.
     */
    private boolean isIpsArchiveFromIpsObjectPath(IProject project, IResource resource) {
        if (! (resource instanceof IFile)){
            return false;
        }
        try {
            if (project.hasNature(IIpsProject.NATURE_ID)){
                // check if one of the archive entries in the ips object path is the given file
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
                IIpsArchiveEntry[] archiveEntries = ipsProject.getIpsObjectPath().getArchiveEntries();
                for (int i = 0; i < archiveEntries.length; i++) {
                    if (archiveEntries[i].getArchiveFile().equals(resource)){
                        return true;
                    }
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return false;
    }

    /*
     * Examins the given <code>JavaProject</code> and its relation to the given
     * <code>IResource</code>. Returns true if the given resource corresponds to a classpath
     * entry of the javaproject. Returns true if the given resource corresponds to a folder that is
     * either the javaprojects default output location or the output location of one of the projects
     * classpathentries. False otherwise.
     * 
     * @param resource
     * @return
     */
    private boolean isJavaResource(IJavaProject jProject, IResource resource) {
        try {
            IPath outputPath = jProject.getOutputLocation();
            IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
            if (resource.getFullPath().equals(outputPath)) {
                return true;
            }
            for (int i = 0; i < entries.length; i++) {
                if (resource.getFullPath().equals(entries[i].getOutputLocation())) {
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
     * 
     * @throws CoreException
     */
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) throws CoreException {
        if (isFlatLayout) {
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
            List filteredFragments = new ArrayList();
            for (int i = 0; i < fragments.length; i++) {
                if (hasChildren(fragments[i]) || fragments[i].getChildIpsPackageFragments().length == 0) {
                    filteredFragments.add(fragments[i]);
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
     * defaultPackageFragment of its <code>IpsPackageFragmentRoot</code>, only the contained
     * files are returned.
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
     * <code>IpsPacakgeFragment</code>s that correspond to a subfolder of the given
     * packagefragments underlying folder.
     * 
     * @throws CoreException
     */
    private Object[] getFolderContent(IIpsPackageFragment fragment) throws CoreException {
        // in hierarchical layout display childpackagefragments as children
        if (!isFlatLayout) {
            return fragment.getChildIpsPackageFragments();
        } else {
            return EMPTY_ARRAY;
        }
    }

    /**
     * Returns all files contained in the given <code>IpsPackageFragment</code>. This includes
     * IpsElements as well as general files.
     * 
     * @throws CoreException
     */
    protected Object[] getFileContent(IIpsPackageFragment fragment) throws CoreException {
        IIpsElement[] files = fragment.getChildren();

        List pcts = new ArrayList();
        for (int i = 0, size = files.length; i < size; i++) {
            if (files[i] instanceof IIpsSrcFile) {
                IFile file = ((IIpsSrcFile)files[i]).getCorrespondingFile();
                if (file!=null && !file.isSynchronized(IResource.DEPTH_ZERO)) {
                    file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
                }
                pcts.add(((IIpsSrcFile)files[i]).getIpsObject());
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
        List filtered = new ArrayList();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof IIpsElement) {
                if (configuration.isAllowedIpsElement(elements[i])) {
                    filtered.add(elements[i]);
                }
            } else if (elements[i] instanceof IResource) {
                // filter out hidden files and folders, except the ".ipsproject"-file
                if (elements[i] instanceof IFile | elements[i] instanceof IFolder) {
                    if (((IResource)elements[i]).getName().indexOf(".") == 0) { //$NON-NLS-1$
                        IIpsProject project= IpsPlugin.getDefault().getIpsModel().getIpsProject(((IResource)elements[i]).getProject());
                        if(!elements[i].equals(project.getIpsProjectPropertiesFile())){ //$NON-NLS-1$
                            continue;
                        }
                    }
                }
                if (elements[i] instanceof IFile) {
                    if (((IResource)elements[i]).getName().endsWith(".class")) { //$NON-NLS-1$
                        continue;
                    }
                }
                if (configuration.isAllowedResource(elements[i])) {
                    filtered.add(elements[i]);
                }
            }
        }
        return filtered.toArray();
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement parent;
            if (!isFlatLayout && element instanceof IIpsPackageFragment) {
                IIpsPackageFragment fragment = (IIpsPackageFragment)element;
                if (fragment.isDefaultPackage()) {
                    parent = fragment.getRoot();
                } else {
                    parent = ((IIpsPackageFragment)element).getParentIpsPackageFragment();
                }
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
            return ((IResource)element).getParent();
        }
        return null;
    }

    /**
     * For IIpsElements this method returns true if the element contains at least one child, that is
     * of a type allowed by the ModelExplorerConfiguration.
     */
    public boolean hasChildren(Object element) {
        return getChildren(element).length >= 1;
    }

    /**
     * Returns all projects currently managed by the IpsModel. This includes java projects as well
     * as other types of projects. An empty array is returned if no such projects are found, or if
     * the given inputElement is not an <code>IIpsModel</code>. {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IIpsModel) {
            try {
                IIpsModel model = (IIpsModel)inputElement;
                if (excludeNoIpsProjects){
                    // return only ips projects and closed projects
                    return concatenate(model.getIpsProjects(), getClosedProjects(model));
                } else {
                    // return alll kind of projects (ips- and no ips projects)
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
        List closedProjects = new ArrayList();
        for (int i = 0; i < nonIpsProjects.length; i++) {
            if (nonIpsProjects[i] instanceof IProject && ! ((IProject)nonIpsProjects[i]).isOpen()){
                closedProjects.add(nonIpsProjects[i]);
            }
        }
        return (IProject[]) closedProjects.toArray(new IProject[closedProjects.size()]);
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /**
     * Sets the flag for flat respectivly hierarchical PackageFragments
     * 
     * @param b
     */
    /* package */void setIsFlatLayout(boolean b) {
        isFlatLayout = b;
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
     * Set <code>true</code> to exlude no ips projects or <code>false</code> to show no ips projects.
     */
    public void setExcludeNoIpsProjects(boolean excludeNoIpsProjects) {
        this.excludeNoIpsProjects = excludeNoIpsProjects;
    }
}
